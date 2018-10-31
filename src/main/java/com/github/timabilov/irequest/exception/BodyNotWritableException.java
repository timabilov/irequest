package com.github.timabilov.irequest.exception;

/**
 * Created by Asus on 2/8/2017.
 */
public class BodyNotWritableException extends Exception {


    public BodyNotWritableException(){

        super("Due to HttpURLConnection architecture and standard robust specification you CANNOT attach body to GET request. Please change it to POST or use different ways.");
    }
}
