package com.restnet.irequest.exception;

/**
 * Created by Asus on 2/8/2017.
 */
public class BadHTTPStatusException extends Exception {



    public BadHTTPStatusException(int status, String data){

        super("GenericRequest returned bad status: " + status + " \n" + data);
    }
}
