package io.github.yanfeiwuji.justauth.social;

import io.github.yanfeiwuji.justauth.social.common.JustAuthKey;
import io.github.yanfeiwuji.justauth.social.common.JustIdentityProvider;
import io.github.yanfeiwuji.justauth.social.common.JustIdentityProviderConfig;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import me.zhyd.oauth.request. AuthFeishuRequest;

/**
 * @author yanfeiwuji
 * @date 2021/1/10 5:48 下午
 */

public class FeishuIdentityProviderFactory extends
        AbstractIdentityProviderFactory<JustIdentityProvider< AuthFeishuRequest>>
        implements SocialIdentityProviderFactory<JustIdentityProvider< AuthFeishuRequest>> {

  public static final JustAuthKey JUST_AUTH_KEY = JustAuthKey.  FEISHU;

  @Override
  public String getName() {
    return JUST_AUTH_KEY.getName();
  }

  @Override
  public JustIdentityProvider< AuthFeishuRequest> create(KeycloakSession session, IdentityProviderModel model) {
    return new JustIdentityProvider<>(session, new JustIdentityProviderConfig<>(model,JUST_AUTH_KEY, AuthFeishuRequest::new));
  }

  @Override
  public OAuth2IdentityProviderConfig createConfig() {
    return new OAuth2IdentityProviderConfig();
  }

  @Override
  public String getId() {
    return JUST_AUTH_KEY.getId();
  }
}
