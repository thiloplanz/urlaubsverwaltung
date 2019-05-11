package org.synyx.urlaubsverwaltung.web.workingtime;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.core.period.WeekDay;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;
import org.synyx.urlaubsverwaltung.web.person.UnknownPersonException;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;


/**
 * Controller to manage {@link org.synyx.urlaubsverwaltung.core.workingtime.WorkingTime}s of
 * {@link org.synyx.urlaubsverwaltung.core.person.Person}s.
 */
@Controller
@RequestMapping("/web")
public class WorkingTimeController {

    private final PersonService personService;
    private final WorkingTimeService workingTimeService;
    private final SettingsService settingsService;
    private final WorkingTimeValidator validator;

    @Autowired
    public WorkingTimeController(PersonService personService, WorkingTimeService workingTimeService, SettingsService settingsService, WorkingTimeValidator validator) {
        this.personService = personService;
        this.workingTimeService = workingTimeService;
        this.settingsService = settingsService;
        this.validator = validator;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/staff/{personId}/workingtime")
    public String editWorkingTime(@PathVariable("personId") Integer personId, Model model)
        throws UnknownPersonException {

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
        Optional<WorkingTime> optionalWorkingTime = workingTimeService.getCurrentOne(person);

        if (optionalWorkingTime.isPresent()) {
            model.addAttribute("workingTime", new WorkingTimeForm(optionalWorkingTime.get()));
        } else {
            model.addAttribute("workingTime", new WorkingTimeForm());
        }

        fillModel(model, person);

        return "workingtime/workingtime_form";
    }


    private void fillModel(Model model, Person person) {

        model.addAttribute(PersonConstants.PERSON_ATTRIBUTE, person);
        model.addAttribute("workingTimes", workingTimeService.getByPerson(person));
        model.addAttribute("weekDays", WeekDay.values());
        model.addAttribute("federalStateTypes", FederalState.values());
        model.addAttribute("defaultFederalState",
            settingsService.getSettings().getWorkingTimeSettings().getFederalState());
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping("/staff/{personId}/workingtime")
    public String updateWorkingTime(@PathVariable("personId") Integer personId,
                                    @ModelAttribute("workingTime") WorkingTimeForm workingTimeForm,
                                    Model model,
                                    Errors errors,
                                    RedirectAttributes redirectAttributes) throws UnknownPersonException {

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        validator.validate(workingTimeForm, errors);

        if (errors.hasErrors()) {
            fillModel(model, person);

            return "workingtime/workingtime_form";
        }

        workingTimeService.touch(workingTimeForm.getWorkingDays(),
            Optional.ofNullable(workingTimeForm.getFederalState()), workingTimeForm.getValidFrom(), person);

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/staff/" + personId;
    }
}
