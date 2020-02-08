import $ from 'jquery';
import { getYear, setYear, startOfYear, subMonths, addMonths } from 'date-fns';
import format from "../../lib/date-fns/format";
import getUrlParam from '../get-url-param';
import '../../components/calendar';

if (window.yados && window.yados.timelineDepartmentId){

$(document).ready(function () {

  var personId = window.uv.personId;
  var webPrefix = window.uv.webPrefix;
  var apiPrefix = window.uv.apiPrefix;
  var timelineDepartmentId = window.yados.timelineDepartmentId;
  var displayYear = window.yados.displayYear;

  function initCalendar() {
      var year = getUrlParam("year");
      var date = new Date();

      if (year.length > 0 && year != getYear(date)) {
          date = startOfYear(setYear(date, year));
      }

      $('#month-selection span.labelText').text(format(date, 'MMMM'));

      var holidayService = Urlaubsverwaltung.HolidayService.create(webPrefix, apiPrefix, +personId, +timelineDepartmentId);

      var shownNumberOfMonths = 10;
      var startDate = subMonths(date, shownNumberOfMonths / 2);
      var endDate = addMonths(date, shownNumberOfMonths / 2);

      var yearOfStartDate = getYear(startDate);
      var yearOfEndDate = getYear(endDate);

      $.when(
          holidayService.fetchPublic(yearOfStartDate),
          holidayService.fetchPersonal(yearOfStartDate),
          holidayService.fetchSickDays(yearOfStartDate),

          holidayService.fetchPublic(yearOfEndDate),
          holidayService.fetchPersonal(yearOfEndDate),
          holidayService.fetchSickDays(yearOfEndDate)
      ).always(function () {
          Urlaubsverwaltung.Calendar.init(holidayService, date);
      });
  }

  initCalendar();

  var resizeTimer = null;

  $(window).on('resize', function () {

      if (resizeTimer !== null) {
          clearTimeout(resizeTimer);
      }

      resizeTimer = setTimeout(function () {
          Urlaubsverwaltung.Calendar.reRender();
          resizeTimer = null;
      }, 30)

  });



   $('#month-selection ul a').on('click', function(event){
        $('#month-selection span.labelText').text(this.text);
        Urlaubsverwaltung.Calendar.jumpToMonth(displayYear, $(this).data('month'));
        event.preventDefault();
   });

   $('#jumpToToday').on('click', function(){
        $('#month-selection span.labelText').text(format(new Date(), 'MMMM'));
        if (getYear(new Date()) == displayYear){
            Urlaubsverwaltung.Calendar.jumpToToday();
        } else {
            location.href = webPrefix + "/staff/" + personId + "/overview?timelineDepartment=" + timelineDepartmentId + "&year=#calendar-selector";
        }
   });


});

} else {



$(document).ready(function () {

  var personId = window.uv.personId;
  var webPrefix = window.uv.webPrefix;
  var apiPrefix = window.uv.apiPrefix;

  function initCalendar() {

    var year = getUrlParam("year");
    var date = new Date();

    if (year.length > 0 && year != getYear(date)) {
      date = startOfYear(setYear(date, year));
    }

    var holidayService = Urlaubsverwaltung.HolidayService.create(webPrefix, apiPrefix, +personId);

    var shownNumberOfMonths = 10;
    var startDate = subMonths(date, shownNumberOfMonths / 2);
    var endDate = addMonths(date, shownNumberOfMonths / 2);

    var yearOfStartDate = getYear(startDate);
    var yearOfEndDate = getYear(endDate);

    $.when(
      holidayService.fetchPublic(yearOfStartDate),
      holidayService.fetchPersonal(yearOfStartDate),
      holidayService.fetchSickDays(yearOfStartDate),

      holidayService.fetchPublic(yearOfEndDate),
      holidayService.fetchPersonal(yearOfEndDate),
      holidayService.fetchSickDays(yearOfEndDate)
    ).always(function () {
      Urlaubsverwaltung.Calendar.init(holidayService, date);
    });
  }

  initCalendar();

  var resizeTimer = null;

  $(window).on('resize', function () {

    if (resizeTimer !== null) {
      clearTimeout(resizeTimer);
    }

    resizeTimer = setTimeout(function () {
      Urlaubsverwaltung.Calendar.reRender();
      resizeTimer = null;
    }, 30)

  });

});
}
