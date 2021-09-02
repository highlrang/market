package com.myproject.myweb.exception;

import lombok.Getter;

@Getter
public class ItemStockException extends IllegalStateException{
    private String message;
    private String[] args;

    public ItemStockException(){ super(); }

    public ItemStockException(String message, String... args){
        this.message = message;
        if(args.length != 0) this.args = args;
    }

}
