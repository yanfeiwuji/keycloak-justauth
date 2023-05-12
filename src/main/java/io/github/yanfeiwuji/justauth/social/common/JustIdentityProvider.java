package io.github.yanfeiwuji.justauth.social.common;


import com.alibaba.fastjson.JSONObject;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.request.AuthRequest;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.util.IdentityBrokerState;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.JsonUtils;


import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import java.util.function.Function;

/**
 * @author yanfeiwuji
 * @date 2021/1/10 4:37 下午
 */
public class JustIdentityProvider<T extends AuthDefaultRequest> extends AbstractOAuth2IdentityProvider<JustIdentityProviderConfig> implements SocialIdentityProvider<JustIdentityProviderConfig> {

    public final String DEFAULT_SCOPES = "default";
    //OAuth2IdentityProviderConfig
    public final AuthConfig AUTH_CONFIG;
    public final Function<AuthConfig, T> authToReqFunc;

    public final String providerId;


    public JustIdentityProvider(KeycloakSession session, JustIdentityProviderConfig<T> config) {
        super(session, config);
        this.AUTH_CONFIG = JustAuthKey.getAuthConfig(config);
        this.authToReqFunc = config.getAuthToReqFunc();
        this.providerId = config.getProviderId();
    }

    @Override
    protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
        String redirectUri = request.getRedirectUri();
        AuthRequest authRequest = getAuthRequest(AUTH_CONFIG, redirectUri);
        String uri = authRequest.authorize(request.getState().getEncoded());
        return UriBuilder.fromUri(uri);
    }

    private AuthRequest getAuthRequest(AuthConfig authConfig, String redirectUri) {
        authConfig.setRedirectUri(redirectUri);
        return authToReqFunc.apply(authConfig);
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPES;
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new Endpoint(session, callback, event);
    }


    protected class Endpoint {
        protected final RealmModel realm;
        protected final AuthenticationCallback callback;
        protected final EventBuilder event;

        protected final KeycloakSession session;

        protected final ClientConnection clientConnection;

        protected final HttpHeaders headers;

        public Endpoint(KeycloakSession session, AuthenticationCallback callback, EventBuilder event) {
            this.session = session;
            this.realm = session.getContext().getRealm();
            this.clientConnection = session.getContext().getConnection();
            this.callback = callback;
            this.event = event;
            this.headers = session.getContext().getRequestHeaders();
        }

        private void sendErrorEvent() {
            event.event(EventType.LOGIN);
            event.error(providerId + "_login_failed");
        }

        @GET
        public Response authResponse(@QueryParam("state") String state,
                                     @QueryParam("code") String authorizationCode,
                                     @QueryParam("error") String error) {
            AuthCallback authCallback = AuthCallback.builder().code(authorizationCode).state(state).build();

            IdentityBrokerState idpState = IdentityBrokerState.encoded(state, realm);
            String clientId = idpState.getClientId();
            String tabId = idpState.getTabId();

            if (clientId == null || tabId == null) {
                logger.errorf("Invalid state parameter: %s", state);
                sendErrorEvent();
                return ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.INVALID_REQUEST);
            }
            ClientModel client = realm.getClientByClientId(clientId);

            AuthenticationSessionModel
                    authSession = ClientSessionCode.getClientSession(state, tabId, session, realm, client, event, AuthenticationSessionModel.class);

            // 没有check 不通过
            String redirectUri = "https://www.yfwj.com";
            AuthRequest authRequest = getAuthRequest(AUTH_CONFIG, redirectUri);
            authRequest.authorize(state);
            AuthResponse<AuthUser> response = authRequest.login(authCallback);

            logger.infof("response: %s", JSONObject.toJSONString(response));
            if (response.ok()) {


                AuthUser authUser = response.getData();
                JustIdentityProviderConfig config = JustIdentityProvider.this.getConfig();
                BrokeredIdentityContext federatedIdentity = new BrokeredIdentityContext(authUser.getUuid());
                authUser.getRawUserInfo().forEach((k, v) -> {
                    String value = (v instanceof String) ? v.toString() : JSONObject.toJSONString(v);
                    // v  不能过长
                    federatedIdentity.setUserAttribute(config.getAlias() + "-" + k, value);
                });

                if (getConfig().isStoreToken()) {
                    // make sure that token wasn't already set by getFederatedIdentity();
                    // want to be able to allow provider to set the token itself.
                    if (federatedIdentity.getToken() == null) {
                        federatedIdentity.setToken(authUser.getToken().getAccessToken());
                    }
                }

                logger.infof("response ok mark: %s", JSONObject.toJSONString(federatedIdentity));
                federatedIdentity.setUsername(authUser.getUuid());
                federatedIdentity.setBrokerUserId(authUser.getUuid());
                federatedIdentity.setIdpConfig(config);
                federatedIdentity.setIdp(JustIdentityProvider.this);

                logger.infof("response ok mark: %s", JSONObject.toJSONString(federatedIdentity));
                return this.callback.authenticated(federatedIdentity);
            } else {
                logger.errorf("Failed to make identity provider oauth callback: %s", response.getMsg());
                sendErrorEvent();
                return ErrorPage.error(session, authSession, Response.Status.BAD_GATEWAY, Messages.UNEXPECTED_ERROR_HANDLING_RESPONSE);
            }
        }


    }

    @Override
    public Response retrieveToken(KeycloakSession session, FederatedIdentityModel identity) {
        return Response.ok(identity.getToken()).type(MediaType.APPLICATION_JSON).build();
    }

    @Override
    public void authenticationFinished(AuthenticationSessionModel authSession, BrokeredIdentityContext context) {
        authSession.setUserSessionNote(IdentityProvider.FEDERATED_ACCESS_TOKEN, (String) context.getContextData().get(IdentityProvider.FEDERATED_ACCESS_TOKEN));
    }
}
