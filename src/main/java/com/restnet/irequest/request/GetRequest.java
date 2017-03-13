package com.restnet.irequest.request;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by iRequest on 3/9/2017.
 */
public class GetRequest extends GenericRequest<GetRequest> {


    GetRequest(String urlRaw) throws MalformedURLException, IOException {

        // body must not be null because of future conversion to json and etc.
        super(urlRaw, Method.GET);



    }


    @Override
    GetRequest body(String content) {
        return super.body(content);
    }

    @Override
    protected GetRequest getThis() {
        return this;
    }
}
