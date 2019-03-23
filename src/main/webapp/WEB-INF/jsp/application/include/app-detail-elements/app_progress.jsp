<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:if test="${not empty comments}">

    <legend>
        <spring:message code="application.progress.title"/>
    </legend>

    <table class="list-table striped-table bordered-table">
        <tbody>

        <c:forEach items="${comments}" var="comment">
            <tr>
                <td class="hidden-print">
                    <div class="gravatar gravatar--medium img-circle hidden-print center-block"
                         data-gravatar="<c:out value='${comment.person.gravatarURL}?d=mm&s=40'/>"></div>
                </td>
                <td>
                    <c:out value="${comment.person.niceName}"/>
                </td>
                <td>
                    <spring:message code="application.progress.${comment.action}"/>

                    <c:choose>
                        <c:when test="${comment.action == 'APPLIED'}">
                            <uv:date date="${application.applicationDate}"/>
                        </c:when>
                        <c:when
                            test="${comment.action == 'ALLOWED' || comment.action == 'TEMPORARY_ALLOWED' || comment.action == 'REJECTED' || comment.action == 'CONVERTED' || comment.action == 'CANCEL_REQUESTED'}">
                            <uv:date date="${application.editedDate}"/>
                        </c:when>
                        <c:when test="${comment.action == 'CANCELLED' || comment.action == 'REVOKED'}">
                            <uv:date date="${application.cancelDate}"/>
                        </c:when>
                    </c:choose>

                    <c:if test="${comment.text != null && not empty comment.text}">
                        <spring:message code="application.progress.comment"/>
                        <br/>
                        <em><c:out value="${comment.text}"/></em>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</c:if>
