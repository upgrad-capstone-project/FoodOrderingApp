package com.upgrad.FoodOrderingApp.api.controller.exception;

import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.upgrad.FoodOrderingApp.api.model.ErrorResponse;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class RestExceptionHandler extends Exception{

    @ExceptionHandler(SignUpRestrictedException.class)
    public ResponseEntity<ErrorResponse> signUpRestrictedExcetion(SignUpRestrictedException exc, WebRequest request){
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exc.getCode()).message(exc.getErrorMessage()), HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> authenticationFailedException(AuthenticationFailedException exc, WebRequest request){
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exc.getCode()).message(exc.getErrorMessage()), HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(AuthorizationFailedException.class)
    public ResponseEntity<ErrorResponse> authorizationFailedExcetion(AuthorizationFailedException exc, WebRequest request){
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exc.getCode()).message(exc.getErrorMessage()), HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ErrorResponse> addressNotFoundException(AddressNotFoundException exc, WebRequest request){
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exc.getCode()).message(exc.getErrorMessage()), HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(SaveAddressException.class)
    public ResponseEntity<ErrorResponse> saveAddressException(SaveAddressException exc, WebRequest request){
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exc.getCode()).message(exc.getErrorMessage()), HttpStatus.BAD_REQUEST
        );
    }





}
