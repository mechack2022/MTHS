package com.auth.service.exceptions;


public class ApiException extends RuntimeException{

    public ApiException(String message){
        super(message);
    }
    public ApiException(){
    }
}
