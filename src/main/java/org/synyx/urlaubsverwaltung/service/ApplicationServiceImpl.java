package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.Comment;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.util.CalcUtil;

import java.math.BigDecimal;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import java.util.List;


/**
 * implementation of the applicationdata-access-service.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private ApplicationDAO applicationDAO;
    private HolidaysAccountService accountService;
    private CryptoService cryptoService;
    private OwnCalendarService calendarService;
    private MailService mailService;
    private CalculationService calculationService;

    @Autowired
    public ApplicationServiceImpl(ApplicationDAO applicationDAO, HolidaysAccountService accountService,
        CryptoService cryptoService, OwnCalendarService calendarService, MailService mailService,
        CalculationService calculationService) {

        this.applicationDAO = applicationDAO;
        this.accountService = accountService;
        this.cryptoService = cryptoService;
        this.calendarService = calendarService;
        this.mailService = mailService;
        this.calculationService = calculationService;
    }

    @Override
    public Application getApplicationById(Integer id) {

        return applicationDAO.findOne(id);
    }


    /**
     * @see  ApplicationService#getApplicationsByPerson(org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public List<Application> getApplicationsByPerson(Person person) {

        return applicationDAO.getApplicationsByPerson(person);
    }


    /**
     * @see  ApplicationService#getApplicationsByState(org.synyx.urlaubsverwaltung.domain.ApplicationStatus)
     */
    @Override
    public List<Application> getApplicationsByState(ApplicationStatus state) {

        return applicationDAO.getApplicationsByState(state);
    }


    /**
     * @see  ApplicationService#getApplicationsForACertainTime(org.joda.time.DateMidnight, org.joda.time.DateMidnight)
     */
    @Override
    public List<Application> getApplicationsForACertainTime(DateMidnight startDate, DateMidnight endDate) {

        return applicationDAO.getApplicationsForACertainTime(startDate.toDate(), endDate.toDate());
    }


    /**
     * @see  ApplicationService#allow(org.synyx.urlaubsverwaltung.domain.Application,org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void allow(Application application, Person boss) {

        application.setBoss(boss);

        // set state on allowed
        application.setStatus(ApplicationStatus.ALLOWED);

        // sign application and save it
        signApplicationByBoss(application, boss);

// mailService.sendAllowedNotification(application);
    }


    /**
     * @see  ApplicationService#save(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void save(Application application) {

        // if check is successful, application is saved

        application.setStatus(ApplicationStatus.WAITING);

        // set number of used days
        application.setDays(calendarService.getVacationDays(application, application.getStartDate(),
                application.getEndDate()));

        List<HolidaysAccount> accounts = calculationService.subtractVacationDays(application);

        accountService.saveHolidaysAccount(accounts.get(0));

        if (accounts.size() > 1) {
            accountService.saveHolidaysAccount(accounts.get(1));
        }

        // save changed application in the end
        applicationDAO.save(application);

        // mail to applicant
// mailService.sendConfirmation(application);
    }


    /**
     * @see  ApplicationService#reject(org.synyx.urlaubsverwaltung.domain.Application,org.synyx.urlaubsverwaltung.domain.Person,
     *       java.lang.String)
     */
    @Override
    public void reject(Application application, Person boss, Comment reasonToReject) {

        application.setStatus(ApplicationStatus.REJECTED);

        application.setBoss(boss);

        rollback(application);

        applicationDAO.save(application);

//        mailService.sendRejectedNotification(application);
    }


    /**
     * @see  ApplicationService#cancel(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void cancel(Application application) {

        rollback(application);

        if (application.getStatus() == ApplicationStatus.WAITING) {
            application.setStatus(ApplicationStatus.CANCELLED);
            applicationDAO.save(application);

            // if application has been waiting, chefs get email
// mailService.sendCancelledNotification(application, true);
        } else if (application.getStatus() == ApplicationStatus.ALLOWED) {
            application.setStatus(ApplicationStatus.CANCELLED);
            applicationDAO.save(application);

            // if application has been allowed, office gets email
// mailService.sendCancelledNotification(application, false);
        }
    }


    /**
     * @see  ApplicationService#addSickDaysOnHolidaysAccount(org.synyx.urlaubsverwaltung.domain.Application, double)
     */
    @Override
    public void addSickDaysOnHolidaysAccount(Application application, double sickDays) {

        application.setSickDays(BigDecimal.valueOf(sickDays));
        application.setDays(application.getDays().subtract(BigDecimal.valueOf(sickDays)));

        HolidaysAccount account = accountService.getHolidaysAccount(application.getDateOfAddingSickDays().getYear(),
                application.getPerson());

        account = calculationService.addSickDaysOnHolidaysAccount(application, account);

        accountService.saveHolidaysAccount(account);
        applicationDAO.save(application);
    }


    /**
     * signs an application with the private key of the signing boss
     *
     * @param  application
     * @param  boss
     */
    public void signApplicationByBoss(Application application, Person boss) {

        byte[] data = signApplication(application, boss);

        if (data != null) {
            application.setSignatureBoss(data);
            applicationDAO.save(application);
        }
    }


    /**
     * signs an application with the private key of the signing user (applicant)
     *
     * @param  application
     * @param  user
     */
    public void signApplicationByUser(Application application, Person user) {

        byte[] data = signApplication(application, user);

        if (data != null) {
            application.setSignaturePerson(data);
            applicationDAO.save(application);
        }
    }


    // exceptions have to be catched!
    /**
     * generates signature (byte[]) by private key of person
     *
     * @param  application
     * @param  person
     *
     * @return  data (=signature) if using cryptoService was successful or null if there was any mistake
     */
    public byte[] signApplication(Application application, Person person) {

        try {
            PrivateKey privKey = cryptoService.getPrivateKeyByBytes(person.getPrivateKey());

            StringBuilder build = new StringBuilder();

            build.append(application.getPerson().getLastName());
            build.append(application.getApplicationDate().toString());
            build.append(application.getVacationType().toString());

            byte[] data = build.toString().getBytes();

            data = cryptoService.sign(privKey, data);

            return data;
        } // TODO Logging, catchen von Exceptions
        catch (InvalidKeyException ex) {
        } catch (SignatureException ex) {
        } catch (NoSuchAlgorithmException ex) {
        } catch (InvalidKeySpecException ex) {
        }

        return null;
    }


    /**
     * @see  ApplicationService#checkApplication(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public boolean checkApplication(Application application) {

        List<HolidaysAccount> accounts = calculationService.subtractVacationDays(application);
        HolidaysAccount account = accounts.get(0);

        if (accounts.size() == 1) {
            if (CalcUtil.isEqualOrGreaterThanZero(account.getVacationDays())) {
                return true;
            }
        } else if (accounts.size() > 1) {
            HolidaysAccount accountNextYear = accounts.get(1);

            if ((CalcUtil.isEqualOrGreaterThanZero(account.getVacationDays()))
                    && (CalcUtil.isEqualOrGreaterThanZero(accountNextYear.getVacationDays()))) {
                return true;
            }
        }

        return false;
    }


    /**
     * if holiday is cancelled or rejected, calculation in HolidaysAccount has to be reversed
     *
     * @param  application
     */
    private void rollback(Application application) {

        List<HolidaysAccount> accounts = calculationService.addVacationDays(application);

        accountService.saveHolidaysAccount(accounts.get(0));

        if (accounts.size() > 1) {
            accountService.saveHolidaysAccount(accounts.get(1));
        }
    }


    /**
     * @see  ApplicationService#getApplicationsByPersonAndYear(org.synyx.urlaubsverwaltung.domain.Person, int)
     */
    @Override
    public List<Application> getApplicationsByPersonAndYear(Person person, int year) {

        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        return applicationDAO.getApplicationsByPersonAndYear(person, firstDayOfYear.toDate(), lastDayOfYear.toDate());
    }
}
