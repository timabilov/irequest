package com.restnet.irequest.request;

import java.util.HashMap;

/**
 * Created by iRequest on 2/25/2017.
 */
public class BrokenRequestException extends RuntimeException {

    private GenericRequest r;

    public BrokenRequestException(GenericRequest r, Exception e) {
        super(e);
        this.r = r;
    }

    public HashMap<String, String> getArgParams() {
        return r.getQueryParams();
    }

    public HashMap<String, String> getHeaders() {
        return r.getHeaders();
    }

    public String getBody() {
        return r.getBody().toString();
    }


    public Method getMethod() {
        return r.getMethod();
    }

    public GenericRequest repair(){

        return r.repair();

    }


}
