<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<!DOCTYPE html>
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1"/>
        <title>Login</title>
        <link rel="shortcut icon" type="image/x-icon" href="<spring:url value='/favicon.ico?' />" />
        <link rel="stylesheet" href="<spring:url value='/lib/bootstrap/bootstrap-3.3.7.min.css' />">
        <link rel="stylesheet" href="<spring:url value='/lib/font-awesome/css/font-awesome-4.5.0.min.css' />" />
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" />
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/login.css' />" />
        <script src="<spring:url value='/lib/jquery/js/jquery-3.3.1.min.js' />" type="text/javascript" ></script>
        <script src="<spring:url value='/lib/bootstrap/bootstrap-3.3.7.min.js' />" type="text/javascript" ></script>
    </head>

    <body>

        <nav class="navbar navbar-default" role="navigation">
            <div class="container">
                <!-- Brand and toggle get grouped for better mobile display -->
                <div class="navbar-header">
                    <a class="navbar-brand" href="#">
                        <img src="<spring:url value='/images/synyx-logo-transparent.png' />" height="24" width="15" />
                        Urlaubsverwaltung
                    </a>
                </div>
            </div><!-- /.container-fluid -->
        </nav>

        <div class="row">

            <div class="col-xs-12">

                <div class="content">

                    <div class="login">

                        <form method="post" class="login--form" action="/login">
                            <c:if test="${param.login_error != null}">
                                <div id="login--error" class="alert alert-danger">
                                    Der eingegebene Nutzername oder das Passwort ist falsch.
                                </div>
                            </c:if>

                            <div class="form-group">
                                <label for="username">Username</label>
                                <input class="form-control" type="text" name="username" id="username" autofocus="autofocus">
                            </div>

                            <div class="form-group">
                                <label for="password">Passwort</label>
                                <input class="form-control" type="password" name="password" id="password">
                            </div>

                            <div class="form-group">
                                <button class="btn btn-primary btn-block" type="submit">
                                    <i class="fa fa-sign-in"></i> Login
                                </button>
                            </div>

                        </form>

                    </div>

                </div>

            </div>
        </div>

    </body>

    <footer>
        <div class="row">
            <div class="col-xs-12">
                <p>synyx Urlaubsverwaltung Version ${version}</p>
            </div>
        </div>
    </footer>

</html>
