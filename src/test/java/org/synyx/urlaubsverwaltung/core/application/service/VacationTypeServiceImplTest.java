package org.synyx.urlaubsverwaltung.core.application.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.core.application.dao.VacationTypeDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VacationTypeServiceImplTest {

    @Mock
    private VacationTypeDAO vacationTypeDAO;

    @Test
    public void getVacationTypesFilteredBy() {

        VacationTypeServiceImpl sut = new VacationTypeServiceImpl(vacationTypeDAO);

        final VacationType holiday = new VacationType();
        holiday.setCategory(VacationCategory.HOLIDAY);

        final VacationType overtime = new VacationType();
        overtime.setCategory(VacationCategory.OVERTIME);

        final VacationType specialLeave = new VacationType();
        specialLeave.setCategory(VacationCategory.SPECIALLEAVE);

        when(vacationTypeDAO.findAll()).thenReturn(asList(holiday, overtime, specialLeave));

        final List<VacationType> noOvertimeType = sut.getVacationTypesFilteredBy(VacationCategory.OVERTIME);

        assertThat(noOvertimeType).hasSize(2).containsExactly(holiday, specialLeave);
    }
}
