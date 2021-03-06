/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.jldap;

import java.security.Security;
import javax.net.ssl.SSLSocketFactory;
import com.novell.ldap.LDAPConstraints;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.LdapURL;
import org.ldaptive.provider.Provider;
import org.ldaptive.provider.ProviderConnectionFactory;
import org.ldaptive.ssl.TLSSocketFactory;

/**
 * JLdap provider implementation. Provides connection factories for clear, SSL, and TLS connections.
 *
 * @author  Middleware Services
 */
public class JLdapProvider implements Provider<JLdapProviderConfig>
{

  /**
   * Add novell sasl provider.
   */
  static {
    Security.addProvider(new com.novell.sasl.client.SaslProvider());
  }

  /** Provider configuration. */
  private JLdapProviderConfig config = new JLdapProviderConfig();


  @Override
  public ProviderConnectionFactory<JLdapProviderConfig> getConnectionFactory(final ConnectionConfig cc)
  {
    final ProviderConnectionFactory<JLdapProviderConfig> cf;
    if (cc.getUseStartTLS()) {
      cf = getJLdapStartTLSConnectionFactory(cc, config.getLDAPConstraints());
    } else if (cc.getUseSSL() || cc.getLdapUrl().toLowerCase().contains("ldaps://")) {
      cf = getJLdapSSLConnectionFactory(cc, config.getLDAPConstraints());
    } else {
      cf = getJLdapConnectionFactory(cc, config.getLDAPConstraints());
    }
    return cf;
  }


  /**
   * Returns a jldap startTLS connection factory using the properties found in the supplied connection config. If the
   * supplied constraints is null, the environment is retrieved from {@link
   * #getDefaultLDAPConstraints(ConnectionConfig)}.
   *
   * @param  cc  connection config
   * @param  constraints  connection constraints or null to use the default
   *
   * @return  jndi startTLS connection factory
   */
  protected JLdapStartTLSConnectionFactory getJLdapStartTLSConnectionFactory(
    final ConnectionConfig cc,
    final LDAPConstraints constraints)
  {
    return
      new JLdapStartTLSConnectionFactory(
        cc.getLdapUrl(),
        cc.getConnectionStrategy(),
        config,
        constraints != null ? constraints : getDefaultLDAPConstraints(cc),
        cc.getResponseTimeout(),
        config.getSslSocketFactory() != null ? config.getSslSocketFactory() : getHostnameVerifierSocketFactory(cc));
  }


  /**
   * Returns a jldap SSL connection factory using the properties found in the supplied connection config. If the
   * supplied constraints is null, the environment is retrieved from {@link
   * #getDefaultLDAPConstraints(ConnectionConfig)}.
   *
   * @param  cc  connection config
   * @param  constraints  connection constraints or null to use the default
   *
   * @return  jndi SSL connection factory
   */
  protected JLdapSSLConnectionFactory getJLdapSSLConnectionFactory(
    final ConnectionConfig cc,
    final LDAPConstraints constraints)
  {
    return
      new JLdapSSLConnectionFactory(
        cc.getLdapUrl(),
        cc.getConnectionStrategy(),
        config,
        constraints != null ? constraints : getDefaultLDAPConstraints(cc),
        cc.getResponseTimeout(),
        config.getSslSocketFactory() != null ? config.getSslSocketFactory() : getHostnameVerifierSocketFactory(cc));
  }


  /**
   * Returns a jldap connection factory using the properties found in the supplied connection config. If the supplied
   * constraints is null, the environment is retrieved from {@link #getDefaultLDAPConstraints(ConnectionConfig)}.
   *
   * @param  cc  connection config
   * @param  constraints  connection constraints or null to use the default
   *
   * @return  jndi connection factory
   */
  protected JLdapConnectionFactory getJLdapConnectionFactory(
    final ConnectionConfig cc,
    final LDAPConstraints constraints)
  {
    return
      new JLdapConnectionFactory(
        cc.getLdapUrl(),
        cc.getConnectionStrategy(),
        config,
        constraints != null ? constraints : getDefaultLDAPConstraints(cc),
        cc.getResponseTimeout());
  }


  /**
   * Returns an SSL socket factory configured with a default hostname verifier.
   *
   * @param  cc  connection configuration
   *
   * @return  SSL socket factory
   */
  protected SSLSocketFactory getHostnameVerifierSocketFactory(final ConnectionConfig cc)
  {
    // JLdap does not do hostname verification by default
    // set a default hostname verifier
    final LdapURL ldapUrl = new LdapURL(cc.getLdapUrl());
    return TLSSocketFactory.getHostnameVerifierFactory(cc.getSslConfig(), ldapUrl.getHostnames());
  }


  /**
   * Returns the default connection constraints for this provider.
   *
   * @param  cc  to configure options with
   *
   * @return  ldap connection constraints
   */
  protected LDAPConstraints getDefaultLDAPConstraints(final ConnectionConfig cc)
  {
    return new LDAPConstraints();
  }


  @Override
  public JLdapProviderConfig getProviderConfig()
  {
    return config;
  }


  @Override
  public void setProviderConfig(final JLdapProviderConfig pc)
  {
    config = pc;
  }


  @Override
  public JLdapProvider newInstance()
  {
    return new JLdapProvider();
  }
}
