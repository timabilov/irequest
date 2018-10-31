package com.github.timabilov.irequest.exception;

/**
 * Created by Asus on 2/8/2017.
 */
public class BadHTTPStatusException extends Exception {

    int status;
    String data;

    public BadHTTPStatusException(int status, String data){

        super("Request returned bad status: " + status + " \n" + data);

        this.status = status;
        this.data = data;
    }


    public String getData() {
        return data;
    }



    public int getStatus() {
        return status;
    }


}
