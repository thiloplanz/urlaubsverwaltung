<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="year" type="java.lang.String" required="true" %>
<%@attribute name="hrefPrefix" type="java.lang.String" required="true" %>
<%@attribute name="onclick" type="java.lang.String" required="false" %>

<%
    int currentYear = java.time.LocalDate.now().getYear();
    java.util.List<Integer> yearsToSelect = new java.util.ArrayList<>();
    for (int i=currentYear + 1; i>currentYear-10; i--){
        yearsToSelect.add(i);
    }
    jspContext.setAttribute("yearsToSelect", yearsToSelect);
    jspContext.setAttribute("labelId", java.util.UUID.randomUUID());
%>


<div class="legend-dropdown dropdown">
    <a id="dropdownLabel_${labelId}" class="dropdownLabel" data-target="#" href="#" data-toggle="dropdown"
       aria-haspopup="true" role="button" aria-expanded="false">
        <c:out value="${year}" /><span class="caret"></span>
    </a>

    <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownLabel_${labelId}">
         <c:forEach items="${yearsToSelect}" var="year">
             <c:choose>
                <c:when test="${!empty onclick}">
                    <li><a href="${hrefPrefix}${year}" onclick="${onclick}">${year}</a></li>
                </c:when>
                <c:otherwise>
                   <li><a href="${hrefPrefix}${year}" >${year}</a></li>
                </c:otherwise>
            </c:choose>
         </c:forEach>
    </ul>
</div>