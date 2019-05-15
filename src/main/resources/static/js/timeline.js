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
        dayHalf               : 'datepicker-day-half',
        dayPublicHoliday      : 'datepicker-day-public-holiday',
        dayPersonalHoliday    : 'datepicker-day-personal-holiday datepicker-day-personal-holiday-{{category}}',
        daySickDay            : 'datepicker-day-sick-note datepicker-day-sick-note-{{category}}',
        dayStatus             : 'datepicker-day-status-{{status}}',
        next                  : 'datepicker-next',
        prev                  : 'datepicker-prev',
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

    // 0=sunday, 1=monday
    const weekStartsOn = 1;

    const {
        addDays,
        addMonths,
        addWeeks,
        getYear,
        isBefore,
        isPast,
        isToday,
        isWeekend,
        isWithinRange,
        format,
        getDay,
        getMonth,
        parse,
        startOfMonth,
        subMonths,
        subDays,
        setYear,
        setMonth,
        setDate
    } = dateFns;

    const startOfWeek = function startOfWeek(date) {
      return dateFns.startOfWeek(date, { weekStartsOn });
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
                isPublicHoliday: function(date, personId) {
                    return holidayService.isPublicHoliday(date, personId);
                },
                isPersonalHoliday: function(date, personId) {
                    return !isWeekend(date) && holidayService.isPersonalHoliday(date, personId);
                },
                isSickDay: function(date, personId) {
                    return !isWeekend(date) && holidayService.isSickDay(date, personId);
                },
                isHalfDay: function(date, personId) {
                  return !isWeekend(date) && holidayService.isHalfDay(date, personId);
                },
                title: function(date, personId) {
                  return holidayService.getDescription(date, personId);
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


        function isOfType(type) {
          return function (date, personId) {

            var year = getYear(date);
            var formattedDate = format(date, 'YYYY-MM-DD');

            var holiday = CACHE_findDate(type, year, formattedDate, personId);
            if (type === 'publicHoliday') {
                return holiday && holiday.dayLength < 1;
            } else {
                return holiday;
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
                return c && _.findWhere(c, {date: formattedDate});
            }
            if (type === 'holiday') {
                c = c && c.personAbsences;
                c = c && c[personId];
                return c && _.findWhere(c, {date: formattedDate, type: 'VACATION' })
            }
            if (type === 'sick') {
                c = c && c.personAbsences;
                c = c && c[personId];
                return c && _.findWhere(c, {date: formattedDate, type: 'SICK_NOTE' })
            }
        }

        var HolidayService = {

            isSickDay: isOfType('sick'),

            isPersonalHoliday: isOfType('holiday'),

            isPublicHoliday: isOfType('publicHoliday'),

            isHalfDay: function (date, personId) {

                var year = getYear(date);
                var formattedDate = format(date, 'YYYY-MM-DD');

                var publicHoliday = CACHE_findDate('publicHoliday', year, formattedDate, personId)
                if(publicHoliday && publicHoliday.dayLength === 0.5) {
                  return true;
                }

                var personalHoliday = CACHE_findDate('holiday', year, formattedDate, personId);
                if(personalHoliday && personalHoliday.dayLength === 0.5) {
                  return true;
                }


                var sickDay = CACHE_findDate('sick', year, formattedDate, personId);
                if(sickDay && sickDay.dayLength === 0.5) {
                  return true;
                }

                return false;
            },

            getDescription: function (date, personId) {

              var year = getYear(date);
              var formattedDate = format(date, 'YYYY-MM-DD');

               var publicHoliday = CACHE_findDate('publicHoliday', year, formattedDate, personId)

               return publicHoliday ? publicHoliday.description : '';

            },

            getStatus: function (date, personId) {

              var year = getYear(date);
              var formattedDate = format(date, 'YYYY-MM-DD');

              var holiday = CACHE_findDate('holiday', year, formattedDate, personId);

              if(holiday) {
                return holiday.status;
              }


              return null;

            },


            getAbsenceId: function (date, personId) {

              var year = getYear(date);
              var formattedDate = format(date, 'YYYY-MM-DD');


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
                var formattedDate = format(date, 'YYYY-MM-DD');
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
                var formattedDate = format(date, 'YYYY-MM-DD');
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
            bookHoliday: function(from, to, personId) {

                var params = {
                    personId: personId,
                    from :      format(from, 'YYYY-MM-DD'),
                    to   : to ? format(to, 'YYYY-MM-DD') : undefined
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

            fetchSickDays: function(year){
                // keep the stub for API compatibility with calendar.js
            },

            fetchPublic : function(year){
                // keep the stub for API compatibility with calendar.js
            },

        };

        return {
            create: function(_webPrefix, _apiPrefix, _personId, _departmentId) {
                webPrefix = _webPrefix;
                apiPrefix = _apiPrefix;
                departmentId  = _departmentId;
                return HolidayService;
            }
        };

    }());


    var View = (function() {

        var assert;

        var TMPL = {

            container: '{{prevBtn}}<div class="datepicker-months-container" style="height: {{height}};">{{weeks}}<div class="datepicker-person">{{names}}</div></div>{{nextBtn}}',

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

                var val = data[type];
                return typeof val === 'function' ? val() : val;
            });
        }

        function renderCalendar(date, personIds) {

            var weeksToShow = numberOfWeeks;

            return render(TMPL.container, {

                height:  90 + personIds.length * 40  ,

                prevBtn   : renderButton ( CSS.prev, '<i class="fa fa-chevron-left"></i>'),
                nextBtn   : renderButton ( CSS.next, '<i class="fa fa-chevron-right"></i>'),

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
                    for (var i =0; i<personIds.length; i++) {
                        html += render(
                            TMPL.name,
                            {name : Urlaubsverwaltung.Calendar.timelinePersons[personIds[i]]}
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
            for (var i=0; i<personIds.length; i++){
                p += renderWeek(date, personIds[i])
            }
            return render(TMPL.week, {
                startDate: format(date, 'YYYY-MM-DD'),
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
                     title: format(endMonth, 'MMMM YYYY')
                });
            }
            return render(TMPL.title, {
                title: format(date, 'MMMM') + ' / ' +format(endMonth, 'MMMM YYYY')
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
                var status = assert.status(date, personId)
                var category = assert.absenceCategory(date, personId)
                return [
                    assert.isToday           (date) ? CSS.dayToday           : '',
                    assert.isWeekend         (date) ? CSS.dayWeekend         : '',
                    assert.isPast            (date) ? CSS.dayPast            : '',
                    assert.isPublicHoliday   (date, personId) ? CSS.dayPublicHoliday   : '',
                    assert.isPersonalHoliday (date, personId) ? CSS.dayPersonalHoliday.replace("{{category}}", category) : '',
                    assert.isSickDay         (date, personId) ? CSS.daySickDay.replace("{{category}}", category)         : '',
                    assert.isHalfDay         (date, personId) ? CSS.dayHalf            : '',
                    status             ? CSS.dayStatus.replace("{{status}}", status)   : ''
                ].join(' ');
            }



            function isSelectable() {

                // NOTE: Order is important here!

                var isPersonalHoliday = assert.isPersonalHoliday(date, personId);
                var isSickDay = assert.isSickDay(date, personId);

                if(isPersonalHoliday || isSickDay) {
                  return true;
                }

                var isPast = assert.isPast(date);
                var isWeekend = assert.isWeekend(date);

                if(isPast || isWeekend) {
                    return false;
                }

                return assert.isHalfDay(date, personId) || !assert.isPublicHoliday(date, personId);
            }

            return render(TMPL.day, {
                date: format(date, 'YYYY-MM-DD'),
                day : format(date, 'DD'),
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
                var len      = elements.length;

                $(elements[0]).remove();

                var $lastWeek = $(elements[len - 1]);
                var start = parse($lastWeek.data(DATA.startDate));

                var $nextWeek = $(renderWeekBox( addDays(start, 7), View.personIds));

                $lastWeek.after($nextWeek);
                tooltip();
            },

            displayPrev: function() {

                var elements = $datepicker.find('.' + CSS.week).get();
                var len = elements.length;

                $(elements[len - 1]).remove();

                var $firstWeek = $(elements[0]);
                var start = parse($firstWeek.data(DATA.startDate));

                var $prevWeek = $(renderWeekBox( subDays(start, 7), View.personIds));

                $firstWeek.before($prevWeek);
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

                var dateThis = getDateFromEl(this);

                if ( !isWithinRange(dateThis, selectionFrom(), selectionTo()) ) {

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

                    var dateThis     = getDateFromEl(this);
                    var dateSelected = $datepicker.data(DATA.selected);

                    var isThisBefore = isBefore(dateThis, dateSelected);

                    selectionFrom( isThisBefore ? dateThis     : dateSelected );
                    selectionTo  ( isThisBefore ? dateSelected : dateThis     );
                }
            },

            click: function() {

                var dateFrom = selectionFrom();
                var dateTo   = selectionTo  ();

                var dateThis = getDateFromEl(this);

                var isSelectable = $(this).attr("data-datepicker-selectable");
                var absenceId = $(this).attr('data-datepicker-absence-id');
                var absenceType = $(this).attr('data-datepicker-absence-type');
                var personId = $(this).closest('tr').attr('data-datepicker-person')

                if(isSelectable === "true" && absenceType === "VACATION" && absenceId !== "-1") {
                    holidayService.navigateToApplicationForLeave(absenceId);
                } else if(isSelectable === "true" && absenceType === "SICK_NOTE" && absenceId !== "-1") {
                    holidayService.navigateToSickNote(absenceId);
                } else if(isSelectable === "true" && isWithinRange(dateThis, dateFrom, dateTo)) {
                    holidayService.bookHoliday(dateFrom, dateTo, personId);
                }

            },

            clickNext: function() {

                // last week of calendar
                var $week = $( $datepicker.find('.' + CSS.week)[numberOfWeeks-1] );

                // to load data for the new (invisible) next week
                var date = addDays(parse($week.data(DATA.startDate))
                    , 13); // go to the last day of the week, it may be in a new year

                $.when(
                    holidayService.fetchAbsences   ( getYear(date) )
                ).then(view.displayNext);
            },

            clickPrev: function() {

                // first week of calendar
                var $week = $( $datepicker.find('.' + CSS.week)[0] );

                // to load data for the new (invisible) prev week
                var date = subDays(parse($week.data(DATA.startDate))
                                    , 7);

                $.when(
                    holidayService.fetchAbsences   ( getYear(date) )
                ).then(view.displayPrev);
            }
        };

        function getDateFromEl(el) {
            return parse($(el).data(DATA.date));
        }

        function selectionFrom(date) {
            if (!date) {
                return parse($datepicker.data(DATA.selectFrom));
            }

            $datepicker.data(DATA.selectFrom, format(date,'YYYY-MM-DD'));
            refreshDatepicker();
        }

        function selectionTo(date) {
            if (!date) {
                return parse($datepicker.data(DATA.selectTo));
            }

            $datepicker.data(DATA.selectTo, format(date, 'YYYY-MM-DD'));
            refreshDatepicker();
        }

        function clearSelection() {
            $datepicker.removeData(DATA.selectFrom);
            $datepicker.removeData(DATA.selectTo);
            refreshDatepicker();
        }


        function refreshDatepicker() {

            var from = selectionFrom();
            var to   = selectionTo();

            $('.' + CSS.day).each(function() {
                var d = parse( $(this).data(DATA.date) );
                select(this, isWithinRange(d, from, to));
            });
        }

        function select(el, select) {

            var $el = $(el);

            if ( ! $el.data(DATA.selectable) ) {
                return;
            }

            if (!!select) {
                $el.addClass(CSS.daySelected);
            }
            else {
                $el.removeClass(CSS.daySelected);
            }
        }

        var Controller = {
            bind: function() {

                $datepicker.on('mousedown', '.' + CSS.day , datepickerHandlers.mousedown);
                $datepicker.on('mouseover', '.' + CSS.day , datepickerHandlers.mouseover);
                $datepicker.on('click'    , '.' + CSS.day , datepickerHandlers.click    );

                $datepicker.on('click'    , '.' + CSS.prev, datepickerHandlers.clickPrev);
                $datepicker.on('click'    , '.' + CSS.next, datepickerHandlers.clickNext);


                $(document.body).on('keyup', function(e) {
                    if (e.keyCode === keyCodes.escape) {
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

                personIds = Urlaubsverwaltung.Calendar.timelinePersons.byName

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
