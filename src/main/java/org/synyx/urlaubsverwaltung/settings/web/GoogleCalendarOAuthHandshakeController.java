package org.synyx.urlaubsverwaltung.settings.web;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.settings.GoogleCalendarSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSyncService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.calendarintegration.providers.google.GoogleCalendarSyncProvider.APPLICATION_NAME;

@Controller
@RequestMapping("/web")
public class GoogleCalendarOAuthHandshakeController {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static HttpTransport httpTransport;

    private final SettingsService settingsService;
    private final CalendarSyncService calendarSyncService;

    private GoogleAuthorizationCodeFlow flow;

    @Autowired
    public GoogleCalendarOAuthHandshakeController(SettingsService settingsService, CalendarSyncService calendarSyncService)
            throws GeneralSecurityException, IOException {

        this.settingsService = settingsService;
        this.calendarSyncService = calendarSyncService;
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping(ControllerConstants.OATUH_REDIRECT_REL)
    public String googleConnectionStatus(HttpServletRequest request) {
        String redirectUrl = request.getRequestURL().toString();

        return "redirect:" + authorize(redirectUrl);
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping(value = ControllerConstants.OATUH_REDIRECT_REL, params = "code")
    public String oauth2Callback(@RequestParam(value = "code") String code, HttpServletRequest request) {

        String redirectUrl = request.getRequestURL().toString();

        try {
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUrl).execute();
            Credential credential = flow.createAndStoreCredential(response, "userID");
            com.google.api.services.calendar.Calendar client =
                    new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                            .setApplicationName(APPLICATION_NAME).build();

            Settings settings = settingsService.getSettings();
            HttpResponse httpResponse = checkGoogleCalendar(client, settings);

            if (httpResponse.getStatusCode() == HttpStatus.SC_OK) {
                String refreshToken = credential.getRefreshToken();
                if (refreshToken == null) {
                    LOG.warn("OAuth Handshake was successful, but refresh token is null!");
                } else {
                    LOG.info("OAuth Handshake was successful!");
                }
                settings.getCalendarSettings().getGoogleCalendarSettings()
                        .setRefreshToken(refreshToken);
                settingsService.save(settings);
                calendarSyncService.checkCalendarSyncSettings();
            } else {
                LOG.warn("OAuth handshake error {}", httpResponse.getStatusMessage());
            }

        } catch (IOException e) {
            LOG.error("Exception while handling OAuth2 callback ({}) Redirecting to google connection status page.", e.getMessage(), e);
        }

        StringBuilder buf = new StringBuilder();
        buf.append("redirect:/web/settings");
        buf.append("#calendar");

        return buf.toString();
    }

    private static HttpResponse checkGoogleCalendar(Calendar client, Settings settings) throws IOException {
        String calendarId = settings.getCalendarSettings().getGoogleCalendarSettings().getCalendarId();
        Calendar.Calendars.Get metadata = client.calendars().get(calendarId);
        return metadata.buildHttpRequestUsingHead().execute();
    }

    private String authorize(String redirectUri) {
        AuthorizationCodeRequestUrl authorizationUrl;

        Details web = new Details();
        GoogleCalendarSettings googleCalendarSettings =
                settingsService.getSettings().getCalendarSettings().getGoogleCalendarSettings();

        web.setClientId(googleCalendarSettings.getClientId());
        web.setClientSecret(googleCalendarSettings.getClientSecret());

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();

        clientSecrets.setWeb(web);

        flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
                Collections.singleton(CalendarScopes.CALENDAR))
                .setApprovalPrompt("force")
                .setAccessType("offline")
                .build();

        authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri);


        LOG.info("using authorizationUrl {}", authorizationUrl);
        return authorizationUrl.build();
    }
}
