<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <spring:message code="applications.statistics.header.title"/>
    </title>
    <uv:custom-head/>
    <script defer src="<asset:url value='npm.tablesorter.js' />"></script>
    <script defer src="<asset:url value='app_statistics.js' />"></script>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>
<c:set var="linkPrefix" value="${URL_PREFIX}/application"/>

<uv:menu/>

<div class="print-info--only-landscape">
    <h4><spring:message code="print.info.landscape"/></h4>
</div>

<div class="content print--only-landscape">

    <div class="container">

        <div class="row">

            <div class="col-xs-12">

                <legend class="is-sticky">
                    <spring:message code="applications.statistics"/>
                    <uv:print/>
                    <uv:export/>
                </legend>

                <p class="is-inline-block">
                    <c:choose>
                        <c:when test="${not empty errors}">
                            <a href="#filterModal" data-toggle="modal">
                                <spring:message code="applications.statistics.error.period"/>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="#filterModal" data-toggle="modal">
                                <spring:message code="filter.period"/>:&nbsp;<uv:date date="${from}"/> - <uv:date
                                date="${to}"/>
                            </a>
                        </c:otherwise>
                    </c:choose>
                </p>

                <uv:filter-modal id="filterModal" actionUrl="${linkPrefix}/statistics"/>

                <c:choose>
                    <c:when test="${not empty errors}">
                        <div class="alert alert-danger">
                            <spring:message code='applications.statistics.error'/>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <table class="list-table sortable tablesorter">
                            <thead class="hidden-xs hidden-sm">
                            <tr>
                                <th scope="col" class="hidden-print"><%-- placeholder to ensure correct number of th --%></th>
                                <th scope="col" class="sortable-field"><spring:message code="person.data.firstName"/></th>
                                <th scope="col" class="sortable-field"><spring:message code="person.data.lastName"/></th>
                                <th scope="col"><%-- placeholder to ensure correct number of th --%></th>
                                <th scope="col" class="hidden"><%-- placeholder to ensure correct number of th --%></th>
                                <th scope="col" class="hidden"><%-- placeholder to ensure correct number of th --%></th>
                                <th scope="col" class="sortable-field"><spring:message code="applications.statistics.allowed"/></th>
                                <th scope="col" class="sortable-field"><spring:message code="applications.statistics.waiting"/></th>
                                <th scope="col" class="sortable-field"><spring:message code="applications.statistics.left"/> (<c:out
                                    value="${from.year}"/>)
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${statistics}" var="statistic">
                                <tr>
                                    <td class="hidden-print is-centered">
                                        <div class="gravatar img-circle hidden-print"
                                             data-gravatar="<c:out value='${statistic.person.gravatarURL}?d=mm&s=60'/>"></div>
                                    </td>
                                    <td class="hidden-xs"><c:out value="${statistic.person.firstName}"/></td>
                                    <td class="hidden-xs"><c:out value="${statistic.person.lastName}"/></td>
                                    <td class="visible-xs hidden-print">
                                        <c:out value="${statistic.person.niceName}"/>
                                    </td>
                                    <td class="visible-xs hidden-print">
                                        <i class="fa fa-fw fa-check" aria-hidden="true"></i>
                                        <uv:number number="${statistic.totalAllowedVacationDays}"/>
                                        <br/>
                                        <i class="fa fa-fw fa-question hidden-print" aria-hidden="true"></i>
                                        <uv:number number="${statistic.totalWaitingVacationDays}"/>
                                    </td>
                                    <td class="hidden-xs hidden-sm">
                                        <spring:message code="applications.statistics.total"/>:
                                        <c:forEach items="${vacationTypes}" var="type">
                                            <br/>
                                            <small>
                                                <spring:message code="${type.messageKey}"/>:
                                            </small>
                                        </c:forEach>
                                    </td>
                                    <td class="hidden-xs hidden-sm number">
                                        <strong class="sortable">
                                            <uv:number
                                                number="${statistic.totalAllowedVacationDays}"/>
                                        </strong>
                                        <spring:message code="duration.days"/>
                                        <c:forEach items="${vacationTypes}" var="type">
                                            <br/>
                                            <small>
                                                <uv:number number="${statistic.allowedVacationDays[type]}"/>
                                            </small>
                                        </c:forEach>
                                    </td>
                                    <td class="hidden-xs hidden-sm number">
                                        <strong class="sortable">
                                            <uv:number
                                                number="${statistic.totalWaitingVacationDays}"/>
                                        </strong>
                                        <spring:message code="duration.days"/>
                                        <c:forEach items="${vacationTypes}" var="type">
                                            <br/>
                                            <small>
                                                <uv:number number="${statistic.waitingVacationDays[type]}"/>
                                            </small>
                                        </c:forEach>
                                    </td>
                                    <td class="hidden-xs">
                                        <strong class="sortable">
                                            <uv:number number="${statistic.leftVacationDays}"/>
                                        </strong>
                                        <spring:message code="duration.vacationDays"/>
                                        <br/>
                                        <strong><uv:number number="${statistic.leftOvertime}"/></strong>
                                        <spring:message code="duration.overtime"/>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>

            </div>

        </div>
        <%-- end of row --%>

    </div>
    <%-- end of container --%>

</div>
<%-- end of content --%>

</body>

</html>


