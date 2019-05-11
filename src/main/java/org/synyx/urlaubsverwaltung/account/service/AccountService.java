package org.synyx.urlaubsverwaltung.account.service;

import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Optional;


/**
 * Provides access to {@link org.synyx.urlaubsverwaltung.account.domain.Account} entities.
 */
public interface AccountService {

    /**
     * Gets the {@link org.synyx.urlaubsverwaltung.account.domain.Account} for the given year and person.
     *
     * @param  year  to get the holidays account for
     * @param  person  to get the holidays account for
     *
     * @return  optional of {@link org.synyx.urlaubsverwaltung.account.domain.Account} that matches the given
     *          parameters
     */
    Optional<Account> getHolidaysAccount(int year, Person person);


    /**
     * Saves the given {@link Account}.
     *
     * @param  account  to be saved
     */
    void save(Account account);
}
