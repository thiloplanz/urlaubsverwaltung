<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html>

<head>
    <uv:head/>
    <script defer src="<spring:url value='/assets/department_form.min.js' />"></script>
</head>

<body>

<uv:menu/>

<spring:url var="URL_PREFIX" value="/web"/>

<div class="content">
    <div class="container">

        <c:choose>
            <c:when test="${department.id == null}">
                <c:set var="ACTION" value="${URL_PREFIX}/department"/>
            </c:when>
            <c:otherwise>
                <c:set var="ACTION" value="${URL_PREFIX}/department/${department.id}"/>
            </c:otherwise>
        </c:choose>

        <form:form method="POST" action="${ACTION}" modelAttribute="department" class="form-horizontal">
        <form:hidden path="id"/>

        <div class="row">

            <div class="form-section">
                <div class="col-xs-12">
                    <legend><spring:message code="department.data"/></legend>
                </div>

                <div class="col-md-4 col-md-push-8">
            <span class="help-block">
                <i class="fa fa-fw fa-info-circle" aria-hidden="true"></i>
                <spring:message code="department.data.description"/>
            </span>
                </div>
                <div class="col-md-8 col-md-pull-4">
                    <div class="form-group is-required">
                        <label class="control-label col-md-3" for="name">
                            <spring:message code='department.data.name'/>:
                        </label>
                        <div class="col-md-9">
                            <form:input id="name" path="name" class="form-control" cssErrorClass="form-control error"/>
                            <span class="help-inline"><form:errors path="name" cssClass="error"/></span>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-3" for="description">
                            <spring:message code='department.data.info'/>:
                        </label>
                        <div class="col-md-9">
                            <span id="text-description"></span><spring:message code='action.comment.maxChars'/>
                            <form:textarea id="description" rows="3" path="description" class="form-control"
                                           cssErrorClass="form-control error"
                                           onkeyup="count(this.value, 'text-description');"
                                           onkeydown="maxChars(this,200); count(this.value, 'text-description');"/>
                            <form:errors path="description" cssClass="error"/>
                        </div>
                    </div>
                </div>

                <div class="col-md-4 col-md-push-8">
            <span class="help-block">
                <i class="fa fa-fw fa-info-circle" aria-hidden="true"></i>
                <spring:message code="department.data.twoStageApproval.help"/>
            </span>
                </div>
                <div class="col-md-8 col-md-pull-4">
                    <div class="form-group">
                        <label class="control-label col-md-3" for="twoStageApproval">
                            <spring:message code="department.data.twoStageApproval"/>:
                        </label>
                        <div class="col-md-9 checkbox">
                            <span class="help-inline"><form:errors path="twoStageApproval" cssClass="error"/></span>
                            <label>
                                <form:checkbox id="twoStageApproval" path="twoStageApproval" cssErrorClass="error"/>
                                <spring:message code="department.data.twoStageApproval.activate"/>
                            </label>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <div class="col-xs-12">
                    <legend><spring:message code="department.members"/></legend>
                </div>

                <c:set var="departmentHeadsError">
                    <form:errors path="departmentHeads" cssClass="error"/>
                </c:set>
                <c:set var="secondStageAuthoritiesError">
                    <form:errors path="secondStageAuthorities" cssClass="error"/>
                </c:set>

                <c:if test="${not empty departmentHeadsError}">
                    <div class="col-xs-12">
                        <div class="alert alert-danger">${departmentHeadsError}</div>
                    </div>
                </c:if>
                <c:if test="${not empty secondStageAuthoritiesError}">
                    <div class="col-xs-12">
                        <div class="alert alert-danger">${secondStageAuthoritiesError}</div>
                    </div>
                </c:if>


                <div class="col-md-4 col-md-push-8">
            <span class="help-block">
                <i class="fa fa-fw fa-info-circle" aria-hidden="true"></i>
                <spring:message code="department.members.description"/>
            </span>
                    <span class="help-block">
                <i class="fa fa-fw fa-info-circle" aria-hidden="true"></i>
                <spring:message code="department.members.secondStageAuthority.description"/>
            </span>
                </div>

                <div class="col-md-8 col-md-pull-4">
                    <div class="form-group">
                        <label class="control-label col-md-3" for="members">
                            <spring:message code='department.members.staff'/>:
                        </label>
                        <div class="col-md-9">
                            <div class="department--members">
                                <c:forEach items="${persons}" var="person">
                                    <c:set var="IS_MEMBER" value="${fn:contains(department.members, person)}"/>
                                    <c:set var="MEMBER_CSS_CLASS" value="${IS_MEMBER ? 'is-assigned' : ''}"/>

                                    <div class="department--member ${MEMBER_CSS_CLASS}">
                                        <div class="department--member-image">
                                            <div class="gravatar gravatar--medium img-circle"
                                                 data-gravatar="<c:out value='${person.gravatarURL}?d=mm&s=40'/>"></div>
                                        </div>
                                        <div class="department--member-assignment">
                                            <p class="department--member-info">
                                                <c:out value="${person.niceName}"/>
                                            </p>
                                            <div class="checkbox">
                                                <label>
                                                    <form:checkbox path="members" value="${person}"/>
                                                    <spring:message code="department.members.assigned"/>
                                                </label>
                                            </div>
                                            <c:if test="${fn:contains(person.permissions, 'DEPARTMENT_HEAD')}">
                                                <div class="checkbox">
                                                    <label>
                                                        <form:checkbox path="departmentHeads" value="${person}"/>
                                                        <spring:message code="department.members.departmentHead"/>
                                                    </label>
                                                </div>
                                            </c:if>
                                            <c:if test="${fn:contains(person.permissions, 'SECOND_STAGE_AUTHORITY')}">
                                                <div class="checkbox">
                                                    <label>
                                                        <form:checkbox path="secondStageAuthorities" value="${person}"/>
                                                        <spring:message code="department.members.secondStageAuthority"/>
                                                    </label>
                                                </div>
                                            </c:if>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <div class="col-xs-12">
                    <hr/>
                    <button class="btn btn-success col-xs-12 col-sm-5 col-md-2" type="submit"><spring:message
                        code="action.save"/></button>
                    <a class="btn btn-default col-xs-12 col-sm-5 col-md-2 pull-right"
                       href="${URL_PREFIX}/department"><spring:message code="action.cancel"/></a>
                </div>
            </div>

            </form:form>

        </div>
    </div>

</body>

</html>
