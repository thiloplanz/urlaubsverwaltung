package org.synyx.urlaubsverwaltung.settings;

import javax.persistence.Embeddable;
import java.time.LocalDate;


/**
 * Settings concerning absence of persons because of vacation or sick days.
 */
@Embeddable
public class AbsenceSettings {

    /**
     * Specifies the maximal number of annual vacation days a person can have.
     */
    private Integer maximumAnnualVacationDays = 40; // NOSONAR

    /**
     * Specifies how many months in advance a person can apply for leave.
     */
    private Integer maximumMonthsToApplyForLeaveInAdvance = 12; // NOSONAR

    /**
     * Specifies how many months after the fact a person can apply for leave retroactively.
     */
    private LocalDate retroactiveApplicationCutoff = null; // NOSONAR

    /**
     * Specifies the maximal period of sick pay in days.
     */
    private Integer maximumSickPayDays = 42; // NOSONAR

    /**
     * Specifies when a notification about the end of sick pay should be sent to the affected person and office. (number
     * of days before the end of sick pay)
     */
    private Integer daysBeforeEndOfSickPayNotification = 7; // NOSONAR

    /**
     * Activates a notification after {daysBeforeWaitingApplicationsReminderNotification} days for waiting applications
     */
    private boolean remindForWaitingApplications = false;

    /**
     * Specifies when a reminder for waiting application should be send to boss / department head /
     * secondary stage authority
     */
    private Integer daysBeforeRemindForWaitingApplications = 2;

    public Integer getMaximumAnnualVacationDays() {

        return maximumAnnualVacationDays;
    }


    public Integer getMaximumMonthsToApplyForLeaveInAdvance() {

        return maximumMonthsToApplyForLeaveInAdvance;
    }

    public LocalDate getRetroactiveApplicationCutoff() {

        return retroactiveApplicationCutoff;
    }


    public Integer getMaximumSickPayDays() {

        return maximumSickPayDays;
    }


    public Integer getDaysBeforeEndOfSickPayNotification() {

        return daysBeforeEndOfSickPayNotification;
    }


    public void setMaximumAnnualVacationDays(Integer maximumAnnualVacationDays) {

        this.maximumAnnualVacationDays = maximumAnnualVacationDays;
    }


    public void setMaximumMonthsToApplyForLeaveInAdvance(Integer maximumMonthsToApplyForLeaveInAdvance) {

        this.maximumMonthsToApplyForLeaveInAdvance = maximumMonthsToApplyForLeaveInAdvance;
    }

    public void setRetroactiveApplicationCutoff(LocalDate cutoff) {
        this.retroactiveApplicationCutoff = cutoff;
    }


    public void setMaximumSickPayDays(Integer maximumSickPayDays) {

        this.maximumSickPayDays = maximumSickPayDays;
    }


    public void setDaysBeforeEndOfSickPayNotification(Integer daysBeforeEndOfSickPayNotification) {

        this.daysBeforeEndOfSickPayNotification = daysBeforeEndOfSickPayNotification;
    }

    public boolean getRemindForWaitingApplications() {

        return remindForWaitingApplications;
    }

    public void setRemindForWaitingApplications(boolean remindForWaitingApplications) {
        this.remindForWaitingApplications = remindForWaitingApplications;
    }

    public Integer getDaysBeforeRemindForWaitingApplications() {
        return daysBeforeRemindForWaitingApplications;
    }

    public void setDaysBeforeRemindForWaitingApplications(Integer daysBeforeRemindForWaitingApplications) {
        this.daysBeforeRemindForWaitingApplications = daysBeforeRemindForWaitingApplications;
    }

}
