<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="id" type="java.lang.String" required="true" %>
<%@attribute name="actionUrl" type="java.lang.String" required="true" %>

<c:set var="FILTER_DATE_PATTERN">
    <spring:message code="pattern.date"/>
</c:set>

<div id="${id}" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="filterModalLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-remove"></i></button>
                <h4 id="filterModalLabel" class="modal-title"><spring:message code="filter.title"/></h4>
            </div>
            <form:form method="POST" action="${actionUrl}" modelAttribute="period" class="form-horizontal">
                <div class="modal-body">
                    <div class="form-group is-required">
                        <label class="control-label col-md-3" for="startDate">
                            <spring:message code="filter.period.startDate" />:
                        </label>
                        <div class="col-md-9">
                            <form:input id="startDate" path="startDate" class="form-control" cssErrorClass="form-control error" placeholder="${FILTER_DATE_PATTERN}" />
                            <span class="help-block"></span>
                        </div>
                    </div>
                    <div class="form-group is-required">
                        <label class="control-label col-md-3" for="endDate">
                            <spring:message code="filter.period.endDate" />:
                        </label>
                        <div class="col-md-9">
                            <form:input id="endDate" path="endDate" class="form-control" cssErrorClass="form-control error" placeholder="${FILTER_DATE_PATTERN}" />
                            <span class="help-block"></span>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-primary is-sticky" type="submit">
                        <spring:message code="action.confirm"/>
                    </button>
                </div>
            </form:form>
        </div>
    </div>
</div>

<script src="<spring:url value='/lib/date-de-DE-1.0-Alpha-1.js' />" type="text/javascript"></script>
<script src="<spring:url value='/js/datepicker.js' />" type="text/javascript"></script>

<script type="text/javascript">
    $(document).ready(function () {
        var locale = "${pageContext.response.locale.language}";
        $.datepicker.setDefaults($.datepicker.regional[locale]);
        $('#startDate').datepicker();
        $('#endDate').datepicker();
    });
</script>
