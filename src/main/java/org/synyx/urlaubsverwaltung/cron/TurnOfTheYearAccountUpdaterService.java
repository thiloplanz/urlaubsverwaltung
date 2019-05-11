
package org.synyx.urlaubsverwaltung.cron;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.ZoneOffset.UTC;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Is to be scheduled every turn of the year: calculates the remaining vacation days for the new year.
 */
@Service
public class TurnOfTheYearAccountUpdaterService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final AccountService accountService;
    private final AccountInteractionService accountInteractionService;
    private final MailService mailService;

    @Autowired
    public TurnOfTheYearAccountUpdaterService(PersonService personService, AccountService accountService,
        AccountInteractionService accountInteractionService, MailService mailService) {

        this.personService = personService;
        this.accountService = accountService;
        this.accountInteractionService = accountInteractionService;
        this.mailService = mailService;
    }

    @Scheduled(cron = "${uv.cron.updateHolidaysAccounts}")
    void updateHolidaysAccounts() {

        LOG.info("Starting update of holidays accounts to calculate the remaining vacation days.");

        // what's the new year?
        int year = ZonedDateTime.now(UTC).getYear();

        // get all persons
        List<Person> persons = personService.getActivePersons();

        List<Account> updatedAccounts = new ArrayList<>();

        // get all their accounts and calculate the remaining vacation days for the new year
        for (Person person : persons) {
            LOG.info("Updating account of {}", person.getLoginName());

            Optional<Account> accountLastYear = accountService.getHolidaysAccount(year - 1, person);

            if (accountLastYear.isPresent() && accountLastYear.get().getAnnualVacationDays() != null) {
                Account holidaysAccount = accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(
                        accountLastYear.get());

                LOG.info("Setting remaining vacation days of {} to {} for {}",
                        person.getLoginName(), holidaysAccount.getRemainingVacationDays(), year);

                updatedAccounts.add(holidaysAccount);
            }
        }

        LOG.info("Successfully updated holidays accounts: {} / {}", updatedAccounts.size(), persons.size());
        mailService.sendSuccessfullyUpdatedAccountsNotification(updatedAccounts);
    }
}
