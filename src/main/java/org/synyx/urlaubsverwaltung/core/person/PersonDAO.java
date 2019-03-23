package org.synyx.urlaubsverwaltung.core.person;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Repository for {@link Person} entities.
 */
public interface PersonDAO extends JpaRepository<Person, Integer> {

    Person findByLoginName(String loginName);
}
