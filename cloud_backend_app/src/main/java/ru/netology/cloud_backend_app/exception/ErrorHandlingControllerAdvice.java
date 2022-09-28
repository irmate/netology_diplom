package ru.netology.cloud_backend_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import ru.netology.cloud_backend_app.dto.response.ErrorResponse;

import javax.validation.ConstraintViolationException;
import java.util.Random;

@RestControllerAdvice
public class ErrorHandlingControllerAdvice {

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadCredentialsException() {
        return new ErrorResponse("Bad credentials", new Random().nextInt(1000));
    }

    @ExceptionHandler({
            MissingServletRequestPartException.class,
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class,
            MultipartException.class,
            ConstraintViolationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleErrorInputDataException() {
        return new ErrorResponse("Error input data", new Random().nextInt(1000));
    }

    @ExceptionHandler(ContentNotFoundException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleServerErrorException(ContentNotFoundException e) {
        return new ErrorResponse(e.getMessage(), new Random().nextInt(1000));
    }
}