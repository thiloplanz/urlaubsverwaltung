package org.synyx.urlaubsverwaltung.core.statistics;

import liquibase.util.csv.CSVWriter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.core.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ApplicationForLeaveStatisticsCsvExportServiceImplTest {


    private MessageSource messageSource;
    private VacationTypeService vacationTypeService;
    private ApplicationForLeaveStatisticsCsvExportServiceImpl sut;

    @Before
    public void setUp() {
        messageSource = mock(MessageSource.class);
        vacationTypeService = mock(VacationTypeService.class);

        sut = new ApplicationForLeaveStatisticsCsvExportServiceImpl();
        // made fields package private to enable testing
        // normally it would be good to use private fields
        // and constructor injection, but at the moment this
        // is not possible in combination with the used spring
        // version 1.4.2.RELEASE and the spring version is defined
        // by the usage of JSPs (older spring versions does not
        // support JSPs)
        // So this is a workaround that should be removed as soon
        // as the spring-version is updated.
        sut.messageSource = messageSource;
        sut.vacationTypeService = vacationTypeService;
    }

    @Test
    public void writeStatisticsForOnePersonFor2018() {
        FilterPeriod period = new FilterPeriod(
                java.util.Optional.ofNullable("01.01.2018"),
                java.util.Optional.ofNullable("31.12.2018"));

        List<ApplicationForLeaveStatistics> statistics = new ArrayList<>();
        Person person = mock(Person.class);
        when(person.getFirstName()).thenReturn("personOneFirstName");
        when(person.getLastName()).thenReturn("personOneLastName");

        VacationTypeService vts = mock(VacationTypeService.class);
        when(vts.getVacationTypes()).thenReturn(new ArrayList());

        ApplicationForLeaveStatistics applicationForLeaveStatistics = mock(ApplicationForLeaveStatistics.class);
        statistics.add(new ApplicationForLeaveStatistics(person, vts));

        CSVWriter csvWriter = mock(CSVWriter.class);

        mockMessageSource("absence.period");

        mockMessageSource("person.data.firstName");
        mockMessageSource("person.data.lastName");
        mockMessageSource("applications.statistics.allowed");
        mockMessageSource("applications.statistics.waiting");
        mockMessageSource("applications.statistics.left");

        mockMessageSource("duration.vacationDays");
        mockMessageSource("duration.overtime");

        mockMessageSource("applications.statistics.total");

        sut.writeStatistics(period, statistics, csvWriter);

        verify(csvWriter, times(1))
                .writeNext(new String[]{"{absence.period}: 01.01.2018 - 31.12.2018"});
        verify(csvWriter, times(1))
                .writeNext(new String[]{"{person.data.firstName}", "{person.data.lastName}", "", "{applications.statistics.allowed}", "{applications.statistics.waiting}", "{applications.statistics.left} (2018)", ""});
        verify(csvWriter, times(1))
                .writeNext(new String[]{"", "", "", "", "", "{duration.vacationDays}", "{duration.overtime}"});
        verify(csvWriter, times(1))
                .writeNext(new String[]{"personOneFirstName", "personOneLastName", "{applications.statistics.total}", "0", "0", "0", "0"});
    }

    @Test
    public void writeStatisticsForTwoPersonsFor2019() {
        FilterPeriod period = new FilterPeriod(
                java.util.Optional.ofNullable("01.01.2019"),
                java.util.Optional.ofNullable("31.12.2019"));

        List<ApplicationForLeaveStatistics> statistics = new ArrayList<>();
        Person personOne = mock(Person.class);
        when(personOne.getFirstName()).thenReturn("personOneFirstName");
        when(personOne.getLastName()).thenReturn("personOneLastName");

        Person personTwo = mock(Person.class);
        when(personTwo.getFirstName()).thenReturn("personTwoFirstName");
        when(personTwo.getLastName()).thenReturn("personTwoLastName");

        VacationTypeService vts = mock(VacationTypeService.class);
        when(vts.getVacationTypes()).thenReturn(new ArrayList());

        ApplicationForLeaveStatistics applicationForLeaveStatistics = mock(ApplicationForLeaveStatistics.class);
        statistics.add(new ApplicationForLeaveStatistics(personOne, vts));
        statistics.add(new ApplicationForLeaveStatistics(personTwo, vts));

        CSVWriter csvWriter = mock(CSVWriter.class);

        mockMessageSource("absence.period");

        mockMessageSource("person.data.firstName");
        mockMessageSource("person.data.lastName");
        mockMessageSource("applications.statistics.allowed");
        mockMessageSource("applications.statistics.waiting");
        mockMessageSource("applications.statistics.left");

        mockMessageSource("duration.vacationDays");
        mockMessageSource("duration.overtime");

        mockMessageSource("applications.statistics.total");

        sut.writeStatistics(period, statistics, csvWriter);

        verify(csvWriter, times(1))
                .writeNext(new String[]{"{absence.period}: 01.01.2019 - 31.12.2019"});
        verify(csvWriter, times(1))
                .writeNext(new String[]{"{person.data.firstName}", "{person.data.lastName}", "", "{applications.statistics.allowed}", "{applications.statistics.waiting}", "{applications.statistics.left} (2019)", ""});
        verify(csvWriter, times(1))
                .writeNext(new String[]{"", "", "", "", "", "{duration.vacationDays}", "{duration.overtime}"});
        verify(csvWriter, times(1))
                .writeNext(new String[]{"personOneFirstName", "personOneLastName", "{applications.statistics.total}", "0", "0", "0", "0"});
        verify(csvWriter, times(1))
                .writeNext(new String[]{"personTwoFirstName", "personTwoLastName", "{applications.statistics.total}", "0", "0", "0", "0"});
    }

    private void mockMessageSource(String key) {
        when(messageSource.getMessage(eq(key), any(), any()))
                .thenReturn(String.format("{%s}", key));
    }

    @Test
    public void getFileNameForComplete2018() {
        FilterPeriod period = new FilterPeriod(
                java.util.Optional.ofNullable("01.01.2018"),
                java.util.Optional.ofNullable("31.12.2018"));

        when(messageSource.getMessage("applications.statistics", new String[]{"Statistik"}, Locale.GERMAN))
                .thenReturn("test");

        String fileName = sut.getFileName(period);

        assertThat(fileName, is("test_01012018_31122018.csv"));
    }

    @Test
    public void getFileNameForComplete2019() {
        FilterPeriod period = new FilterPeriod(
                java.util.Optional.ofNullable("01.01.2019"),
                java.util.Optional.ofNullable("31.12.2019"));

        when(messageSource.getMessage(eq("applications.statistics"), any(), any()))
                .thenReturn("test");

        String fileName = sut.getFileName(period);

        assertThat(fileName, is("test_01012019_31122019.csv"));
    }
}