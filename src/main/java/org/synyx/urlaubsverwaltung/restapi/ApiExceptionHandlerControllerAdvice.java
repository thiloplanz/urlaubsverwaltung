package org.synyx.urlaubsverwaltung.restapi;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


/**
 * Handles exceptions and redirects to error page.
 */
@ControllerAdvice(basePackages = "org.synyx.urlaubsverwaltung.restapi")
public class ApiExceptionHandlerControllerAdvice {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({ NumberFormatException.class, IllegalArgumentException.class })
    @ResponseBody
    public ErrorResponse handleException(IllegalArgumentException exception) {

        return new ErrorResponse(BAD_REQUEST, exception);
    }


    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ErrorResponse handleException(MethodArgumentTypeMismatchException exception) {

        return new ErrorResponse(BAD_REQUEST, exception);
    }


    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ErrorResponse handleException(MissingServletRequestParameterException exception) {

        return new ErrorResponse(BAD_REQUEST, exception);
    }


    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ErrorResponse handleException(AccessDeniedException exception) {

        return new ErrorResponse(HttpStatus.FORBIDDEN, exception);
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorResponse handleException(Exception exception) {

        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }
}
