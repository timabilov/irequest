package com.github.timabilov.irequest.request;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 *  This class purpose as factory instantiating each type of request. Itself relies on GET request. All options cached due to URL-first manipulating (Cannot change url parameter when connection opened)
 */
public class Request extends GenericRequest<Request> {






    Request(String urlRaw, Method method) throws MalformedURLException, IOException {

        // body must not be null because of future conversion to json and etc.
        super(urlRaw, method);



    }


    @Override
    protected Request getThis() {
        return this;
    }

    public static PostRequest post(String urlRaw) throws MalformedURLException, IOException {

        return new PostRequest(urlRaw);
    }


    public static GetRequest get(String urlRaw) throws MalformedURLException, IOException {

        return new GetRequest(urlRaw);
    }

    public static Request url(String url, Method method) throws MalformedURLException, IOException {

        return new Request(url, method);
    }

    public static JsonRequest put(String urlRaw) throws MalformedURLException, IOException {

        return new JsonRequest(urlRaw, Method.PUT);
    }


    public static JsonRequest json(String urlRaw, Method method) throws MalformedURLException, IOException {

        return new JsonRequest(urlRaw, method);
    }




    @Override
    public Request body(String content) {
        return super.body(content);
    }
}
