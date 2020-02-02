package org.synyx.urlaubsverwaltung.security.oidc;


import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OidcLogoutHandlerTest {

    private OidcLogoutHandler sut;

    @Before
    public void setUp() {

        final OidcSecurityProperties properties = new OidcSecurityProperties();
        properties.setLogoutPath("/log/out/path");

        sut = new OidcLogoutHandler(properties);
    }

    @Test
    public void prepareValidResponse() {

        final Map<String, Object> claims = Map.of(
            IdTokenClaimNames.SUB, "uniqueId",
            IdTokenClaimNames.ISS, "https://issuer.com"
        );
        final OidcIdToken oidcIdToken = new OidcIdToken("tokenValue", Instant.now(), Instant.MAX, claims);
        final DefaultOidcUser user = new DefaultOidcUser(List.of(new SimpleGrantedAuthority("USER")), oidcIdToken);

        final TestingAuthenticationToken authentication = new TestingAuthenticationToken(user, null);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("uv.de");
        request.setServerPort(8080);

        final MockHttpServletResponse response = new MockHttpServletResponse();

        sut.logout(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo("https://issuer.com/log/out/path?id_token_hint=tokenValue&redirect_uri=http://uv.de:8080&client_id&returnTo=http://uv.de:8080");
    }
}
