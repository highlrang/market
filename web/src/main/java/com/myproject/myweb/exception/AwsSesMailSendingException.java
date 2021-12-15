package com.myproject.myweb.exception;

import lombok.Getter;

@Getter
public class AwsSesMailSendingException extends RuntimeException{
    private String msg;

    public AwsSesMailSendingException(){
        super();
    }

    public AwsSesMailSendingException(String msg){
        this.msg = msg;
    }
}
