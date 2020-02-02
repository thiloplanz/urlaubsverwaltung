package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.synyx.urlaubsverwaltung.person.PersonService;

@Configuration
public class LdapSecurityConfiguration {

    @Configuration
    @ConditionalOnProperty(name = "uv.security.auth", havingValue = "ldap")
    static class LdapAuthConfiguration {

        private final DirectoryServiceSecurityProperties directoryServiceSecurityProperties;
        private final LdapSecurityConfigurationProperties ldapProperties;

        @Autowired
        public LdapAuthConfiguration(DirectoryServiceSecurityProperties directoryServiceSecurityProperties, LdapSecurityConfigurationProperties ldapProperties) {
            this.directoryServiceSecurityProperties = directoryServiceSecurityProperties;
            this.ldapProperties = ldapProperties;
        }

        @Bean
        public LdapContextSource ldapContextSource() {
            final LdapContextSource source = new LdapContextSource();
            source.setUserDn(ldapProperties.getManagerDn());
            source.setPassword(ldapProperties.getManagerPassword());
            source.setBase(ldapProperties.getBase());
            source.setUrl(ldapProperties.getUrl());

            return source;
        }

        @Bean
        public LdapAuthoritiesPopulator authoritiesPopulator() {
            return new DefaultLdapAuthoritiesPopulator(ldapContextSource(), null);
        }

        @Bean
        public FilterBasedLdapUserSearch ldapUserSearch() {
            final String searchBase = ldapProperties.getUserSearchBase();
            final String searchFilter = ldapProperties.getUserSearchFilter();

            return new FilterBasedLdapUserSearch(searchBase, searchFilter, ldapContextSource());
        }

        @Bean
        public LdapAuthenticator authenticator() {
            final BindAuthenticator authenticator = new BindAuthenticator(ldapContextSource());
            authenticator.setUserSearch(ldapUserSearch());

            return authenticator;
        }

        @Bean
        public AuthenticationProvider ldapAuthenticationProvider(LdapPersonContextMapper ldapPersonContextMapper) {
            final LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(authenticator(), authoritiesPopulator());
            ldapAuthenticationProvider.setUserDetailsContextMapper(ldapPersonContextMapper);

            return ldapAuthenticationProvider;
        }


        @Bean
        public LdapUserMapper ldapUserMapper() {
            return new LdapUserMapper(directoryServiceSecurityProperties);
        }

        @Bean
        public LdapPersonContextMapper personContextMapper(PersonService personService, LdapUserMapper ldapUserMapper) {
            return new LdapPersonContextMapper(personService, ldapUserMapper);
        }

    }

    @Configuration
    @ConditionalOnExpression("'${uv.security.auth}'=='ldap' and '${uv.security.directory-service.ldap.sync.enabled}'=='true'")
    public static class LdapAuthSyncConfiguration {

        private final DirectoryServiceSecurityProperties directoryServiceSecurityProperties;
        private final LdapSecurityConfigurationProperties ldapProperties;

        @Autowired
        public LdapAuthSyncConfiguration(DirectoryServiceSecurityProperties directoryServiceSecurityProperties, LdapSecurityConfigurationProperties ldapProperties) {
            this.directoryServiceSecurityProperties = directoryServiceSecurityProperties;
            this.ldapProperties = ldapProperties;
        }

        @Bean
        public LdapContextSourceSync ldapContextSourceSync() {
            return new LdapContextSourceSync(ldapProperties);
        }

        @Bean
        public LdapUserDataImportConfiguration ldapUserDataImportConfiguration(LdapUserDataImporter ldapUserDataImporter) {
            return new LdapUserDataImportConfiguration(directoryServiceSecurityProperties, ldapUserDataImporter);
        }

        @Bean
        public LdapUserDataImporter ldapUserDataImporter(LdapUserService ldapUserService, PersonService personService) {
            return new LdapUserDataImporter(ldapUserService, personService);
        }

        @Bean
        public LdapUserServiceImpl ldapUserService(LdapTemplate ldapTemplate, LdapUserMapper ldapUserMapper) {
            return new LdapUserServiceImpl(ldapTemplate, ldapUserMapper, directoryServiceSecurityProperties);
        }

        @Bean
        public LdapTemplate ldapTemplate() {
            return new UVLdapTemplate(ldapContextSourceSync());
        }
    }
}
