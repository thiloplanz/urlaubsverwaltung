<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html>

<head>
    <uv:head/>
</head>

<body>
<spring:url var="URL_PREFIX" value="/web"/>

<sec:authorize access="hasAuthority('OFFICE')">
    <c:set var="IS_OFFICE" value="true"/>
</sec:authorize>

<uv:menu/>

<div class="print-info--only-portrait">
    <h4><spring:message code="print.info.portrait"/></h4>
</div>

<div class="content print--only-portrait">

    <div class="container">

        <div class="row">

            <div class="col-xs-12">
                <%@include file="include/overview_header.jsp" %>
            </div>

        </div>

        <div class="row">

            <div class="col-xs-12 col-sm-12 col-md-4">
                <uv:person person="${person}" nameIsNoLink="${true}"/>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-4">
                <uv:account-entitlement account="${account}"/>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-4">
                <uv:account-left account="${account}" vacationDaysLeft="${vacationDaysLeft}"
                                 beforeApril="${beforeApril}"/>
            </div>

        </div>

        <c:if test="${settings.workingTimeSettings.overtimeActive}">
            <div class="row">
                <div class="col-xs-12">
                    <legend>
                        <spring:message code="overtime.title"/>
                        <a href="${URL_PREFIX}/overtime?person=${person.id}"
                           class="fa-action pull-right" aria-hidden="true"
                           style="margin-top: 1px" data-title="<spring:message code="action.overtime.list"/>">
                            <i class="fa fa-th" aria-hidden="true"></i>
                        </a>
                        <c:if test="${person.id == signedInUser.id || IS_OFFICE}">
                            <a href="${URL_PREFIX}/overtime/new?person=${person.id}"
                               class="fa-action pull-right" aria-hidden="true"
                               data-title="<spring:message code="action.overtime.new"/>">
                                <i class="fa fa-plus-circle" aria-hidden="true"></i>
                            </a>
                        </c:if>
                    </legend>
                </div>
                <div class="col-xs-12 col-md-6">
                    <uv:overtime-total hours="${overtimeTotal}"/>
                </div>
                <div class="col-xs-12 col-md-6">
                    <uv:overtime-left hours="${overtimeLeft}"/>
                </div>
            </div>
        </c:if>

        <script src=<spring:url value='${ timelineDepartment != null ? "/js/timeline.js" : "/js/calendar.js" }' /> type="text/javascript" ></script>
        <script>
            $(function () {

                var datepickerLocale = "${pageContext.response.locale.language}";
                var personId = '<c:out value="${person.id}" />';
                var webPrefix = "<spring:url value='/web' />";
                var apiPrefix = "<spring:url value='/api' />";

                function initCalendar() {
                    const { getYear, setYear, startOfYear, subMonths, addMonths, format } = dateFns;

                    var year = getUrlParam("year");
                    var date = new Date();

                    if (year.length > 0 && year != getYear(date)) {
                        date = startOfYear(setYear(date, year));
                    }

                    $('#month-selection span.labelText').text(format(date, 'MMMM'));

                    var holidayService = Urlaubsverwaltung.HolidayService.create(webPrefix, apiPrefix, +personId, +'${timelineDepartment.id}');

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

        <c:if test="${timelineDepartment != null}">

           var t = Urlaubsverwaltung.Calendar.timelinePersons = {};
           <% /* how to do this without a scriptlet? */ pageContext.setAttribute("umlaute", java.text.Collator.getInstance(java.util.Locale.GERMANY)) ; %>
           <c:set var="sortedMembers" value="${timelineDepartment.members.stream().sorted((a,b) -> umlaute.compare(a.lastName, b.lastName)).map(x -> x.id).toArray()}" />
           t.byName = <%= /* how to do this without a scriptlet? */ java.util.Arrays.toString((Object[])pageContext.getAttribute("sortedMembers")) %>;
           <c:forEach items="${timelineDepartment.members}" var="p" >t[${p.id}] = '<spring:escapeBody>${p.niceName}</spring:escapeBody>';
           </c:forEach>

           $('#month-selection ul a').on('click', function(event){
                $('#month-selection span.labelText').text(this.text);
                Urlaubsverwaltung.Calendar.jumpToMonth(${displayYear}, $(this).data('month'));
                event.preventDefault();
           });

           $('#jumpToToday').on('click', function(){
                const { getYear, format } = dateFns;

                $('#month-selection span.labelText').text(format(new Date(), 'MMMM'));
                if (getYear(new Date()) == ${displayYear}){
                    Urlaubsverwaltung.Calendar.jumpToToday();
                } else {
                    location.href = "${URL_PREFIX}/staff/${person.id}/overview?timelineDepartment=${timelineDepartment.id}&year=#calendar-selector";
                }
           });

        </c:if>


            });


        </script>


        <div class="row">
            <div class="col-xs-12">
                <legend >
                     <div class="legend-dropdown dropdown">
                             <c:choose>
                                <c:when test="${timelineDepartment == null}">
                                    <a id="calendar-selector" name="calendar-selector" href="#" data-toggle="dropdown"
                                                             aria-haspopup="true" role="button" aria-expanded="false">
                                        <spring:message code="overview.calendar.title" /> <c:out value="${person.niceName}" /><span class="caret"></span>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a id="calendar-selector" name="calendar-selector" href="#" data-toggle="dropdown"
                                                                                                 aria-haspopup="true" role="button" aria-expanded="false">
                                        <spring:message code="overview.calendar.title" /> <c:out value="${timelineDepartment.name}" /><span class="caret"></span>
                                    </a>
                                    <uv:month-selector month="Monat" />  <%-- Month named filled in by Javascript on pageReady --%>
                                     &nbsp;
                                    <button type="button" class="btn btn-default btn-sm" id='jumpToToday'><spring:message code="overview.calendar.button.today" /></button>
                                </c:otherwise>
                            </c:choose>
                            <ul class="dropdown-menu" role="menu" aria-labelledby="calendar-selector">
                                <li>
                                    <a href="?year=${displayYear}#calendar-selector">
                                         <i class="fa fa-fw fa-user"></i>
                                         <c:out value="${person.niceName}" />
                                    </a>
                                </li>
                                <li role="separator" class="divider"></li>
                                <c:forEach items="${departments}" var="d">
                                <li>
                                    <a href="?year=${displayYear}&timelineDepartment=${d.id}#calendar-selector">
                                         <i class="fa fa-fw fa-group"></i>
                                         <c:out value="${d.name}" />
                                    </a>
                                </li>
                                </c:forEach>
                            </ul>
                    </div>
                </legend>

                <div id="datepicker"></div>
            </div>

        </div>



        <div class="row">
            <div class="col-xs-12">
                <legend id="vacation">
                    <spring:message code="applications.title"/>
                    <c:choose>
                        <c:when test="${person.id == signedInUser.id}">
                            <a class="fa-action pull-right" aria-hidden="true" href="${URL_PREFIX}/application/new"
                               data-title="<spring:message code="action.apply.vacation"/>">
                                <i class="fa fa-plus-circle" aria-hidden="true"></i>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <c:if test="${IS_OFFICE}">
                                <a class="fa-action pull-right" aria-hidden="true"
                                   href="${URL_PREFIX}/application/new?person=${person.id}"
                                   data-title="<spring:message code="action.apply.vacation"/>">
                                    <i class="fa fa-plus-circle" aria-hidden="true"></i>
                                </a>
                            </c:if>
                        </c:otherwise>
                    </c:choose>

                </legend>
            </div>
        </div>

        <div class="row">

            <c:set var="holidayLeave"
                   value="${usedDaysOverview.holidayDays.days['WAITING'] + usedDaysOverview.holidayDays.days['TEMPORARY_ALLOWED'] + usedDaysOverview.holidayDays.days['ALLOWED'] + 0}"/>
            <c:set var="holidayLeaveAllowed" value="${usedDaysOverview.holidayDays.days['ALLOWED'] + 0}"/>
            <c:set var="otherLeave"
                   value="${usedDaysOverview.otherDays.days['WAITING'] + usedDaysOverview.otherDays.days['TEMPORARY_ALLOWED'] + usedDaysOverview.otherDays.days['ALLOWED'] + 0}"/>
            <c:set var="otherLeaveAllowed" value="${usedDaysOverview.otherDays.days['ALLOWED'] + 0}"/>

            <div class="col-xs-12 col-sm-12 col-md-6">
                <div class="box">
                    <span class="box-icon bg-yellow hidden-print">
                        <i class="fa fa-sun-o" aria-hidden="true"></i>
                    </span>
                    <span class="box-text">
                        <spring:message code="overview.vacations.holidayLeave" arguments="${holidayLeave}"/>
                        <i class="fa fa-check positive" aria-hidden="true"></i> <spring:message
                        code="overview.vacations.holidayLeaveAllowed" arguments="${holidayLeaveAllowed}"/>
                    </span>
                </div>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-6">
                <div class="box">
                    <span class="box-icon bg-yellow hidden-print"><i class="fa fa-flag-o" aria-hidden="true"></i></span>
                    <span class="box-text">
                        <spring:message code="overview.vacations.otherLeave" arguments="${otherLeave}"/>
                        <i class="fa fa-check positive" aria-hidden="true"></i> <spring:message code="overview.vacations.otherLeaveAllowed"
                                                                             arguments="${otherLeaveAllowed}"/>
                    </span>
                </div>
            </div>

        </div>

        <div class="row">

            <div class="col-xs-12">
                <%@include file="include/overview_app_list.jsp" %>
            </div>

        </div>

        <c:if test="${person.id == signedInUser.id || IS_OFFICE}">

            <div class="row">
                <div class="col-xs-12">
                    <legend id="anchorSickNotes">
                        <spring:message code="sicknotes.title"/>
                        <c:if test="${IS_OFFICE}">
                            <a class="fa-action pull-right" href="${URL_PREFIX}/sicknote/new?person=${person.id}"
                               data-title="<spring:message code="action.apply.sicknote" />">
                                <i class="fa fa-plus-circle" aria-hidden="true"></i>
                            </a>
                        </c:if>
                    </legend>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12 col-sm-12 col-md-6">
                    <div class="box">
                        <span class="box-icon bg-red hidden-print">
                            <i class="fa fa-medkit" aria-hidden="true"></i>
                        </span>
                        <span class="box-text">
                        <spring:message code="overview.sicknotes.sickdays"
                                        arguments="${sickDaysOverview.sickDays.days['TOTAL']}"/>
                        <i class="fa fa-check positive" aria-hidden="true"></i>
                        <spring:message code="overview.sicknotes.sickdays.aub"
                                        arguments="${sickDaysOverview.sickDays.days['WITH_AUB']}"/>
                    </span>
                    </div>
                </div>
                <div class="col-xs-12 col-sm-12 col-md-6">
                    <div class="box">
                        <span class="box-icon bg-red hidden-print">
                            <i class="fa fa-child" aria-hidden="true"></i>
                        </span>
                        <span class="box-text">
                        <spring:message code="overview.sicknotes.sickdays.child"
                                        arguments="${sickDaysOverview.childSickDays.days['TOTAL']}"/>
                        <i class="fa fa-check positive" aria-hidden="true"></i>
                        <spring:message code="overview.sicknotes.sickdays.aub"
                                        arguments="${sickDaysOverview.childSickDays.days['WITH_AUB']}"/>
                    </span>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <%@include file="include/sick_notes.jsp" %>
                </div>
            </div>

        </c:if>

    </div>
</div>


</body>


