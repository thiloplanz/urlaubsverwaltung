<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<spring:url var="URL_PREFIX" value="/web"/>

<p class="text-right visible-print">
    <spring:message code="filter.validity"/> <uv:date date="${now}"/>
</p>

<div id="users">

    <form class="form-inline" id="search">
        <div class="form-group">
            <div class="input-group search-field">
                <%-- NOTE: class 'search' is needed for list.js --%>
                <input type="text" class="search form-control" placeholder="<spring:message code='action.search'/>"/>
                <span class="input-group-addon"><i class="fa fa-search" aria-hidden="true"></i></span>
            </div>
        </div>
    </form>

    <table class="list-table selectable-table sortable tablesorter">
        <thead class="hidden-xs hidden-sm">
        <tr>
            <th scope="col"><%-- placeholder to ensure correct number of th --%></th>
            <th scope="col" class="sortable-field"><spring:message code="person.data.firstName"/></th>
            <th scope="col" class="sortable-field"><spring:message code="person.data.lastName"/></th>
            <th scope="col" class="sortable-field is-centered"><spring:message
                code='persons.account.vacation.entitlement.year'/></th>
            <th scope="col" class="sortable-field is-centered"><spring:message
                code='persons.account.vacation.entitlement.actual'/></th>
            <th scope="col" class="sortable-field is-centered"><spring:message
                code='persons.account.vacation.entitlement.remaining'/></th>
            <th scope="col" class="sortable-field is-centered"><spring:message
                code="persons.account.vacation.vacationDaysLeft"/></th>
            <sec:authorize access="hasAuthority('OFFICE')">
                <th scope="col"><%-- placeholder to ensure correct number of th --%></th>
            </sec:authorize>
        </tr>
        </thead>
        <%-- NOTE: class 'list' is needed for list.js --%>
        <tbody class="list">
        <c:forEach items="${persons}" var="person" varStatus="loopStatus">
            <tr onclick="navigate('${URL_PREFIX}/person/${person.id}/overview');">
                <td class="is-centered">
                    <div class="gravatar img-circle hidden-print"
                         data-gravatar="<c:out value='${person.gravatarURL}?d=mm&s=60'/>"></div>
                </td>
                <td class="firstname">
                    <c:out value="${person.firstName}"/>
                </td>
                <td class="lastname">
                    <c:choose>
                        <c:when test="${person.firstName == null && person.lastName == null}">
                            <c:out value="${person.username}"/>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${person.lastName}"/>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="is-centered hidden-xs hidden-sm">
                    <c:choose>
                        <c:when test="${accounts[person] != null}">
                            <uv:number number="${accounts[person].annualVacationDays}"/>
                        </c:when>
                        <c:otherwise>
                            <spring:message code='person.account.vacation.noInformation'/>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="is-centered hidden-xs hidden-sm">
                    <c:choose>
                        <c:when test="${accounts[person] != null}">
                            <uv:number number="${accounts[person].vacationDays}"/>
                        </c:when>
                        <c:otherwise>
                            <spring:message code='person.account.vacation.noInformation'/>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="is-centered hidden-xs hidden-sm">
                    <c:choose>
                        <c:when test="${accounts[person] != null}">
                            <uv:number number="${accounts[person].remainingVacationDays}"/>
                        </c:when>
                        <c:otherwise>
                            <spring:message code='person.account.vacation.noInformation'/>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="is-centered hidden-xs hidden-sm">
                    <c:choose>
                        <c:when test="${vacationDaysLeftMap[person] != null}">
                            <uv:number number="${vacationDaysLeftMap[person].vacationDays}"/>
                            <c:choose>
                                <c:when test="${beforeApril}">
                                    +
                                    <uv:number number="${vacationDaysLeftMap[person].remainingVacationDays}"/>
                                </c:when>
                                <c:otherwise>
                                    +
                                    <uv:number
                                        number="${vacationDaysLeftMap[person].remainingVacationDaysNotExpiring}"/>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <spring:message code='person.account.vacation.noInformation'/>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="hidden-print hidden-xs">
                    <a class="fa-action pull-right" href="${URL_PREFIX}/person/${person.id}"
                       data-title="<spring:message code="action.details" />">
                        <i class="fa fa-fw fa-list-alt" aria-hidden="true"></i>
                    </a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
