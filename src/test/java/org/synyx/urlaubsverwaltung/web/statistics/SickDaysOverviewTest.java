package org.synyx.urlaubsverwaltung.web.statistics;

import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SickDaysOverviewTest {

    private WorkDaysService calendarService;

    @Before
    public void setUp() {

        calendarService = mock(WorkDaysService.class);
    }


    @Test
    public void ensureGeneratesCorrectSickDaysOverview() {

        SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);
        sickNoteType.setMessageKey("Krankmeldung");

        SickNoteType sickNoteTypeChild = new SickNoteType();
        sickNoteTypeChild.setCategory(SickNoteCategory.SICK_NOTE_CHILD);
        sickNoteTypeChild.setMessageKey("Kind-Krankmeldung");

        SickNote sickNoteWithoutAUB = TestDataCreator.anySickNote();
        sickNoteWithoutAUB.setSickNoteType(sickNoteType);
        sickNoteWithoutAUB.setStatus(SickNoteStatus.ACTIVE);
        sickNoteWithoutAUB.setStartDate(new DateMidnight(2014, 10, 13));
        sickNoteWithoutAUB.setEndDate(new DateMidnight(2014, 10, 13));

        SickNote sickNoteWithAUB = TestDataCreator.anySickNote();
        sickNoteWithAUB.setSickNoteType(sickNoteType);
        sickNoteWithAUB.setStatus(SickNoteStatus.ACTIVE);
        sickNoteWithAUB.setStartDate(new DateMidnight(2014, 10, 14));
        sickNoteWithAUB.setEndDate(new DateMidnight(2014, 10, 14));
        sickNoteWithAUB.setAubStartDate(new DateMidnight(2014, 10, 14));
        sickNoteWithAUB.setAubEndDate(new DateMidnight(2014, 10, 14));

        SickNote childSickNoteWithoutAUB = TestDataCreator.anySickNote();
        childSickNoteWithoutAUB.setSickNoteType(sickNoteTypeChild);
        childSickNoteWithoutAUB.setStatus(SickNoteStatus.ACTIVE);
        childSickNoteWithoutAUB.setStartDate(new DateMidnight(2014, 10, 15));
        childSickNoteWithoutAUB.setEndDate(new DateMidnight(2014, 10, 15));

        SickNote childSickNoteWithAUB = TestDataCreator.anySickNote();
        childSickNoteWithAUB.setSickNoteType(sickNoteTypeChild);
        childSickNoteWithAUB.setStatus(SickNoteStatus.ACTIVE);
        childSickNoteWithAUB.setStartDate(new DateMidnight(2014, 10, 16));
        childSickNoteWithAUB.setEndDate(new DateMidnight(2014, 10, 16));
        childSickNoteWithAUB.setAubStartDate(new DateMidnight(2014, 10, 16));
        childSickNoteWithAUB.setAubEndDate(new DateMidnight(2014, 10, 16));

        SickNote inactiveSickNote = TestDataCreator.anySickNote();
        inactiveSickNote.setSickNoteType(sickNoteTypeChild);
        inactiveSickNote.setStatus(SickNoteStatus.CANCELLED);
        inactiveSickNote.setStartDate(new DateMidnight(2014, 10, 17));
        inactiveSickNote.setEndDate(new DateMidnight(2014, 10, 17));

        SickNote inactiveChildSickNote = TestDataCreator.anySickNote();
        inactiveChildSickNote.setSickNoteType(sickNoteTypeChild);
        inactiveChildSickNote.setStatus(SickNoteStatus.CANCELLED);
        inactiveChildSickNote.setStartDate(new DateMidnight(2014, 10, 18));
        inactiveChildSickNote.setEndDate(new DateMidnight(2014, 10, 18));

        List<SickNote> sickNotes = Arrays.asList(sickNoteWithoutAUB, sickNoteWithAUB, childSickNoteWithoutAUB,
            childSickNoteWithAUB, inactiveSickNote, inactiveChildSickNote);

        // just return 1 day for each sick note
        when(calendarService.getWorkDays(any(DayLength.class), any(DateMidnight.class),
            any(DateMidnight.class), any(Person.class)))
            .thenReturn(BigDecimal.ONE);

        SickDaysOverview sickDaysOverview = new SickDaysOverview(sickNotes, calendarService);

        SickDays sickDays = sickDaysOverview.getSickDays();
        Assert.assertNotNull("Should not be null", sickDays.getDays());
        Assert.assertEquals("Wrong number of sick days without AUB", new BigDecimal("2"),
            sickDays.getDays().get("TOTAL"));
        Assert.assertEquals("Wrong number of sick days with AUB", BigDecimal.ONE, sickDays.getDays().get("WITH_AUB"));

        SickDays childSickDays = sickDaysOverview.getChildSickDays();
        Assert.assertNotNull("Should not be null", childSickDays.getDays());
        Assert.assertEquals("Wrong number of child sick days without AUB", new BigDecimal("2"),
            childSickDays.getDays().get("TOTAL"));
        Assert.assertEquals("Wrong number of child sick days with AUB", BigDecimal.ONE,
            childSickDays.getDays().get("WITH_AUB"));
    }

}
