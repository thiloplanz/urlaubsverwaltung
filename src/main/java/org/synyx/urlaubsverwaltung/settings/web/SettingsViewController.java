package org.synyx.urlaubsverwaltung.settings.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.GoogleCalendarSettings;
import org.synyx.urlaubsverwaltung.settings.MailSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.LocalDatePropertyEditor;
import org.synyx.urlaubsverwaltung.web.TimePropertyEditor;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/web/settings")
public class SettingsViewController {

    private final SettingsService settingsService;
    private final List<CalendarProvider> calendarProviders;
    private final MailService mailService;
    private final SettingsValidator settingsValidator;
    private final boolean isMailServerFromApplicationProperties;

    @Autowired
    public SettingsViewController(SettingsService settingsService,
                                  List<CalendarProvider> calendarProviders,
                                  MailService mailService,
                                  SettingsValidator settingsValidator,
                                  @Value("${spring.mail.host:unknown}") String mailServerFromApplicationProperties) {
        this.settingsService = settingsService;
        this.calendarProviders = calendarProviders;
        this.mailService = mailService;
        this.settingsValidator = settingsValidator;
        this.isMailServerFromApplicationProperties = !mailServerFromApplicationProperties.equalsIgnoreCase("unknown");
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping
    public String settingsDetails(Model model,
                                  @RequestParam(value = "oautherrors", required = false) String googleOAuthError,
                                  HttpServletRequest request) {

        String authorizedRedirectUrl = getAuthorizedRedirectUrl(
            request.getRequestURL().toString(), "/google-api-handshake");

        Settings settings = settingsService.getSettings();

        fillModel(model, settings, authorizedRedirectUrl);

        if (shouldShowOAuthError(googleOAuthError, settings)) {
            model.addAttribute("errors", googleOAuthError);
            model.addAttribute("oautherrors", googleOAuthError);
        }

        return "settings/settings_form";
    }

    String getAuthorizedRedirectUrl(String requestURL, String redirectPath) {
        return requestURL.replace("/settings", redirectPath);
    }

    private void fillModel(Model model, Settings settings, String authorizedRedirectUrl) {
        model.addAttribute("settings", settings);
        model.addAttribute("federalStateTypes", FederalState.values());
        model.addAttribute("dayLengthTypes", DayLength.values());
        model.addAttribute("isMailServerFromApplicationProperties", isMailServerFromApplicationProperties);

        List<String> providers = calendarProviders.stream()
            .map(provider -> provider.getClass().getSimpleName())
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());
        model.addAttribute("providers", providers);

        List<String> availableTimezones = Arrays.asList(TimeZone.getAvailableIDs());
        model.addAttribute("availableTimezones", availableTimezones);

        if (settings.getCalendarSettings().getExchangeCalendarSettings().getTimeZoneId() == null) {
            settings.getCalendarSettings().getExchangeCalendarSettings().setTimeZoneId(TimeZone.getDefault().getID());
        }

        model.addAttribute("authorizedRedirectUrl", authorizedRedirectUrl);
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping
    public String settingsSaved(@ModelAttribute("settings") Settings settings,
                                Errors errors,
                                Model model,
                                RedirectAttributes redirectAttributes,
                                @RequestParam(value = "googleOAuthButton", required = false) String googleOAuthButton,
                                HttpServletRequest request) {

        String authorizedRedirectUrl = getAuthorizedRedirectUrl(
            request.getRequestURL().toString(), "oautherrors");

        settingsValidator.validate(settings, errors);

        if (errors.hasErrors()) {
            fillModel(model, settings, authorizedRedirectUrl);

            model.addAttribute("errors", errors);

            return "settings/settings_form";
        }

        if (isMailServerFromApplicationProperties) {
            settings.setMailSettings(new MailSettings());
        }

        settingsService.save(processGoogleRefreshToken(settings));

        if (!isMailServerFromApplicationProperties) {
            sendSuccessfullyUpdatedSettingsNotification(settings);
        }

        if (googleOAuthButton != null) {
            return "redirect:/web/google-api-handshake";
        }

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings";
    }


    /**
     * Sends mail to the tool's manager if settings has been updated to ensure that the mail configuration works.
     *
     * @param settings the updated {@link Settings} to notify via mail
     */
    private void sendSuccessfullyUpdatedSettingsNotification(Settings settings) {

        Map<String, Object> model = new HashMap<>();
        model.put("host", settings.getMailSettings().getHost());
        model.put("port", settings.getMailSettings().getPort());

        mailService.sendTechnicalMail("subject.settings.updated", "updated_settings", model);
    }

    private Settings processGoogleRefreshToken(Settings settingsUpdate) {
        Settings storedSettings = settingsService.getSettings();

        GoogleCalendarSettings storedGoogleSettings = storedSettings.getCalendarSettings().getGoogleCalendarSettings();
        GoogleCalendarSettings updateGoogleSettings = settingsUpdate.getCalendarSettings().getGoogleCalendarSettings();

        updateGoogleSettings.setRefreshToken(storedGoogleSettings.getRefreshToken());

        if (refreshTokenGotInvalid(storedGoogleSettings, updateGoogleSettings)) {
            // refresh token is invalid if settings changed
            updateGoogleSettings.setRefreshToken(null);
        }

        return settingsUpdate;
    }

    private boolean refreshTokenGotInvalid(GoogleCalendarSettings oldSettings, GoogleCalendarSettings newSettings) {
        if (oldSettings.getClientSecret() == null
            || oldSettings.getClientId() == null
            || oldSettings.getCalendarId() == null) {
            return true;
        }

        return !oldSettings.equals(newSettings);
    }

    private boolean shouldShowOAuthError(String googleOAuthError, Settings settings) {
        return googleOAuthError != null
            && !googleOAuthError.isEmpty()
            && settings.getCalendarSettings().getGoogleCalendarSettings().getRefreshToken() == null;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(LocalDate.class, new LocalDatePropertyEditor());
    }

}
