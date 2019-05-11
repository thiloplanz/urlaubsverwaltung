package org.synyx.urlaubsverwaltung.account.web;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.LocalDatePropertyEditor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;

/**
 * Controller to manage {@link org.synyx.urlaubsverwaltung.account.domain.Account}s of {@link org.synyx.urlaubsverwaltung.person.Person}s.
 */
@Controller
@RequestMapping("/web")
public class AccountController {

    private final PersonService personService;
    private final AccountService accountService;
    private final AccountInteractionService accountInteractionService;
    private final AccountValidator validator;

    @Autowired
    public AccountController(PersonService personService, AccountService accountService, AccountInteractionService accountInteractionService, AccountValidator validator) {
        this.personService = personService;
        this.accountService = accountService;
        this.accountInteractionService = accountInteractionService;
        this.validator = validator;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(LocalDate.class, new LocalDatePropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/staff/{personId}/account")
    public String editAccount(@PathVariable("personId") Integer personId,
        @RequestParam(value = ControllerConstants.YEAR_ATTRIBUTE, required = false) Integer year, Model model)
        throws UnknownPersonException {

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        int yearOfHolidaysAccount = year != null ? year : ZonedDateTime.now(UTC).getYear();
        AccountForm accountForm = new AccountForm(yearOfHolidaysAccount, accountService.getHolidaysAccount(
            yearOfHolidaysAccount, person));

        model.addAttribute("person", person);
        model.addAttribute("account", accountForm);
        model.addAttribute("year", yearOfHolidaysAccount);

        return "account/account_form";
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping("/staff/{personId}/account")
    public String updateAccount(@PathVariable("personId") Integer personId,
        @ModelAttribute("account") AccountForm accountForm, Model model, Errors errors,
        RedirectAttributes redirectAttributes) throws UnknownPersonException {

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        validator.validate(accountForm, errors);

        if (errors.hasErrors()) {
            model.addAttribute("person", person);
            model.addAttribute("year", accountForm.getHolidaysAccountYear());

            return "account/account_form";
        }

        LocalDate validFrom = accountForm.getHolidaysAccountValidFrom();
        LocalDate validTo = accountForm.getHolidaysAccountValidTo();

        BigDecimal annualVacationDays = accountForm.getAnnualVacationDays();
        BigDecimal actualVacationDays = accountForm.getActualVacationDays();
        BigDecimal remainingVacationDays = accountForm.getRemainingVacationDays();
        BigDecimal remainingVacationDaysNotExpiring = accountForm.getRemainingVacationDaysNotExpiring();
        String comment = accountForm.getComment();

        // check if there is an existing account
        Optional<Account> account = accountService.getHolidaysAccount(validFrom.getYear(), person);

        if (account.isPresent()) {
            accountInteractionService.editHolidaysAccount(account.get(), validFrom, validTo, annualVacationDays,
                actualVacationDays, remainingVacationDays, remainingVacationDaysNotExpiring, comment);
        } else {
            accountInteractionService.updateOrCreateHolidaysAccount(person, validFrom, validTo, annualVacationDays,
                actualVacationDays, remainingVacationDays, remainingVacationDaysNotExpiring, comment);
        }

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/staff/" + personId;
    }
}
