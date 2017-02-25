package com.restnet.irequest.request;

/**
 * Created by iRequest on 2/25/2017.
 */
public abstract class ResponseHandler {




    abstract public void success(Response r);
    abstract public void error(BrokenRequestException failedRequest);
}
