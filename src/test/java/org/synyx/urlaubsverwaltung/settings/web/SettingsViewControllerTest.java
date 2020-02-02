package org.synyx.urlaubsverwaltung.settings.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(MockitoJUnitRunner.class)
public class SettingsViewControllerTest {

    private SettingsViewController sut;

    private static final String OATUH_REDIRECT_REL = "/google-api-handshake";
    private static final String ERRORS_ATTRIBUTE = "errors";
    private static final String OAUTH_ERROR_ATTRIBUTE = "oautherrors";
    private static final String OAUTH_ERROR_VALUE = "some-error";

    private static final CalendarProvider SOME_CALENDAR_PROVIDER = new SomeCalendarProvider();
    private static final CalendarProvider ANOTHER_CALENDAR_PROVIDER = new AnotherCalendarProvider();
    private static final List<CalendarProvider> CALENDAR_PROVIDER_LIST = List.of(SOME_CALENDAR_PROVIDER, ANOTHER_CALENDAR_PROVIDER);

    private static final String SOME_GOOGLE_REFRESH_TOKEN = "0815-4711-242";

    @Mock
    private SettingsService settingsService;
    @Mock
    private MailService mailService;
    @Mock
    private SettingsValidator settingsValidator;

    @Before
    public void setUp() {
        sut = new SettingsViewController(settingsService, CALENDAR_PROVIDER_LIST, mailService, settingsValidator, "unknown");
    }

    @Test
    public void getAuthorizedRedirectUrl() {

        String actual = sut.getAuthorizedRedirectUrl("http://localhost:8080/web/settings", OATUH_REDIRECT_REL);
        String expected = "http://localhost:8080/web" + OATUH_REDIRECT_REL;
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void ensureSettingsDetailsFillsModelCorrectly() throws Exception {

        final Settings settings = someSettings();
        when(settingsService.getSettings()).thenReturn(settings);

        final String requestUrl = "/web/settings";

        perform(get(requestUrl))
            .andExpect(model().attribute("settings", settings))
            .andExpect(model().attribute("federalStateTypes", FederalState.values()))
            .andExpect(model().attribute("dayLengthTypes", DayLength.values()))
            .andExpect(model().attribute("providers", contains("SomeCalendarProvider", "AnotherCalendarProvider")))
            .andExpect(model().attribute("availableTimezones", containsInAnyOrder(TimeZone.getAvailableIDs())))
            .andExpect(model().attribute("authorizedRedirectUrl",
                sut.getAuthorizedRedirectUrl("http://localhost" + requestUrl, OATUH_REDIRECT_REL)));
    }

    @Test
    public void ensureSettingsDetailsAddsOAuthErrorToModelIfErrorProvidedAndNoCurrentRefreshToken() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettingsWithoutGoogleCalendarRefreshToken());

        perform(get("/web/settings")
            .param(OAUTH_ERROR_ATTRIBUTE, OAUTH_ERROR_VALUE))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, OAUTH_ERROR_VALUE))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, OAUTH_ERROR_VALUE));
    }

    @Test
    public void ensureSettingsDetailsDoesNotAddOAuthErrorToModelIfErrorProvidedAndCurrentRefreshToken() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettingsWithGoogleCalendarRefreshToken());

        perform(get("/web/settings")
            .param(OAUTH_ERROR_ATTRIBUTE, OAUTH_ERROR_VALUE))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, nullValue()))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, nullValue()));
    }

    @Test
    public void ensureSettingsDetailsDoesNotAddOAuthErrorToModelIfNoErrorProvidedAndNoCurrentRefreshToken() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettingsWithoutGoogleCalendarRefreshToken());

        perform(get("/web/settings"))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, nullValue()))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, nullValue()));
    }

    @Test
    public void ensureSettingsDetailsDoesNotAddOAuthErrorToModelIfNoErrorProvidedAndCurrentRefreshToken() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettingsWithGoogleCalendarRefreshToken());

        perform(get("/web/settings"))
            .andExpect(model().attribute(OAUTH_ERROR_ATTRIBUTE, nullValue()))
            .andExpect(model().attribute(ERRORS_ATTRIBUTE, nullValue()));
    }

    @Test
    public void ensureSettingsDetailsSetsDefaultExchangeTimeZoneIfNoneConfigured() throws Exception {

        final Settings settings = someSettingsWithNoExchangeTimezone();
        when(settingsService.getSettings()).thenReturn(settings);

        assertThat(settings.getCalendarSettings().getExchangeCalendarSettings().getTimeZoneId()).isNull();

        perform(get("/web/settings"));

        assertThat(settings.getCalendarSettings().getExchangeCalendarSettings().getTimeZoneId())
            .isEqualTo(TimeZone.getDefault().getID());
    }

    @Test
    public void ensureSettingsDetailsDoesNotAlterExchangeTimeZoneIfAlreadyConfigured() throws Exception {

        final String timeZoneId = "XYZ";
        final Settings settings = someSettingsWithExchangeTimeZone(timeZoneId);
        when(settingsService.getSettings()).thenReturn(someSettingsWithExchangeTimeZone(timeZoneId));

        assertThat(settings.getCalendarSettings().getExchangeCalendarSettings().getTimeZoneId()).isEqualTo(timeZoneId);

        perform(get("/web/settings"));

        assertThat(settings.getCalendarSettings().getExchangeCalendarSettings().getTimeZoneId()).isEqualTo(timeZoneId);
    }

    @Test
    public void ensureSettingsDetailsUsesCorrectView() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettings());

        perform(get("/web/settings")).andExpect(view().name("settings/settings_form"));
    }

    @Test
    public void ensureSettingsSavedShowsFormIfValidationFails() throws Exception {

        doAnswer(invocation -> {

            Errors errors = invocation.getArgument(1);
            errors.rejectValue("absenceSettings", "error");
            return null;
        }).when(settingsValidator).validate(any(), any());

        perform(post("/web/settings"))
            .andExpect(view().name("settings/settings_form"));
    }

    @Test
    public void ensureSettingsSavedSavesSettingsIfValidationSuccessfully() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettings());

        perform(post("/web/settings"));
        verify(settingsService).save(any(Settings.class));
    }

    @Test
    public void ensureSettingsSavedSendsTechnicalMailIfValidationSuccessfullyAndMailSettingsWebConfigured() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettings());

        perform(post("/web/settings"));
        verify(mailService).sendTechnicalMail(anyString(), anyString(), anyMap());
    }

    @Test
    public void ensureSettingsSavedSendsNoTechnicalMailIfValidationSuccessfullyAndMailSettingsFromApplicationProperties() throws Exception {
        sut = new SettingsViewController(settingsService, CALENDAR_PROVIDER_LIST, mailService, settingsValidator, "my.smtp.server");
        when(settingsService.getSettings()).thenReturn(someSettings());

        perform(post("/web/settings"));
        verifyZeroInteractions(mailService);
    }

    @Test
    public void ensureSettingsSavedAddFlashAttributeAndRedirectsToSettings() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettings());

        perform(post("/web/settings")).andExpect(flash().attribute("success", true));

        perform(post("/web/settings"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/web/settings"));
    }

    private static Settings someSettings() {

        return new Settings();
    }

    private static Settings someSettingsWithNoExchangeTimezone() {

        return someSettings();
    }

    private static Settings someSettingsWithExchangeTimeZone(String timeZoneId) {

        Settings settings = someSettings();
        settings.getCalendarSettings().getExchangeCalendarSettings().setTimeZoneId(timeZoneId);

        return settings;
    }

    private static Settings someSettingsWithoutGoogleCalendarRefreshToken() {

        return someSettings();
    }

    private static Settings someSettingsWithGoogleCalendarRefreshToken() {

        Settings settings = someSettings();
        settings.getCalendarSettings().getGoogleCalendarSettings().setRefreshToken(SOME_GOOGLE_REFRESH_TOKEN);

        return settings;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }

    private static class SomeCalendarProvider implements CalendarProvider {

        @Override
        public Optional<String> add(Absence absence, CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalenderProvider impl.");
        }

        @Override
        public void update(Absence absence, String eventId, CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalenderProvider impl.");
        }

        @Override
        public void delete(String eventId, CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalenderProvider impl.");
        }

        @Override
        public void checkCalendarSyncSettings(CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalenderProvider impl.");
        }
    }

    private static class AnotherCalendarProvider implements CalendarProvider {

        @Override
        public Optional<String> add(Absence absence, CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalenderProvider impl.");
        }

        @Override
        public void update(Absence absence, String eventId, CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalenderProvider impl.");
        }

        @Override
        public void delete(String eventId, CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalenderProvider impl.");
        }

        @Override
        public void checkCalendarSyncSettings(CalendarSettings calendarSettings) {
            throw new UnsupportedOperationException("This is just a mock to have some named CalenderProvider impl.");
        }
    }
}
