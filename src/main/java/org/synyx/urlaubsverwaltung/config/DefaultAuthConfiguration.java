package org.synyx.urlaubsverwaltung.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SimpleAuthenticationProvider;

@Configuration
@ConditionalOnProperty(name = "auth", havingValue = "default")
public class DefaultAuthConfiguration {

    @Bean
    public AuthenticationProvider defaultAuthenticationProvider(PersonService personService) {
        return new SimpleAuthenticationProvider(personService);
    }

}
