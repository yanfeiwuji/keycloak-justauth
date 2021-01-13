package com.yfwj.justauth.social.common;


import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWeChatOpenRequest;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

/**
 * @author yanfeiwuji
 * @date 2021/1/10 4:37 下午
 */


public class JustIdentityProvider extends AbstractOAuth2IdentityProvider<JustIdentityProviderConfig> implements SocialIdentityProvider<JustIdentityProviderConfig> {

  public final String DEFAULT_SCOPES = "default";
  //OAuth2IdentityProviderConfig
  public final AuthConfig AUTH_CONFIG;
  public final Class<? extends AuthDefaultRequest> tClass;

  public JustIdentityProvider(KeycloakSession session, JustIdentityProviderConfig config) {
    super(session, config);
    JustAuthKey justAuthKey = config.getJustAuthKey();
    AUTH_CONFIG = JustAuthKey.getAuthConfig(config, justAuthKey);
    this.tClass = justAuthKey.getTClass();

  }

  @Override
  protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
    AUTH_CONFIG.setRedirectUri(request.getRedirectUri());
    AuthRequest authRequest = null;
    try {
      Constructor<? extends AuthDefaultRequest> constructor = tClass.getConstructor(AuthConfig.class);
      authRequest = constructor.newInstance(AUTH_CONFIG);
    } catch (Exception e) {
      // can't
      logger.error(e.getMessage());
    }

    String uri = authRequest.authorize(request.getState().getEncoded());
    return UriBuilder.fromUri(uri);
  }

  @Override
  protected String getDefaultScopes() {
    return DEFAULT_SCOPES;
  }

  @Override
  public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
    return new Endpoint(callback, realm, event);
  }



  protected class Endpoint {
    protected AuthenticationCallback callback;
    protected RealmModel realm;
    protected EventBuilder event;
    @Context
    protected KeycloakSession session;
    @Context
    protected ClientConnection clientConnection;
    @Context
    protected HttpHeaders headers;

    public Endpoint(AuthenticationCallback callback, RealmModel realm, EventBuilder event) {
      this.callback = callback;
      this.realm = realm;
      this.event = event;
    }

    @GET
    public Response authResponse(@QueryParam("state") String state,
                                 @QueryParam("code") String authorizationCode,
                                 @QueryParam("error") String error) {
      AuthCallback authCallback = AuthCallback.builder().code(authorizationCode).state(state).build();
      AuthRequest authRequest = null;
      try {
        Constructor<? extends AuthDefaultRequest> constructor = tClass.getConstructor(AuthConfig.class);
        authRequest = constructor.newInstance(AUTH_CONFIG);
      } catch (Exception e) {
        // can't
        logger.error(e.getMessage());
      }


      AuthResponse<AuthUser> response = authRequest.login(authCallback);
      if (response.ok()) {
        AuthUser authUser = response.getData();
        JustIdentityProviderConfig config = JustIdentityProvider.this.getConfig();
        BrokeredIdentityContext federatedIdentity = new BrokeredIdentityContext(authUser.getUuid());
        federatedIdentity.setUserAttribute(config.getAlias(), authUser.getRawUserInfo().toJSONString());
        federatedIdentity.setIdpConfig(config);
        federatedIdentity.setIdp(JustIdentityProvider.this);
        federatedIdentity.setCode(state);
        return this.callback.authenticated(federatedIdentity);
      } else {
        return this.errorIdentityProviderLogin("identityProviderUnexpectedErrorMessage");
      }
    }

    private Response errorIdentityProviderLogin(String message) {
      this.event.event(EventType.LOGIN);
      this.event.error("identity_provider_login_failure");
      return ErrorPage.error(this.session, (AuthenticationSessionModel) null, Response.Status.BAD_GATEWAY, message, new Object[0]);
    }
  }
}
