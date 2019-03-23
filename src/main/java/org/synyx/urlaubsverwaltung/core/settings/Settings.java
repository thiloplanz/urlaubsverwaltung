package org.synyx.urlaubsverwaltung.core.settings;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;


/**
 * Represents the settings / business rules for the application.
 */
@Entity
public class Settings extends AbstractPersistable<Integer> {

    private AbsenceSettings absenceSettings;

    private WorkingTimeSettings workingTimeSettings;

    private MailSettings mailSettings;

    private CalendarSettings calendarSettings;

    public AbsenceSettings getAbsenceSettings() {

        if (absenceSettings == null) {
            absenceSettings = new AbsenceSettings();
        }

        return absenceSettings;
    }


    public void setAbsenceSettings(AbsenceSettings absenceSettings) {

        this.absenceSettings = absenceSettings;
    }


    public WorkingTimeSettings getWorkingTimeSettings() {

        if (workingTimeSettings == null) {
            workingTimeSettings = new WorkingTimeSettings();
        }

        return workingTimeSettings;
    }


    public void setWorkingTimeSettings(WorkingTimeSettings workingTimeSettings) {

        this.workingTimeSettings = workingTimeSettings;
    }


    public MailSettings getMailSettings() {

        if (mailSettings == null) {
            mailSettings = new MailSettings();
        }

        return mailSettings;
    }


    public void setMailSettings(MailSettings mailSettings) {

        this.mailSettings = mailSettings;
    }


    public CalendarSettings getCalendarSettings() {

        if (calendarSettings == null) {
            calendarSettings = new CalendarSettings();
        }

        return calendarSettings;
    }


    public void setCalendarSettings(CalendarSettings calendarSettings) {

        this.calendarSettings = calendarSettings;
    }


    @Override
    public void setId(Integer id) { // NOSONAR - make it public instead of protected

        super.setId(id);
    }


    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
