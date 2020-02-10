import $ from 'jquery';
import { findWhere } from 'underscore';
import {
  addDays,
  addMonths,
  addWeeks,
  getYear,
  isBefore,
  isPast,
  isToday,
  isValid as isValidDate,
  isWeekend,
  isWithinInterval,
  getDay,
  getMonth,
  parseISO,
  startOfMonth,
  subMonths,
  subDays,
  setYear,
  setMonth,
  setDate
} from 'date-fns';
import format from '../../lib/date-fns/format'
import startOfWeek from '../../lib/date-fns/start-of-week'
import tooltip from '../tooltip';
import './calendar.css';


if (window.yados && window.yados.timelineDepartmentId){
  $(function() {

    var $datepicker = $('#datepicker');

    var numberOfWeeks = 10;

    var keyCodes = {
        escape: 27
    };

    var mouseButtons = {
        left   : 0,
        middle : 1,
        right  : 2
    };

    var CSS = {
        day                   : 'datepicker-day',
        daySelected           : 'datepicker-day-selected',
        dayToday              : 'datepicker-day-today',
        dayWeekend            : 'datepicker-day-weekend',
        dayPast               : 'datepicker-day-past',
        dayPublicHolidayFull              : 'datepicker-day-public-holiday-full',
        dayPublicHolidayMorning           : 'datepicker-day-public-holiday-morning',
        dayPublicHolidayNoon              : 'datepicker-day-public-holiday-noon',
        dayPersonalHolidayFull            : 'datepicker-day-personal-holiday-full datepicker-day-personal-holiday-{{category}}',
        dayPersonalHolidayFullApproved    : 'datepicker-day-personal-holiday-full-approved datepicker-day-personal-holiday-{{category}}',
        dayPersonalHolidayMorning         : 'datepicker-day-personal-holiday-morning datepicker-day-personal-holiday-{{category}}',
        dayPersonalHolidayMorningApproved : 'datepicker-day-personal-holiday-morning-approved datepicker-day-personal-holiday-{{category}}',
        dayPersonalHolidayNoon            : 'datepicker-day-personal-holiday-noon datepicker-day-personal-holiday-{{category}}',
        dayPersonalHolidayNoonApproved    : 'datepicker-day-personal-holiday-noon-approved datepicker-day-personal-holiday-{{category}}',
        daySickDayFull                    : 'datepicker-day-sick-note-full datepicker-day-sick-note-{{category}}',
        daySickDayMorning                 : 'datepicker-day-sick-note-morning datepicker-day-sick-note-{{category}}',
        daySickDayNoon                    : 'datepicker-day-sick-note-noon datepicker-day-sick-note-{{category}}',
        next                  : 'datepicker-next',
        previous              : 'datepicker-prev',
        week                  : 'datepicker-week',
        month                 : 'datepicker-month',
        mousedown             : 'mousedown'
    };

    var DATA = {
        startDate  : 'datepickerStart',
        date       : 'datepickerDate',
        selected   : 'datepickerSelected',
        selectFrom : 'datepickerSelectFrom',
        selectTo   : 'datepickerSelectTo',
        selectable : 'datepickerSelectable'
    };

    var Assertion = (function() {
            var holidayService;

            var assert = {
                isToday: function(date) {
                    return isToday(date);
                },
                isWeekend: function(date) {
                    return isWeekend(date);
                },
                isPast: function(date) {
                    /* NOTE: Today is not in the past! */
                    return !isToday(date) && isPast(date);
                },
                isHalfDayAbsence: function(date, personId) {
                  if (assert.isPersonalHolidayMorning(date, personId) || assert.isPersonalHolidayNoon(date, personId)) {
                    return true;
                  }
                  if (assert.isSickDayMorning(date, personId) || assert.isSickDayNoon(date, personId)) {
                    return true;
                  }
                  return assert.isPublicHolidayMorning(date, personId) || assert.isPublicHolidayNoon(date, personId);
                },
                isPublicHolidayFull: function(date, personId) {
                    return holidayService.isPublicHolidayFull(date, personId);
                },
                isPublicHolidayMorning: function(date, personId) {
                  return holidayService.isPublicHolidayMorning(date, personId);
                },
                isPublicHolidayNoon: function(date, personId) {
                  return holidayService.isPublicHolidayNoon(date, personId);
                },
                isPersonalHolidayFull: function(date, personId) {
                    return !isWeekend(date) && holidayService.isPersonalHolidayFull(date, personId);
                },
                isPersonalHolidayFullApproved: function(date, personId) {
                  return !isWeekend(date) && holidayService.isPersonalHolidayFullApproved(date, personId);
                },
                isPersonalHolidayMorning: function(date, personId) {
                  return !isWeekend(date) && holidayService.isPersonalHolidayMorning(date, personId);
                },
                isPersonalHolidayMorningApproved: function(date, personId) {
                  return !isWeekend(date) && holidayService.isPersonalHolidayMorningApproved(date, personId);
                },
                isPersonalHolidayNoon: function(date, personId) {
                  return !isWeekend(date) && holidayService.isPersonalHolidayNoon(date, personId);
                },
                isPersonalHolidayNoonApproved: function(date, personId) {
                  return !isWeekend(date) && holidayService.isPersonalHolidayNoonApproved(date, personId);
                },
                isSickDayFull: function(date, personId) {
                    return !isWeekend(date) && holidayService.isSickDayFull(date, personId);
                },
                isSickDayMorning: function(date, personId) {
                  return !isWeekend(date) && holidayService.isSickDayMorning(date, personId);
                },
                isSickDayNoon: function(date, personId) {
                  return !isWeekend(date) && holidayService.isSickDayNoon(date, personId);
                },
                title: function(date) {
                  return holidayService.getDescription(date);
                },
                absenceId: function(date, personId) {
                  return holidayService.getAbsenceId(date, personId);
                },
                absenceType: function(date, personId) {
                    return holidayService.getAbsenceType(date, personId);
                },
                status : function(date, personId) {
                    return holidayService.getStatus(date, personId);
                },
                absenceCategory: function(date, personId) {
                    return holidayService.getAbsenceCategory(date, personId);
                }
            };

            return {
                create: function(_holidayService) {
                    holidayService = _holidayService;
                    return assert;
                }
            };

        }());


    var HolidayService = (function() {

        var _CACHE  = {};

        var webPrefix;
        var apiPrefix;
        var departmentId;
        var viewerPersonId;


        function paramize(p) {
            var result = '?';
            for (var v in p) {
                if (p[v]) {
                    result += v + '=' + p[v] + '&';
                }
            }
            return result.replace(/[?&]$/, '');
        }

        /**
         *
         * @param {string} endpoint
         * @param {{}} params
         * @returns {$.ajax}
         */
        function fetch(endpoint, params) {

            var query = endpoint + paramize(params);

            return $.ajax({
                url: apiPrefix + query,
                dataType: 'json'
            });
        }

        function cacheAbsences(year) {
            return function(data) {
                _CACHE[year] = data.response;
            }

        }


        function isOfType(type, matcherAttributes) {
          return function (date, personId) {

            const year = getYear(date);
            const formattedDate = format(date, 'yyyy-MM-dd');

            var holiday = CACHE_findDate(type, year, formattedDate, personId);
            if (type === 'publicHoliday') {
                return holiday && holiday.dayLength < 1;
            } else {
                return findWhere([holiday], {...matcherAttributes});
            }
          };
        }

        function CACHE_findDate(type, year, formattedDate, personId){
            var c = _CACHE[year];
            if (!c) return null;
            if (type === 'publicHoliday') {
                var p = c.personPublicHolidays;
                p = p && p[personId]
                c = p && c.publicHolidays;
                c = c && c[p]
                return c && findWhere(c, {date: formattedDate});
            }
            if (type === 'holiday') {
                c = c && c.personAbsences;
                c = c && c[personId];
                return c && findWhere(c, {date: formattedDate, type: 'VACATION' })
            }
            if (type === 'sick') {
                c = c && c.personAbsences;
                c = c && c[personId];
                return c && findWhere(c, {date: formattedDate, type: 'SICK_NOTE' })
            }
        }

        const absencePeriod = Object.freeze({
          FULL: 'FULL',
          MORNING: 'MORNING',
          NOON: 'NOON',
        });

        const HolidayService = {

            isSickDayFull: isOfType('sick', { absencePeriodName: absencePeriod.FULL }),
            isSickDayMorning: isOfType('sick', { absencePeriodName: absencePeriod.MORNING }),
            isSickDayNoon: isOfType('sick', { absencePeriodName: absencePeriod.NOON }),

            isPersonalHolidayFull: isOfType('holiday', { absencePeriodName: absencePeriod.FULL, status: 'WAITING' }),
            isPersonalHolidayFullApproved: isOfType('holiday', { absencePeriodName: absencePeriod.FULL, status: 'ALLOWED' }),
            isPersonalHolidayMorning: isOfType('holiday', { absencePeriodName: absencePeriod.MORNING, status: 'WAITING' }),
            isPersonalHolidayMorningApproved: isOfType('holiday', { absencePeriodName: absencePeriod.MORNING, status: 'ALLOWED' }),
            isPersonalHolidayNoon: isOfType('holiday', { absencePeriodName: absencePeriod.NOON, status: 'WAITING' }),
            isPersonalHolidayNoonApproved: isOfType('holiday', { absencePeriodName: absencePeriod.NOON, status: 'ALLOWED' }),

            isPublicHolidayFull: isOfType('publicHoliday', { absencePeriodName: absencePeriod.FULL }),
            isPublicHolidayMorning: isOfType('publicHoliday', { absencePeriodName: absencePeriod.MORNING }),
            isPublicHolidayNoon: isOfType('publicHoliday', { absencePeriodName: absencePeriod.NOON }),

            getDescription: function (date, personId) {

              var year = getYear(date);
              var formattedDate = format(date, 'yyyy-MM-dd');

               var publicHoliday = CACHE_findDate('publicHoliday', year, formattedDate, personId)

               return publicHoliday ? publicHoliday.description : '';

            },

            getStatus: function (date, personId) {

              var year = getYear(date);
              var formattedDate = format(date, 'yyyy-MM-dd');

              var holiday = CACHE_findDate('holiday', year, formattedDate, personId);

              if(holiday) {
                return holiday.status;
              }


              return null;

            },


            getAbsenceId: function (date, personId) {

              var year = getYear(date);
              var formattedDate = format(date, 'yyyy-MM-dd');


                var holiday = CACHE_findDate('holiday', year, formattedDate, personId);

                if(holiday) {
                  return holiday.href;
                }


                  var sickDay = CACHE_findDate('sick', year, formattedDate, personId);

                  if(sickDay) {
                      return sickDay.href;
                  }


              return '-1';

            },

            getAbsenceType: function (date, personId) {

                var year = getYear(date);
                var formattedDate = format(date, 'yyyy-MM-dd');
                var holiday = CACHE_findDate('holiday', year, formattedDate, personId);

                if(holiday) {
                  return holiday.type;
                }

                 var sickDay = CACHE_findDate('sick', year, formattedDate, personId);

                  if(sickDay) {
                      return sickDay.type;
                  }

                return '';

            },

            getAbsenceCategory: function (date, personId) {

                var year = getYear(date);
                var formattedDate = format(date, 'yyyy-MM-dd');
                var holiday = CACHE_findDate('holiday', year, formattedDate, personId);

                if(holiday) {
                  return holiday.category;
                }

                var sickDay = CACHE_findDate('sick', year, formattedDate, personId);

                if(sickDay) {
                    return sickDay.category;
                }

                return '';
            },


            /**
             *
             * @param {Date} from
             * @param {Date} [to]
             */
            bookHoliday: function(from, to) {

                var params = {
                    personId: viewerPersonId,
                    from :      format(from, 'yyyy-MM-dd'),
                    to   : to ? format(to, 'yyyy-MM-dd') : undefined
                };

                document.location.href = webPrefix + '/application/new' + paramize( params );
            },

            navigateToApplicationForLeave: function(applicationId) {

              document.location.href = webPrefix + '/application/' + applicationId;

            },

            navigateToSickNote: function(sickNoteId) {

                document.location.href = webPrefix + '/sicknote/' + sickNoteId;

            },

            fetchAbsences: function(year) {

                var c = _CACHE[year];

                if (c) {
                    return $.Deferred().resolve( c );
                } else {
                    return fetch('/absences', {department: departmentId, year: year}).done( cacheAbsences(year) );
                }
            },

            fetchPersonal: function(year){
                // keep the stub for API compatibility with calendar.js
                return this.fetchAbsences(year)
            },

            fetchSickDays: function(){
                // keep the stub for API compatibility with calendar.js
            },

            fetchPublic : function(){
                // keep the stub for API compatibility with calendar.js
            },

        };

        return {
            create: function(_webPrefix, _apiPrefix, _personId, _departmentId) {
                webPrefix = _webPrefix;
                apiPrefix = _apiPrefix;
                viewerPersonId = _personId;
                departmentId  = _departmentId;
                HolidayService.viewerPersonId = _personId;
                return HolidayService;
            }
        };

    }());


    var View = (function() {

        var assert;

        var TMPL = {

            container: '{{previousButton}}<div class="datepicker-months-container" style="height: {{height}}px;">{{weeks}}<div class="datepicker-person">{{names}}</div></div>{{nextButton}}',

            button: '<button class="{{css}}">{{text}}</button>',

            name: '<div><span>{{name}}</span></div>',

            week: '<div class="datepicker-week" data-datepicker-start="{{startDate}}">{{title}}<table class="datepicker-table"><thead>{{weekdays}}</thead><tbody>{{persons}}</tbody></table></div>',

            title: '<h3>{{title}}</h3>',

            weekdays: '<tr><th>{{' + [0,1,2,3,4,5,6].join('}}</th><th>{{') + '}}</th></tr>',

            personWeek: '<tr data-datepicker-person="{{id}}"><td>{{' + [1,2,3,4,5,6,0].join('}}</td><td>{{') + '}}</td></tr>',

            day: '<span class="datepicker-day {{css}}" data-title="{{title}}" data-datepicker-absence-id={{absenceId}} data-datepicker-absence-type="{{absenceType}}" data-datepicker-date="{{date}}" data-datepicker-selectable="{{selectable}}">{{day}}</span>'
        };

        function render(tmpl, data) {
            return tmpl.replace(/{{(\w+)}}/g, function(_, type) {

                if (typeof data === 'function') {
                    return data.apply(this, arguments);
                }

                var value = data[type];
                return typeof value === 'function' ? value() : value;
            });
        }

        function renderCalendar(date, personIds) {

            var weeksToShow = numberOfWeeks;

            var timelinePersons = window.yados.timelinePersons;

            return render(TMPL.container, {

                height:  90 + personIds.length * 40  ,

                previousButton   : renderButton ( CSS.previous, '<i class="fa fa-chevron-left"></i>'),
                nextButton   : renderButton ( CSS.next, '<i class="fa fa-chevron-right"></i>'),

                weeks: function() {
                    var html = '';
                    var d = subDays(date, 28);
                    d = startOfWeek(d);
                    while(weeksToShow--) {
                        html += renderWeekBox(d, personIds)
                        d = addDays(d, 7);
                    }
                    return html;
                },

                names: function(){
                    var html = '';
                    for (const personId of personIds) {
                        html += render(
                            TMPL.name,
                            {name : timelinePersons[personId]}
                        )
                    }
                    return html;
                }
            });
        }

        function renderButton(css, text) {
            return render(TMPL.button, {
                css : css,
                text: text
            });
        }

        function renderWeekBox(date, personIds){
            var p = "";
            for (const personId of personIds) {
                p += renderWeek(date, personId)
            }
            return render(TMPL.week, {
                startDate: format(date, 'yyyy-MM-dd'),
                weekdays: renderWeekdaysHeader(date),
                persons:  p,
                title: renderWeekTitle(date)
            });
        }


        function renderWeek(date, personId) {

            var d = date;

            return render(TMPL.personWeek, function(_, dayIdx) {

                if (dayIdx === 'id') return personId;

                var html = '&nbsp;';

                if (Number (dayIdx) === getDay(d) ) {
                    html = renderDay(d, personId);
                    d = addDays(d, 1);
                }

                return html;
            });
        }

         function renderWeekTitle(date) {
            var endMonth = addDays(date, 6);
            if (getMonth(endMonth) === getMonth(date)){
                return render(TMPL.title, {
                     title: format(endMonth, 'MMMM yyyy')
                });
            }
            return render(TMPL.title, {
                title: format(date, 'MMMM') + ' / ' +format(endMonth, 'MMMM yyyy')
            });

         }

        function renderWeekdaysHeader(date) {
            var d = startOfWeek(date);

            return render(TMPL.weekdays, {
                0: format(d, 'dd'),
                1: format(addDays(d, 1), 'dd'),
                2: format(addDays(d, 2), 'dd'),
                3: format(addDays(d, 3), 'dd'),
                4: format(addDays(d, 4), 'dd'),
                5: format(addDays(d, 5), 'dd'),
                6: format(addDays(d, 6), 'dd'),
            });
        }


        function renderDay(date, personId) {

            function classes() {
                var category = assert.absenceCategory(date, personId)
                return [
                    assert.isToday                          (date) ? CSS.dayToday                          : '',
                    assert.isWeekend                        (date) ? CSS.dayWeekend                        : '',
                    assert.isPast                           (date) ? CSS.dayPast                           : '',
                    assert.isPublicHolidayFull              (date) ? CSS.dayPublicHolidayFull              : '',
                    assert.isPublicHolidayMorning           (date) ? CSS.dayPublicHolidayMorning           : '',
                    assert.isPublicHolidayNoon              (date) ? CSS.dayPublicHolidayNoon              : '',
                    assert.isPersonalHolidayFull            (date, personId) ? CSS.dayPersonalHolidayFull            : '',
                    assert.isPersonalHolidayFullApproved    (date, personId) ? CSS.dayPersonalHolidayFullApproved    : '',
                    assert.isPersonalHolidayMorning         (date, personId) ? CSS.dayPersonalHolidayMorning         : '',
                    assert.isPersonalHolidayMorningApproved (date, personId) ? CSS.dayPersonalHolidayMorningApproved : '',
                    assert.isPersonalHolidayNoon            (date, personId) ? CSS.dayPersonalHolidayNoon            : '',
                    assert.isPersonalHolidayNoonApproved    (date, personId) ? CSS.dayPersonalHolidayNoonApproved    : '',
                    assert.isSickDayFull                    (date, personId) ? CSS.daySickDayFull                    : '',
                    assert.isSickDayMorning                 (date, personId) ? CSS.daySickDayMorning                 : '',
                    assert.isSickDayNoon                    (date, personId) ? CSS.daySickDayNoon                    : '',
                ].filter(Boolean).join(' ').replace("{{category}}", category);

            }



            function isSelectable() {

                // NOTE: Order is important here!

                var isPersonalHoliday = assert.isPersonalHolidayFull(date, personId);
                var isSickDay = assert.isSickDayFull(date, personId);

                if(isPersonalHoliday || isSickDay) {
                  return true;
                }

                var isPast = assert.isPast(date);
                var isWeekend = assert.isWeekend(date);

                if(isPast || isWeekend) {
                    return false;
                }

                return assert.isHalfDayAbsence(date, personId) || !assert.isPublicHolidayFull(date, personId);
            }

            return render(TMPL.day, {
                date: format(date, 'yyyy-MM-dd'),
                day : format(date, 'dd'),
                css : classes(),
                selectable: isSelectable(),
                title: assert.title(date, personId),
                absenceId: assert.absenceId(date, personId),
                absenceType: assert.absenceType(date, personId)
            });
        }

        var View = {

            display: function(date, personIds) {
                View.personIds = personIds;
                $datepicker.html( renderCalendar(date, personIds)).addClass('unselectable');
                tooltip();
            },

            displayNext: function() {

                var elements = $datepicker.find('.' + CSS.week).get();
                var length   = elements.length;

                $(elements[0]).remove();

                var $lastWeek = $(elements[length - 1]);
                var start = parseISO($lastWeek.data(DATA.startDate));

                var $nextWeek = $(renderWeekBox( addDays(start, 7), View.personIds));

                $lastWeek.after($nextWeek);
                tooltip();
            },

            displayPrevious: function() {

                var elements = $datepicker.find('.' + CSS.week).get();
                var length = elements.length;

                $(elements[length - 1]).remove();

                var $firstWeek = $(elements[0]);
                var start = parseISO($firstWeek.data(DATA.startDate));

                var $previousWeek = $(renderWeekBox( subDays(start, 7), View.personIds));

                $firstWeek.before($previousWeek);
                tooltip();
            }
        };

        return {
            create: function(_assert) {
                assert = _assert;
                return View;
            }
        };
    }());


    var Controller = (function() {

        var view;
        var holidayService;

        var datepickerHandlers = {

            mousedown: function(event) {

                if (event.button != mouseButtons.left) {
                    return;
                }

                $(document.body).addClass(CSS.mousedown);

                var dateThis = getDateFromElement(this);

                const start = selectionFrom();
                const end = selectionTo();

                if ( !isValidDate(start) || !isValidDate(end) || !isWithinInterval(dateThis, { start, end }) ) {

                    clearSelection();

                    $datepicker.data(DATA.selected, dateThis);

                    selectionFrom( dateThis );
                    selectionTo  ( dateThis );
                }
            },

            mouseup: function() {
                $(document.body).removeClass(CSS.mousedown);
            },

            mouseover: function() {
                if ( $(document.body).hasClass(CSS.mousedown) ) {

                    var dateThis     = getDateFromElement(this);
                    var dateSelected = $datepicker.data(DATA.selected);

                    var isThisBefore = isBefore(dateThis, dateSelected);

                    selectionFrom( isThisBefore ? dateThis     : dateSelected );
                    selectionTo  ( isThisBefore ? dateSelected : dateThis     );
                }
            },

            click: function() {

                var dateFrom = selectionFrom();
                var dateTo   = selectionTo  ();

                var dateThis = getDateFromElement(this);

                var isSelectable = $(this).attr("data-datepicker-selectable");
                var personId = +($(this).closest('tr').attr('data-datepicker-person'))

                var absenceId = $(this).attr('data-datepicker-absence-id');
                var absenceType = $(this).attr('data-datepicker-absence-type');
                const viewerPersonId = holidayService.viewerPersonId;

                if(personId === viewerPersonId && isSelectable === "true" && absenceType === "VACATION" && absenceId !== "-1") {
                    holidayService.navigateToApplicationForLeave(absenceId);
                } else if(personId === viewerPersonId && isSelectable === "true" && absenceType === "SICK_NOTE" && absenceId !== "-1") {
                    holidayService.navigateToSickNote(absenceId);
                } else if(isSelectable === "true" && isValidDate(dateFrom) && isValidDate(dateTo) && isWithinInterval(dateThis, { start: dateFrom, end: dateTo })) {
                    holidayService.bookHoliday(dateFrom, dateTo);
                }

            },

            clickNext: function() {

                // last week of calendar
                var $week = $( $datepicker.find('.' + CSS.week)[numberOfWeeks-1] );

                // to load data for the new (invisible) next week
                var date = addDays(parseISO($week.data(DATA.startDate))
                    , 13); // go to the last day of the week, it may be in a new year

                $.when(
                    holidayService.fetchAbsences   ( getYear(date) )
                ).then(view.displayNext);
            },

            clickPrevious: function() {

                // first week of calendar
                var $week = $( $datepicker.find('.' + CSS.week)[0] );

                // to load data for the new (invisible) prev week
                var date = subDays(parseISO($week.data(DATA.startDate))
                                    , 7);

                $.when(
                    holidayService.fetchAbsences   ( getYear(date) )
                ).then(view.displayPrevious);
            }
        };

        function getDateFromElement(element) {
            return parseISO($(element).data(DATA.date));
        }

        function selectionFrom(date) {
            if (!date) {
                return parseISO($datepicker.data(DATA.selectFrom));
            }

            $datepicker.data(DATA.selectFrom, format(date,'yyyy-MM-dd'));
            refreshDatepicker();
        }

        function selectionTo(date) {
            if (!date) {
                return parseISO($datepicker.data(DATA.selectTo));
            }

            $datepicker.data(DATA.selectTo, format(date, 'yyyy-MM-dd'));
            refreshDatepicker();
        }

        function clearSelection() {
            $datepicker.removeData(DATA.selectFrom);
            $datepicker.removeData(DATA.selectTo);
            refreshDatepicker();
        }


        function refreshDatepicker() {

            var start = selectionFrom();
            var end   = selectionTo();

            const startIsValid = isValidDate(start);
            const endIsValid = isValidDate(end);

            $('.' + CSS.day).each(function() {
                if (!startIsValid || !endIsValid) {
                  select(this, false);
                } else {
                  const date = parseISO($(this).data(DATA.date));
                  select(this, isWithinInterval(date, { start, end }));
                }
            });
        }

        function select(element, selection) {

            var $element = $(element);

            if ( ! $element.data(DATA.selectable) ) {
                return;
            }

            if (selection) {
                $element.addClass(CSS.daySelected);
            }
            else {
                $element.removeClass(CSS.daySelected);
            }
        }

        var Controller = {
            bind: function() {

                $datepicker.on('mousedown', '.' + CSS.day , datepickerHandlers.mousedown);
                $datepicker.on('mouseover', '.' + CSS.day , datepickerHandlers.mouseover);
                $datepicker.on('click'    , '.' + CSS.day , datepickerHandlers.click    );

                $datepicker.on('click'    , '.' + CSS.previous, datepickerHandlers.clickPrevious);
                $datepicker.on('click'    , '.' + CSS.next, datepickerHandlers.clickNext);


                $(document.body).on('keyup', function(event) {
                    if (event.keyCode === keyCodes.escape) {
                        clearSelection();
                    }
                });

                $(document.body).on('mouseup', function() {
                    $(document.body).removeClass(CSS.mousedown);
                });
            },

            jumpTo: function(date, personIds){
                 $.when( holidayService.fetchAbsences   ( getYear(date) ))
                 .then(function() {
                    // if we are in December, also fetch the next year
                    return holidayService.fetchAbsences ( getYear(addDays(date, 30)) )
                 })
                 .then(function(){ view.display(date, personIds); });
            }
        };

        return {
            create: function(_holidayService, _view) {
                holidayService = _holidayService;
                view = _view;
                return Controller;
            }
        };
    }());


    var Calendar = (function () {

        var view;
        var date;
        var personIds;
        var c;


        return {
            init: function(holidayService, referenceDate) {

                personIds = window.yados.timelinePersons.byName

                date = referenceDate;

                var a = Assertion.create (holidayService);
                view = View.create(a);
                c = Controller.create(holidayService, view);

                view.display(date, personIds);
                c.bind();
            },

            reRender: function() {
                view.display(date, personIds);
            },

            jumpToMonth: function(year, month){
               date = setYear(date, year);
               date = setMonth(date, month-1);
               date = setDate(date, 8);

               c.jumpTo(date, personIds);
            },

            jumpToToday: function(){
               c.jumpTo(date = new Date(), personIds);
            }

        }
    })();

    /**
     * @export
     */
    window.Urlaubsverwaltung = {
        Calendar      : Calendar,
        HolidayService: HolidayService
    };

});
}

else {



$(function() {

    var $datepicker = $('#datepicker');

    var numberOfMonths = 10;

    var keyCodes = {
        escape: 27
    };

    var mouseButtons = {
        left   : 0,
        middle : 1,
        right  : 2
    };

    var CSS = {
        day                               : 'datepicker-day',
        daySelected                       : 'datepicker-day-selected',
        dayToday                          : 'datepicker-day-today',
        dayWeekend                        : 'datepicker-day-weekend',
        dayPast                           : 'datepicker-day-past',
        dayPublicHolidayFull              : 'datepicker-day-public-holiday-full',
        dayPublicHolidayMorning           : 'datepicker-day-public-holiday-morning',
        dayPublicHolidayNoon              : 'datepicker-day-public-holiday-noon',
        dayPersonalHolidayFull            : 'datepicker-day-personal-holiday-full datepicker-day-personal-holiday-{{category}}',
        dayPersonalHolidayFullApproved    : 'datepicker-day-personal-holiday-full-approved datepicker-day-personal-holiday-{{category}}',
        dayPersonalHolidayMorning         : 'datepicker-day-personal-holiday-morning datepicker-day-personal-holiday-{{category}}',
        dayPersonalHolidayMorningApproved : 'datepicker-day-personal-holiday-morning-approved datepicker-day-personal-holiday-{{category}}',
        dayPersonalHolidayNoon            : 'datepicker-day-personal-holiday-noon datepicker-day-personal-holiday-{{category}}',
        dayPersonalHolidayNoonApproved    : 'datepicker-day-personal-holiday-noon-approved datepicker-day-personal-holiday-{{category}}',
        daySickDayFull                    : 'datepicker-day-sick-note-full datepicker-day-sick-note-{{category}}',
        daySickDayMorning                 : 'datepicker-day-sick-note-morning datepicker-day-sick-note-{{category}}',
        daySickDayNoon                    : 'datepicker-day-sick-note-noon datepicker-day-sick-note-{{category}}',
        next                              : 'datepicker-next',
        previous                          : 'datepicker-prev',
        month                             : 'datepicker-month',
        mousedown                         : 'mousedown'
    };

    var DATA = {
        date       : 'datepickerDate',
        month      : 'datepickerMonth',
        year       : 'datepickerYear',
        selected   : 'datepickerSelected',
        selectFrom : 'datepickerSelectFrom',
        selectTo   : 'datepickerSelectTo',
        selectable : 'datepickerSelectable'
    };

    var Assertion = (function() {
        var holidayService;

        var assert = {
            isToday: function(date) {
                return isToday(date);
            },
            isWeekend: function(date) {
                return isWeekend(date);
            },
            isPast: function(date) {
                /* NOTE: Today is not in the past! */
                return !isToday(date) && isPast(date);
            },
            isHalfDayAbsence: function(date) {
              if (assert.isPersonalHolidayMorning(date) || assert.isPersonalHolidayNoon(date)) {
                return true;
              }
              if (assert.isSickDayMorning(date) || assert.isSickDayNoon(date)) {
                return true;
              }
              return assert.isPublicHolidayMorning(date) || assert.isPublicHolidayNoon(date);
            },
            isPublicHolidayFull: function(date) {
                return holidayService.isPublicHolidayFull(date);
            },
            isPublicHolidayMorning: function(date) {
              return holidayService.isPublicHolidayMorning(date);
            },
            isPublicHolidayNoon: function(date) {
              return holidayService.isPublicHolidayNoon(date);
            },
            isPersonalHolidayFull: function(date) {
                return !isWeekend(date) && holidayService.isPersonalHolidayFull(date);
            },
            isPersonalHolidayFullApproved: function(date) {
              return !isWeekend(date) && holidayService.isPersonalHolidayFullApproved(date);
            },
            isPersonalHolidayMorning: function(date) {
              return !isWeekend(date) && holidayService.isPersonalHolidayMorning(date);
            },
            isPersonalHolidayMorningApproved: function(date) {
              return !isWeekend(date) && holidayService.isPersonalHolidayMorningApproved(date);
            },
            isPersonalHolidayNoon: function(date) {
              return !isWeekend(date) && holidayService.isPersonalHolidayNoon(date);
            },
            isPersonalHolidayNoonApproved: function(date) {
              return !isWeekend(date) && holidayService.isPersonalHolidayNoonApproved(date);
            },
            isSickDayFull: function(date) {
                return !isWeekend(date) && holidayService.isSickDayFull(date);
            },
            isSickDayMorning: function(date) {
              return !isWeekend(date) && holidayService.isSickDayMorning(date);
            },
            isSickDayNoon: function(date) {
              return !isWeekend(date) && holidayService.isSickDayNoon(date);
            },
            title: function(date) {
              return holidayService.getDescription(date);
            },
            absenceId: function(date) {
              return holidayService.getAbsenceId(date);
            },
            absenceType: function(date) {
                return holidayService.getAbsenceType(date);
            },
            status : function(date) {
                return holidayService.getStatus(date);
            },
            absenceCategory: function(date) {
                return holidayService.getAbsenceCategory(date);
            }
        };

        return {
            create: function(_holidayService) {
                holidayService = _holidayService;
                return assert;
            }
        };

    }());


    var HolidayService = (function() {

        var _CACHE  = {};

        var webPrefix;
        var apiPrefix;
        var personId;

        function paramize(p) {
            var result = '?';
            for (var v in p) {
                if (p[v]) {
                    result += v + '=' + p[v] + '&';
                }
            }
            return result.replace(/[?&]$/, '');
        }

        /**
         *
         * @param {string} endpoint
         * @param {{}} params
         * @returns {$.ajax}
         */
        function fetch(endpoint, params) {

            var query = endpoint + paramize(params);

            return $.ajax({
                url: apiPrefix + query,
                dataType: 'json'
            });
        }

        function cacheAbsences(type, year) {
            var c = _CACHE[type] = _CACHE[type] || {};

            return function(data) {

                var absences = data.response.absences;

                if(absences.length > 0) {
                    $.each(absences, function(idx, absence) {
                        c[year] = c[year] || [];
                        c[year].push(absence);
                    });
                } else {
                    c[year] = [];
                }


            }

        }

        function cachePublicHoliday(year) {
            var c = _CACHE['publicHoliday'] = _CACHE['publicHoliday'] || {};

            return function(data) {

                var publicHolidays = data.response.publicHolidays;

                if(publicHolidays.length > 0) {
                    $.each(publicHolidays, function(idx, publicHoliday) {
                        c[year] = c[year] || [];
                        c[year].push(publicHoliday);
                    });
                } else {
                    c[year] = c[year] || [];
                }

            }
        }

        function isOfType(type, matcherAttributes) {
          return function (date) {
            const year = getYear(date);
            const formattedDate = format(date, 'yyyy-MM-dd');

            if (!_CACHE[type]) {
                return false;
            }

            if(_CACHE[type][year]) {
              const absence = findWhere(_CACHE[type][year], {...matcherAttributes, date: formattedDate});
              return Boolean(absence);
            }

            return false;
          };
        }

        const absencePeriod = Object.freeze({
          FULL: 'FULL',
          MORNING: 'MORNING',
          NOON: 'NOON',
        });

        const HolidayService = {

            isSickDayFull: isOfType('sick', { absencePeriodName: absencePeriod.FULL }),
            isSickDayMorning: isOfType('sick', { absencePeriodName: absencePeriod.MORNING }),
            isSickDayNoon: isOfType('sick', { absencePeriodName: absencePeriod.NOON }),

            isPersonalHolidayFull: isOfType('holiday', { absencePeriodName: absencePeriod.FULL, status: 'WAITING' }),
            isPersonalHolidayFullApproved: isOfType('holiday', { absencePeriodName: absencePeriod.FULL, status: 'ALLOWED' }),
            isPersonalHolidayMorning: isOfType('holiday', { absencePeriodName: absencePeriod.MORNING, status: 'WAITING' }),
            isPersonalHolidayMorningApproved: isOfType('holiday', { absencePeriodName: absencePeriod.MORNING, status: 'ALLOWED' }),
            isPersonalHolidayNoon: isOfType('holiday', { absencePeriodName: absencePeriod.NOON, status: 'WAITING' }),
            isPersonalHolidayNoonApproved: isOfType('holiday', { absencePeriodName: absencePeriod.NOON, status: 'ALLOWED' }),

            isPublicHolidayFull: isOfType('publicHoliday', { absencePeriodName: absencePeriod.FULL }),
            isPublicHolidayMorning: isOfType('publicHoliday', { absencePeriodName: absencePeriod.MORNING }),
            isPublicHolidayNoon: isOfType('publicHoliday', { absencePeriodName: absencePeriod.NOON }),

            getDescription: function (date) {
              var year = getYear(date);
              var formattedDate = format(date, 'yyyy-MM-dd');

              if (!_CACHE['publicHoliday']) {
                  return '';
              }

              if(_CACHE['publicHoliday'][year]) {

                var publicHoliday = findWhere(_CACHE['publicHoliday'][year], {date: formattedDate});

                if(publicHoliday) {
                  return publicHoliday.description;
                }

              }

              return '';

            },

            getStatus: function (date) {
              var year = getYear(date);
              var formattedDate = format(date, 'yyyy-MM-dd');

              if (!_CACHE['holiday']) {
                  return null;
              }

              if(_CACHE['holiday'][year]) {

                var holiday = findWhere(_CACHE['holiday'][year], {date: formattedDate});

                if(holiday) {
                  return holiday.status;
                }

              }

              return null;

            },

            getAbsenceId: function (date) {
              var year = getYear(date);
              var formattedDate = format(date, 'yyyy-MM-dd');

              if (!_CACHE['holiday']) {
                  return '-1';
              }

              if(_CACHE['holiday'][year]) {

                var holiday = findWhere(_CACHE['holiday'][year], {date: formattedDate});

                if(holiday) {
                  return holiday.href;
                }

              }

              if(_CACHE['sick'][year]) {

                  var sickDay = findWhere(_CACHE['sick'][year], {date: formattedDate});

                  if(sickDay) {
                      return sickDay.href;
                  }

              }

              return '-1';

            },

            getAbsenceType: function (date) {
                var year = getYear(date);
                var formattedDate = format(date, 'yyyy-MM-dd');

                if (!_CACHE['holiday']) {
                    return '';
                }

                if(_CACHE['holiday'][year]) {

                    var holiday = findWhere(_CACHE['holiday'][year], {date: formattedDate});

                    if(holiday) {
                        return holiday.type;
                    }

                }

                if(_CACHE['sick'][year]) {

                    var sickDay = findWhere(_CACHE['sick'][year], {date: formattedDate});

                    if(sickDay) {
                        return sickDay.type;
                    }

                }

                return '';

            },

            getAbsenceCategory: function (date) {

                var year = getYear(date);
                var formattedDate = format(date, 'yyyy-MM-dd');

                if (!_CACHE['holiday']) {
                    return '';
                }

                if(_CACHE['holiday'][year]) {

                    var holiday = findWhere(_CACHE['holiday'][year], {date: formattedDate});

                    if(holiday) {
                        return holiday.category;
                    }

                }

                if(_CACHE['sick'][year]) {

                    var sickDay = findWhere(_CACHE['sick'][year], {date: formattedDate});

                    if(sickDay) {
                        return sickDay.category;
                    }

                }

                return '';

            },

            /**
             *
             * @param {Date} from
             * @param {Date} [to]
             */
            bookHoliday: function(from, to) {
                var params = {
                    personId: personId,
                    from: format(from, 'yyyy-MM-dd'),
                    to: to ? format(to, 'yyyy-MM-dd') : undefined
                };

                document.location.href = webPrefix + '/application/new' + paramize( params );
            },

            navigateToApplicationForLeave: function(applicationId) {

              document.location.href = webPrefix + '/application/' + applicationId;

            },

            navigateToSickNote: function(sickNoteId) {

                document.location.href = webPrefix + '/sicknote/' + sickNoteId;

            },

            /**
             *
             * @param {number} year
             * @returns {$.ajax}
             */
            fetchPublic: function(year) {

                var deferred = $.Deferred();

                _CACHE['publicHoliday'] = _CACHE['publicHoliday'] || {};

                if (_CACHE['publicHoliday'][year]) {
                    return deferred.resolve( _CACHE[year] );
                } else {
                    return fetch('/holidays', {year: year, person: personId}).done( cachePublicHoliday(year) );
                }
            },

            /**
             *
             * @param {number} personId
             * @param {number} year
             * @param {number} [month]
             * @returns {$.ajax}
             */
            fetchPersonal: function(year) {
                var deferred = $.Deferred();

                _CACHE['holiday'] = _CACHE['holiday'] || {};

                if (_CACHE['holiday'][year]) {
                    return deferred.resolve( _CACHE[year] );
                } else {
                    return fetch('/absences', {person: personId, year: year, type: 'VACATION'}).done( cacheAbsences('holiday', year) );
                }
            },

            fetchSickDays: function(year) {
                var deferred = $.Deferred();

                _CACHE['sick'] = _CACHE['sick'] || {};

                if (_CACHE['sick'][year]) {
                    return deferred.resolve( _CACHE[year] );
                } else {
                    return fetch('/absences', {person: personId, year: year, type: 'SICK_NOTE'}).done( cacheAbsences('sick', year) );
                }
            }
        };

        return {
            create: function(_webPrefix, _apiPrefix, _personId) {
                webPrefix = _webPrefix;
                apiPrefix = _apiPrefix;
                personId  = _personId;
                return HolidayService;
            }
        };

    }());


    var View = (function() {

        var assert;

        var TMPL = {

            container: '{{previousButton}}<div class="datepicker-months-container">{{months}}</div>{{nextButton}}',

            button: '<button class="{{css}}">{{text}}</button>',

            month: '<div class="datepicker-month {{css}}" data-datepicker-month="{{month}}" data-datepicker-year="{{year}}">{{title}}<table class="datepicker-table"><thead>{{weekdays}}</thead><tbody>{{weeks}}</tbody></table></div>',

            title: '<h3>{{title}}</h3>',

            // <tr><th>{{0}}</th>......<th>{{6}}</th></tr>
            weekdays: '<tr><th>{{' + [0,1,2,3,4,5,6].join('}}</th><th>{{') + '}}</th></tr>',

            // <tr><td>{{0}}</td>......<td>{{6}}</td></tr>
            week: '<tr><td>{{' + [0,1,2,3,4,5,6].join('}}</td><td>{{') + '}}</td></tr>',

            day: '<span class="datepicker-day {{css}}" data-title="{{title}}" data-datepicker-absence-id={{absenceId}} data-datepicker-absence-type="{{absenceType}}" data-datepicker-date="{{date}}" data-datepicker-selectable="{{selectable}}">{{day}}</span>'
        };

        function render(tmpl, data) {
            return tmpl.replace(/{{(\w+)}}/g, function(_, type) {

                if (typeof data === 'function') {
                    return data.apply(this, arguments);
                }

                var value = data[type];
                return typeof value === 'function' ? value() : value;
            });
        }

        function renderCalendar(date) {

            var monthsToShow = numberOfMonths;

            return render(TMPL.container, {

                previousButton: renderButton ( CSS.previous, '<i class="fa fa-chevron-left" aria-hidden="true"></i>'),
                nextButton: renderButton ( CSS.next, '<i class="fa fa-chevron-right" aria-hidden="true"></i>'),

                months: function() {
                    var html = '';
                    var d = subMonths(date, 4);
                    while(monthsToShow--) {
                        html += renderMonth(d);
                        d = addMonths(d, 1);
                    }
                    return html;
                }
            });
        }

        function renderButton(css, text) {
            return render(TMPL.button, {
                css : css,
                text: text
            });
        }

        function renderMonth(date, cssClasses) {

            var m = getMonth(date);
            var d = startOfMonth(date);

            return render(TMPL.month, {

                css     : cssClasses || '',
                month   : getMonth(d),
                year    : getYear(d),
                title   : renderMonthTitle(d),
                weekdays: renderWeekdaysHeader(d),

                weeks: function() {
                    var html = '';
                    while(getMonth(d) === m) {
                        html += renderWeek(d);
                        d = addWeeks(d, 1);
                        d = startOfWeek(d);
                    }
                    return html;
                }
            });
        }

        function renderMonthTitle(date) {
            return render(TMPL.title, {
                title: format(date, 'MMMM yyyy')
            });
        }

        function renderWeekdaysHeader(date) {
            var d = startOfWeek(date);

            return render(TMPL.weekdays, {
                0: format(d, 'EEEEEE'),
                1: format(addDays(d, 1), 'EEEEEE'),
                2: format(addDays(d, 2), 'EEEEEE'),
                3: format(addDays(d, 3), 'EEEEEE'),
                4: format(addDays(d, 4), 'EEEEEE'),
                5: format(addDays(d, 5), 'EEEEEE'),
                6: format(addDays(d, 6), 'EEEEEE'),
            });
        }

        function renderWeek(date) {
            var d = date;
            var m = getMonth(d);

            return render(TMPL.week, function(_, dayIdx) {

                let dayIndexToRender = Number(dayIdx) + window.uv.weekStartsOn;
                if (dayIndexToRender === 7) {
                    dayIndexToRender = 0;
                }

                var html = '&nbsp;';

                if (dayIndexToRender === getDay(d) && m === getMonth(d)) {
                    html = renderDay(d);
                    d = addDays(d, 1);
                }

                return html;
            });
        }

        function renderDay(date) {

            function classes() {
                var category = assert.absenceCategory(date);
                return [
                    assert.isToday                          (date) ? CSS.dayToday                          : '',
                    assert.isWeekend                        (date) ? CSS.dayWeekend                        : '',
                    assert.isPast                           (date) ? CSS.dayPast                           : '',
                    assert.isPublicHolidayFull              (date) ? CSS.dayPublicHolidayFull              : '',
                    assert.isPublicHolidayMorning           (date) ? CSS.dayPublicHolidayMorning           : '',
                    assert.isPublicHolidayNoon              (date) ? CSS.dayPublicHolidayNoon              : '',
                    assert.isPersonalHolidayFull            (date) ? CSS.dayPersonalHolidayFull            : '',
                    assert.isPersonalHolidayFullApproved    (date) ? CSS.dayPersonalHolidayFullApproved    : '',
                    assert.isPersonalHolidayMorning         (date) ? CSS.dayPersonalHolidayMorning         : '',
                    assert.isPersonalHolidayMorningApproved (date) ? CSS.dayPersonalHolidayMorningApproved : '',
                    assert.isPersonalHolidayNoon            (date) ? CSS.dayPersonalHolidayNoon            : '',
                    assert.isPersonalHolidayNoonApproved    (date) ? CSS.dayPersonalHolidayNoonApproved    : '',
                    assert.isSickDayFull                    (date) ? CSS.daySickDayFull                    : '',
                    assert.isSickDayMorning                 (date) ? CSS.daySickDayMorning                 : '',
                    assert.isSickDayNoon                    (date) ? CSS.daySickDayNoon                    : '',
                ].filter(Boolean).join(' ').replace("{{category}}", category);
            }

            function isSelectable() {

                // NOTE: Order is important here!

                const isPersonalHoliday = assert.isPersonalHolidayFull(date);
                const isSickDay = assert.isSickDayFull(date);

                if(isPersonalHoliday || isSickDay) {
                  return true;
                }

                const isPast = assert.isPast(date);
                const isWeekend = assert.isWeekend(date);

                if(isPast || isWeekend) {
                    return false;
                }

                return assert.isHalfDayAbsence(date) || !assert.isPublicHolidayFull(date);
            }

            return render(TMPL.day, {
                date: format(date, 'yyyy-MM-dd'),
                day : format(date, 'dd'),
                css : classes(),
                selectable: isSelectable(),
                title: assert.title(date),
                absenceId: assert.absenceId(date),
                absenceType: assert.absenceType(date)
            });
        }

        var View = {

            display: function(date) {
                $datepicker.html( renderCalendar(date)).addClass('unselectable');
                tooltip();
            },

            displayNext: function() {

                var elements = $datepicker.find('.' + CSS.month).get();
                var length_      = elements.length;

                $(elements[0]).remove();

                var $lastMonth = $(elements[length_ - 1]);
                var month = Number ($lastMonth.data(DATA.month));
                var year  = Number ($lastMonth.data(DATA.year));

                var $nextMonth = $(renderMonth( addMonths(new Date(year, month), 1)));

                $lastMonth.after($nextMonth);
                tooltip();
            },

            displayPrevious: function() {

                var elements = $datepicker.find('.' + CSS.month).get();
                var length_ = elements.length;

                $(elements[length_ - 1]).remove();

                var $firstMonth = $(elements[0]);
                var month = Number ($firstMonth.data(DATA.month));
                var year  = Number ($firstMonth.data(DATA.year));

                var previousMonth = $(renderMonth( subMonths(new Date(year, month), 1)));

                $firstMonth.before(previousMonth);
                tooltip();
            }
        };

        return {
            create: function(_assert) {
                assert = _assert;
                return View;
            }
        };
    }());


    var Controller = (function() {

        var view;
        var holidayService;

        var datepickerHandlers = {

            mousedown: function(event) {

                if (event.button != mouseButtons.left) {
                    return;
                }

                $(document.body).addClass(CSS.mousedown);

                var dateThis = getDateFromElement(this);

                const start = selectionFrom();
                const end = selectionTo();
                if ( !isValidDate(start) || !isValidDate(end) || !isWithinInterval(dateThis, { start, end }) ) {

                    clearSelection();

                    $datepicker.data(DATA.selected, dateThis);

                    selectionFrom( dateThis );
                    selectionTo  ( dateThis );
                }
            },

            mouseup: function() {
                $(document.body).removeClass(CSS.mousedown);
            },

            mouseover: function() {
                if ( $(document.body).hasClass(CSS.mousedown) ) {

                    var dateThis     = getDateFromElement(this);
                    var dateSelected = $datepicker.data(DATA.selected);

                    var isThisBefore = isBefore(dateThis, dateSelected);

                    selectionFrom( isThisBefore ? dateThis     : dateSelected );
                    selectionTo  ( isThisBefore ? dateSelected : dateThis     );
                }
            },

            click: function() {

                var dateFrom = selectionFrom();
                var dateTo   = selectionTo  ();

                var dateThis = getDateFromElement(this);

                var isSelectable = $(this).attr("data-datepicker-selectable");
                var absenceId = $(this).attr('data-datepicker-absence-id');
                var absenceType = $(this).attr('data-datepicker-absence-type');

                if(isSelectable === "true" && absenceType === "VACATION" && absenceId !== "-1") {
                    holidayService.navigateToApplicationForLeave(absenceId);
                } else if(isSelectable === "true" && absenceType === "SICK_NOTE" && absenceId !== "-1") {
                    holidayService.navigateToSickNote(absenceId);
                } else if(isSelectable === "true" && isValidDate(dateFrom) && isValidDate(dateTo) && isWithinInterval(dateThis, { start: dateFrom, end: dateTo })) {
                    holidayService.bookHoliday(dateFrom, dateTo);
                }

            },

            clickNext: function() {

                // last month of calendar
                var $month = $( $datepicker.find('.' + CSS.month)[numberOfMonths-1] );

                const y = $month.data(DATA.year);
                const m = $month.data(DATA.month);

                // to load data for the new (invisible) prev month
                var date = addMonths(new Date(y, m, 1), 1);

                $.when(
                    holidayService.fetchPublic   ( getYear(date) ),
                    holidayService.fetchPersonal ( getYear(date) ),
                    holidayService.fetchSickDays ( getYear(date) )
                ).then(view.displayNext);
            },

            clickPrevious: function() {

                // first month of calendar
                var $month = $( $datepicker.find('.' + CSS.month)[0] );

                const y = $month.data(DATA.year);
                const m = $month.data(DATA.month);

                // to load data for the new (invisible) prev month
                var date = subMonths(new Date(y, m, 1), 1);

                $.when(
                    holidayService.fetchPublic   ( getYear(date) ),
                    holidayService.fetchPersonal ( getYear(date) ),
                    holidayService.fetchSickDays ( getYear(date) )
                ).then(view.displayPrevious);
            }
        };

        function getDateFromElement(element) {
            return parseISO($(element).data(DATA.date));
        }

        function selectionFrom(date) {
            if (!date) {
                const d = $datepicker.data(DATA.selectFrom);
                return parseISO(d);
            }

            $datepicker.data(DATA.selectFrom, format(date, 'yyyy-MM-dd'));
            refreshDatepicker();
        }

        function selectionTo(date) {
            if (!date) {
                return parseISO($datepicker.data(DATA.selectTo));
            }

            $datepicker.data(DATA.selectTo, format(date, 'yyyy-MM-dd'));
            refreshDatepicker();
        }

        function clearSelection() {
            $datepicker.removeData(DATA.selectFrom);
            $datepicker.removeData(DATA.selectTo);
            refreshDatepicker();
        }

        function refreshDatepicker() {

            const start = selectionFrom();
            const end   = selectionTo();
            const startIsValid = isValidDate(start);
            const endIsValid = isValidDate(end);

            $('.' + CSS.day).each(function() {
                if (!startIsValid || !endIsValid) {
                  select(this, false);
                } else {
                  const date = parseISO($(this).data(DATA.date));
                  select(this, isWithinInterval(date, { start, end }));
                }
            });
        }

        function select(element, select) {

            var element_ = $(element);

            if ( ! element_.data(DATA.selectable) ) {
                return;
            }

            if (select) {
                element_.addClass(CSS.daySelected);
            }
            else {
                element_.removeClass(CSS.daySelected);
            }
        }

        var Controller = {
            bind: function() {

                $datepicker.on('mousedown', '.' + CSS.day , datepickerHandlers.mousedown);
                $datepicker.on('mouseover', '.' + CSS.day , datepickerHandlers.mouseover);
                $datepicker.on('click'    , '.' + CSS.day , datepickerHandlers.click    );

                $datepicker.on('click'    , '.' + CSS.previous, datepickerHandlers.clickPrevious);
                $datepicker.on('click'    , '.' + CSS.next, datepickerHandlers.clickNext);


                $(document.body).on('keyup', function(event) {
                    if (event.keyCode === keyCodes.escape) {
                        clearSelection();
                    }
                });

                $(document.body).on('mouseup', function() {
                    $(document.body).removeClass(CSS.mousedown);
                });
            }
        };

        return {
            create: function(_holidayService, _view) {
                holidayService = _holidayService;
                view = _view;
                return Controller;
            }
        };
    }());


    var Calendar = (function () {

        var view;
        var date;

        return {
            init: function(holidayService, referenceDate) {

                date = referenceDate;

                var a = Assertion.create (holidayService);
                view = View.create(a);
                var c = Controller.create(holidayService, view);

                view.display(date);
                c.bind();
            },

            reRender: function() {
                view.display(date);
            }
        }
    })();

    /**
     * @export
     */
    window.Urlaubsverwaltung = {
        Calendar      : Calendar,
        HolidayService: HolidayService
    };

});

}
