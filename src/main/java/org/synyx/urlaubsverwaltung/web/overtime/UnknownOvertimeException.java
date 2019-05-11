package org.synyx.urlaubsverwaltung.web.overtime;

import org.synyx.urlaubsverwaltung.web.AbstractNoResultFoundException;


/**
 * Thrown in case no overtime record found for a certain ID.
 */
public class UnknownOvertimeException extends AbstractNoResultFoundException {

    public UnknownOvertimeException(Integer id) {

        super(id, "overtime");
    }
}
