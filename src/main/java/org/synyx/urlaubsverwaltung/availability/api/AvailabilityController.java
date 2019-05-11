package org.synyx.urlaubsverwaltung.availability.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;


@Api("Availabilities: Get all availabilities for a certain period")
@RestController("restApiAvailabilityController")
@RequestMapping("/api")
public class AvailabilityController {

    private final PersonService personService;
    private final AvailabilityService availabilityService;

    @Autowired
    AvailabilityController(AvailabilityService availabilityService, PersonService personService) {

        this.availabilityService = availabilityService;
        this.personService = personService;
    }

    @ApiOperation(
        value = "Get all availabilities for a certain period and person",
        notes =
            "Get all availabilities for a certain period and person. Maximum allowed period per request is one month."
    )
    @GetMapping("/availabilities")
    public AvailabilityList personsAvailabilities(
        @ApiParam(value = "start of interval to get availabilities from (inclusive)", defaultValue = RestApiDateFormat.EXAMPLE_FIRST_DAY_OF_YEAR)
        @RequestParam("from")
        String startDateString,
        @ApiParam(value = "end of interval to get availabilities from (inclusive)", defaultValue = RestApiDateFormat.EXAMPLE_LAST_DAY_OF_YEAR)
        @RequestParam("to")
        String endDateString,
        @ApiParam(value = "login name of the person")
        @RequestParam(value = "person")
        String personLoginName) {

        Optional<Person> optionalPerson = personService.getPersonByLogin(personLoginName);

        if (!optionalPerson.isPresent()) {
            throw new IllegalArgumentException("No person found for loginName = " + personLoginName);
        }

        LocalDate startDate;
        LocalDate endDate;
        try{
            startDate = LocalDate.parse(startDateString);
            endDate = LocalDate.parse(endDateString);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(exception.getMessage());
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startdate " + startDateString + " must not be after endDate "
                + endDateString);
        }

        boolean requestedDateRangeIsMoreThanOneMonth = startDate.minusDays(1).isBefore(endDate.minusMonths(1));

        if (requestedDateRangeIsMoreThanOneMonth) {
            throw new IllegalArgumentException("Requested date range to large. Maximum allowed range is one month");
        }

        return availabilityService.getPersonsAvailabilities(startDate, endDate, optionalPerson.get());
    }
}
