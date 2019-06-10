<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<!DOCTYPE html>
<html>
<head>
    <uv:head/>
    <script defer src="<asset:url value='sick_note_convert.js' />"></script>
</head>
<body>

<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu/>

<div class="content">

    <c:set var="METHOD" value="POST"/>
    <c:set var="ACTION" value="${URL_PREFIX}/sicknote/${sickNote.id}/convert"/>

    <form:form method="${METHOD}" action="${ACTION}" modelAttribute="sickNoteConvertForm" class="form-horizontal">

        <div class="container">

            <div class="row">

                <div class="col-xs-12 col-sm-12 col-md-6">
                    <legend>
                        <spring:message code="sicknote.convert.title"/>
                    </legend>

                    <div class="form-group">
                        <form:hidden path="person" value="${sickNoteConvertForm.person.id}"/>
                        <label class="control-label col-sm-12 col-md-4">
                            <spring:message code='sicknote.data.staff'/>:
                        </label>

                        <div class="col-md-7">
                            <c:out value="${sickNoteConvertForm.person.niceName}"/>
                        </div>
                    </div>

                    <div class="form-group is-required">
                        <label class="control-label col-md-4">
                            <spring:message code="application.data.vacationType"/>:
                        </label>

                        <div class="col-md-7">
                            <form:select path="vacationType" size="1" cssClass="form-control"
                                         cssErrorClass="form-control error">
                                <c:forEach items="${vacationTypes}" var="vacationType">
                                    <option value="${vacationType.id}">
                                        <spring:message code="${vacationType.messageKey}"/>
                                    </option>
                                </c:forEach>
                            </form:select>
                            <span class="help-inline"><form:errors path="vacationType" cssClass="error"/></span>
                        </div>
                    </div>

                    <div class="form-group">
                        <form:hidden path="dayLength"/>
                        <form:hidden path="startDate"/>
                        <form:hidden path="endDate"/>

                        <label class="control-label col-md-4">
                            <spring:message code="absence.period"/>:
                        </label>

                        <div class="col-md-7">
                            <uv:date date="${sickNoteConvertForm.startDate}"/> - <uv:date
                            date="${sickNoteConvertForm.endDate}"/>, <spring:message
                            code="${sickNoteConvertForm.dayLength}"/>
                        </div>
                    </div>

                    <div class="form-group is-required">
                        <label class="control-label col-md-4">
                            <spring:message code="application.data.reason"/>:
                        </label>

                        <div class="col-md-7">
                            <span id="count-chars"></span><spring:message code="action.comment.maxChars"/>
                            <br/>
                            <form:textarea id="reason" path="reason" cssClass="form-control"
                                           cssErrorClass="form-control error" rows="2"
                                           onkeyup="count(this.value, 'count-chars');"
                                           onkeydown="maxChars(this,200); count(this.value, 'count-chars');"/>
                            <span class="help-inline"><form:errors path="reason" cssClass="error"/></span>
                        </div>

                    </div>

                </div>

                <div class="col-xs-12 col-sm-12 col-md-6">

                    <legend>
                        <spring:message code="sicknote.title"/>
                    </legend>

                    <div class="box">
                    <span class="box-icon bg-red">
                        <c:choose>
                            <c:when test="${sickNote.sickNoteType == 'SICK_NOTE_CHILD'}">
                                <i class="fa fa-child" aria-hidden="true"></i>
                            </c:when>
                            <c:otherwise>
                                <i class="fa fa-medkit" aria-hidden="true"></i>
                            </c:otherwise>
                        </c:choose>
                    </span>
                        <span class="box-text">
                        <h5 class="is-inline-block is-sticky"><c:out value="${sickNote.person.niceName}"/></h5>

                        <c:set var="SICK_NOTE_MESSAGEKEY">
                            <spring:message code='${sickNote.sickNoteType.messageKey}'/>
                        </c:set>
                        <spring:message code="sicknotes.details.title" arguments="${SICK_NOTE_MESSAGEKEY}"/>

                        <c:choose>
                            <c:when test="${sickNote.startDate == sickNote.endDate}">
                                <c:set var="SICK_NOTE_DATE">
                                    <h5 class="is-inline-block is-sticky">
                                        <spring:message code="${sickNote.weekDayOfStartDate}.short"/>,
                                        <uv:date date="${sickNote.startDate}"/>
                                    </h5>
                                </c:set>
                                <c:set var="SICK_NOTE_DAY_LENGTH">
                                    <spring:message code="${sickNote.dayLength}"/>
                                </c:set>
                                <spring:message code="absence.period.singleDay"
                                                arguments="${SICK_NOTE_DATE};${SICK_NOTE_DAY_LENGTH}"
                                                argumentSeparator=";"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="SICK_NOTE_START_DATE">
                                    <h5 class="is-inline-block is-sticky">
                                        <spring:message code="${sickNote.weekDayOfStartDate}.short"/>,
                                        <uv:date date="${sickNote.startDate}"/>
                                    </h5>
                                </c:set>
                                <c:set var="SICK_NOTE_END_DATE">
                                    <h5 class="is-inline-block is-sticky">
                                        <spring:message code="${sickNote.weekDayOfEndDate}.short"/>,
                                        <uv:date date="${sickNote.endDate}"/>
                                    </h5>
                                </c:set>
                                <spring:message code="absence.period.multipleDays"
                                                arguments="${SICK_NOTE_START_DATE};${SICK_NOTE_END_DATE}"
                                                argumentSeparator=";"/>
                            </c:otherwise>
                        </c:choose>
                    </span>
                    </div>

                    <table class="list-table striped-table bordered-table">
                        <tbody>
                        <tr>
                            <td>
                                <spring:message code="absence.period.duration"/>
                            </td>
                            <td>
                                = <uv:number number="${sickNote.workDays}"/> <spring:message
                                code="duration.workDays"/>
                            </td>
                        </tr>
                        <tr>
                            <td><spring:message code="sicknote.data.aub.short"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${sickNote.aubPresent}">
                                        <i class="fa fa-check hidden-print" aria-hidden="true"></i>
                                        <uv:date date="${sickNote.aubStartDate}"/> - <uv:date
                                        date="${sickNote.aubEndDate}"/>
                                    </c:when>
                                    <c:otherwise>
                                        <i class="fa fa-remove hidden-print" aria-hidden="true"></i>
                                        <spring:message code="sicknote.data.aub.notPresent"/>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>

            </div>

            <div class="row">

                <div class="col-xs-12">

                    <hr/>

                    <button class="btn btn-success col-xs-12 col-sm-5 col-md-2" type="submit"><spring:message
                        code="action.save"/></button>
                    <a class="btn btn-default col-xs-12 col-sm-5 col-md-2 pull-right"
                       href="${URL_PREFIX}/sicknote/${sickNote.id}"><spring:message code="action.cancel"/></a>


                </div>

            </div>

        </div>

    </form:form>
</div>

</body>
</html>
