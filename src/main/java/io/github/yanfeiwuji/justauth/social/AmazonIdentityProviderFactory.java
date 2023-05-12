package io.github.yanfeiwuji.justauth.social;

import io.github.yanfeiwuji.justauth.social.common.JustAuthKey;
import io.github.yanfeiwuji.justauth.social.common.JustIdentityProvider;
import io.github.yanfeiwuji.justauth.social.common.JustIdentityProviderConfig;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import me.zhyd.oauth.request. AuthAmazonRequest;

/**
 * @author yanfeiwuji
 * @date 2021/1/10 5:48 下午
 */

public class AmazonIdentityProviderFactory extends
        AbstractIdentityProviderFactory<JustIdentityProvider< AuthAmazonRequest>>
        implements SocialIdentityProviderFactory<JustIdentityProvider< AuthAmazonRequest>> {

  public static final JustAuthKey JUST_AUTH_KEY = JustAuthKey.  AMAZON;

  @Override
  public String getName() {
    return JUST_AUTH_KEY.getName();
  }

  @Override
  public JustIdentityProvider< AuthAmazonRequest> create(KeycloakSession session, IdentityProviderModel model) {
    return new JustIdentityProvider<>(session, new JustIdentityProviderConfig<>(model,JUST_AUTH_KEY, AuthAmazonRequest::new));
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
