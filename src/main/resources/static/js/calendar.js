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
        day                   : 'datepicker-day',
        daySelected           : 'datepicker-day-selected',
        dayToday              : 'datepicker-day-today',
        dayWeekend            : 'datepicker-day-weekend',
        dayPast               : 'datepicker-day-past',
        dayHalf               : 'datepicker-day-half',
        dayPublicHoliday      : 'datepicker-day-public-holiday',
        dayHalfPublicHoliday  : 'datepicker-day-half-public-holiday',
        dayPersonalHoliday    : 'datepicker-day-personal-holiday',
        dayHalfPersonalHoliday: 'datepicker-day-half-personal-holiday',
        daySickDay            : 'datepicker-day-sick-note',
        dayStatus             : 'datepicker-day-status-{{status}}',
        next                  : 'datepicker-next',
        prev                  : 'datepicker-prev',
        month                 : 'datepicker-month',
        mousedown             : 'mousedown'
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
            isPublicHoliday: function(date) {
                return holidayService.isPublicHoliday(date);
            },
            isPersonalHoliday: function(date) {
                return !isWeekend(date) && holidayService.isPersonalHoliday(date);
            },
            isSickDay: function(date) {
                return !isWeekend(date) && holidayService.isSickDay(date);
            },
            isHalfDay: function(date) {
              return !isWeekend(date) && holidayService.isHalfDay(date);
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

        function isOfType(type) {
          return function (date) {
            var year = getYear(date);
            var formattedDate = format(date, 'YYYY-MM-DD');

            if (!_CACHE[type]) {
                return false;
            }

            if(_CACHE[type][year]) {

              var holiday = _.findWhere(_CACHE[type][year], {date: formattedDate});

              if (type === 'publicHoliday') {
                return holiday !== undefined && holiday.dayLength < 1;
              } else {
                return holiday !== undefined;
              }

            }

            return false;
          };
        }

        var HolidayService = {

            isSickDay: isOfType('sick'),

            isPersonalHoliday: isOfType('holiday'),

            isPublicHoliday: isOfType('publicHoliday'),

            isHalfDay: function (date) {
              var year = getYear(date);
              var formattedDate = format(date, 'YYYY-MM-DD');

              if (!_CACHE['publicHoliday']) {
                  return false;
              }

              if(_CACHE['publicHoliday'][year]) {

                var publicHoliday = _.findWhere(_CACHE['publicHoliday'][year], {date: formattedDate});

                if(publicHoliday && publicHoliday.dayLength === 0.5) {
                  return true;
                }

              }

              if(_CACHE['holiday'][year]) {

                var personalHoliday = _.findWhere(_CACHE['holiday'][year], {date: formattedDate});

                if(personalHoliday && personalHoliday.dayLength === 0.5) {
                  return true;
                }

              }

              if(_CACHE['sick'][year]) {

                  var sickDay = _.findWhere(_CACHE['sick'][year], {date: formattedDate});

                  if(sickDay && sickDay.dayLength === 0.5) {
                      return true;
                  }

              }

              return false;
            },

            getDescription: function (date) {
              var year = getYear(date);
              var formattedDate = format(date, 'YYYY-MM-DD');

              if (!_CACHE['publicHoliday']) {
                  return '';
              }

              if(_CACHE['publicHoliday'][year]) {

                var publicHoliday = _.findWhere(_CACHE['publicHoliday'][year], {date: formattedDate});

                if(publicHoliday) {
                  return publicHoliday.description;
                }

              }

              return '';

            },

            getStatus: function (date) {
              var year = getYear(date);
              var formattedDate = format(date, 'YYYY-MM-DD');

              if (!_CACHE['holiday']) {
                  return null;
              }

              if(_CACHE['holiday'][year]) {

                var holiday = _.findWhere(_CACHE['holiday'][year], {date: formattedDate});

                if(holiday) {
                  return holiday.status;
                }

              }

              return null;

            },



            getAbsenceId: function (date) {
              var year = getYear(date);
              var formattedDate = format(date, 'YYYY-MM-DD');

              if (!_CACHE['holiday']) {
                  return '-1';
              }

              if(_CACHE['holiday'][year]) {

                var holiday = _.findWhere(_CACHE['holiday'][year], {date: formattedDate});

                if(holiday) {
                  return holiday.href;
                }

              }

              if(_CACHE['sick'][year]) {

                  var sickDay = _.findWhere(_CACHE['sick'][year], {date: formattedDate});

                  if(sickDay) {
                      return sickDay.href;
                  }

              }

              return '-1';

            },

            getAbsenceType: function (date) {
                var year = getYear(date);
                var formattedDate = format(date, 'YYYY-MM-DD');

                if (!_CACHE['holiday']) {
                    return '';
                }

                if(_CACHE['holiday'][year]) {

                    var holiday = _.findWhere(_CACHE['holiday'][year], {date: formattedDate});

                    if(holiday) {
                        return holiday.type;
                    }

                }

                if(_CACHE['sick'][year]) {

                    var sickDay = _.findWhere(_CACHE['sick'][year], {date: formattedDate});

                    if(sickDay) {
                        return sickDay.type;
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
                    from: format(from, 'YYYY-MM-DD'),
                    to: to ? format(to, 'YYYY-MM-DD') : undefined
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

            container: '{{prevBtn}}<div class="datepicker-months-container">{{months}}</div>{{nextBtn}}',

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

                var val = data[type];
                return typeof val === 'function' ? val() : val;
            });
        }

        function renderCalendar(date) {

            var monthsToShow = numberOfMonths;

            return render(TMPL.container, {

                prevBtn   : renderButton ( CSS.prev, '<i class="fa fa-chevron-left" aria-hidden="true"></i>'),
                nextBtn   : renderButton ( CSS.next, '<i class="fa fa-chevron-right" aria-hidden="true"></i>'),

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
                title: format(date, 'MMMM YYYY')
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

        function renderWeek(date) {
            var d = date;
            var m = getMonth(d);

            return render(TMPL.week, function(_, dayIdx) {

                let dayIndexToRender = Number(dayIdx) + weekStartsOn;
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
                var status = assert.status(date);
                return [
                    assert.isToday           (date) ? CSS.dayToday                                  : '',
                    assert.isWeekend         (date) ? CSS.dayWeekend                                : '',
                    assert.isPast            (date) ? CSS.dayPast                                   : '',
                    assert.isPublicHoliday   (date) ? CSS.dayPublicHoliday                          : '',
                    assert.isPersonalHoliday (date) ? CSS.dayPersonalHoliday                        : '',
                    assert.isSickDay         (date) ? CSS.daySickDay                                : '',
                    assert.isHalfDay         (date) ? CSS.dayHalf                                   : '',
                    status                          ? CSS.dayStatus.replace("{{status}}", status)   : ""
                ].join(' ');
            }

            function isSelectable() {

                // NOTE: Order is important here!

                var isPersonalHoliday = assert.isPersonalHoliday(date);
                var isSickDay = assert.isSickDay(date);

                if(isPersonalHoliday || isSickDay) {
                  return true;
                }

                var isPast = assert.isPast(date);
                var isWeekend = assert.isWeekend(date);

                if(isPast || isWeekend) {
                    return false;
                }

                return assert.isHalfDay(date) || !assert.isPublicHoliday(date);
            }

            return render(TMPL.day, {
                date: format(date, 'YYYY-MM-DD'),
                day : format(date, 'DD'),
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
                var len      = elements.length;

                $(elements[0]).remove();

                var $lastMonth = $(elements[len - 1]);
                var month = Number ($lastMonth.data(DATA.month));
                var year  = Number ($lastMonth.data(DATA.year));

                var $nextMonth = $(renderMonth( addMonths(new Date(year, month), 1)));

                $lastMonth.after($nextMonth);
                tooltip();
            },

            displayPrev: function() {

                var elements = $datepicker.find('.' + CSS.month).get();
                var len = elements.length;

                $(elements[len - 1]).remove();

                var $firstMonth = $(elements[0]);
                var month = Number ($firstMonth.data(DATA.month));
                var year  = Number ($firstMonth.data(DATA.year));

                var $prevMonth = $(renderMonth( subMonths(new Date(year, month), 1)));

                $firstMonth.before($prevMonth);
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

                if(isSelectable === "true" && absenceType === "VACATION" && absenceId !== "-1") {
                    holidayService.navigateToApplicationForLeave(absenceId);
                } else if(isSelectable === "true" && absenceType === "SICK_NOTE" && absenceId !== "-1") {
                    holidayService.navigateToSickNote(absenceId);
                } else if(isSelectable === "true" && isWithinRange(dateThis, dateFrom, dateTo)) {
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

            clickPrev: function() {

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

            $datepicker.data(DATA.selectFrom, format(date, 'YYYY-MM-DD'));
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
