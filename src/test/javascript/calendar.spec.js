import { setup, cleanup, waitForFinishedJQueryReadyCallbacks } from './TestSetupHelper';

describe ('calendar', () => {
    const RealDate = Date;
    const dateInstanceIdentifier = Symbol('date-identifier');

    // mocking Date with overridden instanceof operator o_O phew°°°
    // required since datefn verifies with foo instanceof Date
    function mockDate(isoDate) {
      global.Date = class extends RealDate {
        static [Symbol.hasInstance](instance) {
          return instance[dateInstanceIdentifier];
        }

        // noinspection JSAnnotator
        constructor(...args) {
          let d = args.length ? new RealDate(...args) : new RealDate(isoDate);
          d[dateInstanceIdentifier] = true;
          return d;
        }
      };
    }

    afterEach(() => {
      global.Date = RealDate;
    });

    beforeEach (calendarTestSetup);
    afterEach (cleanup);

    it ('renders', () => {
        renderCalendar(createHolidayService());
        expect(document.body).toMatchSnapshot();
    });

    it ('does not set halfDay on a weekend', () => {
        const holidayService = createHolidayService();
        jest.spyOn(holidayService, 'isHalfDay').mockReturnValue(true);

        renderCalendar(holidayService);

        const christmasEve = document.body.querySelector('[data-datepicker-date="2017-12-24"]');
        expect(christmasEve).not.toBeNull();
        expect(christmasEve.classList).not.toContain('datepicker-day-half');
    });

    it ('does not set datepicker-day-personal-holiday on a weekend', () => {
        const holidayService = createHolidayService();
        jest.spyOn(holidayService, 'isPersonalHoliday').mockReturnValue(true);

        renderCalendar(holidayService);

        const christmasEve = document.body.querySelector('[data-datepicker-date="2017-12-24"]');
        expect(christmasEve).not.toBeNull();
        expect(christmasEve.classList).not.toContain('datepicker-day-personal-holiday');
    });

    it ('does not set datepicker-day-sick-note on weekend', () => {
        const holidayService = createHolidayService();
        jest.spyOn(holidayService, 'isSickDay').mockReturnValue(true);

        renderCalendar(holidayService);

        const christmasEve = document.body.querySelector('[data-datepicker-date="2017-12-24"]');
        expect(christmasEve).not.toBeNull();
        expect(christmasEve.classList).not.toContain('datepicker-day-sick-note');
    });

    function createHolidayService () {
        const webPrefix = 'webPrefix';
        const apiPrefix = 'apiPrefix';
        const personId = 'personId';
        return window.Urlaubsverwaltung.HolidayService.create();
    }

    function renderCalendar (holidayService) {
        // note: Date is mocked in calendarTestSetup to return a fixed date value
        const referenceDate = new Date();
        window.Urlaubsverwaltung.Calendar.init(holidayService, referenceDate);
    }

    async function calendarTestSetup () {
        await setup();

        window.dateFns = await import('date-fns');

        // 01.12.2017
        mockDate(1512130448379);

        document.body.innerHTML = `<div id="datepicker"></div>`;

        // loading calendar.js registers a jQuery ready callback
        // which will be executed asynchronously
        await import('../../main/resources/static/js/calendar.js');

        // therefore we have to wait till ready callbacks are invoked
        return waitForFinishedJQueryReadyCallbacks();
    }
});
