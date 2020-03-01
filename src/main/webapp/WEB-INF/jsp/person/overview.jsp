<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <spring:message code="overview.header.title" arguments="${person.niceName}, ${year}"/>
    </title>
    <uv:custom-head/>
    <link rel="stylesheet" type="text/css" href="<asset:url value='app_form~overtime_form~person_overview~sick_note_form.css' />" />
    <script>
        window.uv = {};
        window.uv.personId = '<c:out value="${person.id}" />';
        window.uv.isOffice = '<c:out value="${person.office}" />';
        window.uv.webPrefix = "<spring:url value='/web' />";
        window.uv.apiPrefix = "<spring:url value='/api' />";
        // 0=sunday, 1=monday
        window.uv.weekStartsOn = 1;
    </script>
    <script defer src="<asset:url value="npm.date-fns.js" />"></script>
    <script defer src="<asset:url value="date-fns-localized.js" />"></script>
    <script defer src="<asset:url value="app_detail~app_form~person_overview.js" />"></script>
    <script defer src="<asset:url value='app_form~overtime_form~person_overview~sick_note_form.js' />"></script>

    <c:if test="${timelineDepartment != null}">
        <c:choose>
            <c:when test="${!empty param.year}">
                <c:set var="displayYear" value="${param.year}"/>
            </c:when>
            <c:otherwise>
                <c:set var="displayYear" value="${year}"/>
            </c:otherwise>
        </c:choose>

        <script defer>
           var t = {};
           window.yados = { timelineDepartmentId: ${timelineDepartment.id}, displayYear: ${displayYear}, timelinePersons: t };

           <% /* how to do this without a scriptlet? */ pageContext.setAttribute("umlaute", java.text.Collator.getInstance(java.util.Locale.GERMANY)) ; %>
           <c:set var="sortedMembers" value="${timelineDepartment.members.stream().sorted((a,b) -> umlaute.compare(a.lastName, b.lastName)).map(x -> x.id).toArray()}" />
           <c:set var="sortedMembers" value="${timelineDepartment.members.stream().sorted((a,b) -> umlaute.compare(a.lastName, b.lastName)).map(x -> x.id).toArray()}" />
           t.byName = <%= /* how to do this without a scriptlet? */ java.util.Arrays.toString((Object[])pageContext.getAttribute("sortedMembers")) %>;
           <c:forEach items="${timelineDepartment.members}" var="p" >t[${p.id}] = '<spring:escapeBody>${p.niceName}</spring:escapeBody>';
           </c:forEach>
        </script>
    </c:if>


    <script defer src="<asset:url value="person_overview.js" />"></script>
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

        <!-- Overtime -->
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

        <!-- Calendar -->
        <div class="row">
            <div class="col-xs-12">
                <legend id="vacation">
                     <a class="fa-action pull-right text-base flex justify-center" aria-hidden="true" href="${URL_PREFIX}/calendars/share/persons/${personId}">
                         <i class="fa fa-calendar" aria-hidden="true"></i>
                         &nbsp;<spring:message code="overview.calendar.share.link.text" />
                     </a>
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

        <!-- Vacation -->
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
                    <span class="box-icon bg-yellow hidden-print">
                        <i class="fa fa-flag-o" aria-hidden="true"></i>
                    </span>
                    <span class="box-text">
                        <spring:message code="overview.vacations.otherLeave" arguments="${otherLeave}"/>
                        <i class="fa fa-check positive" aria-hidden="true"></i>
                        <spring:message code="overview.vacations.otherLeaveAllowed" arguments="${otherLeaveAllowed}"/>
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


