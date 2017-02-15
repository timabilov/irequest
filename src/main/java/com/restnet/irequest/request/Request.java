package com.restnet.irequest.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restnet.irequest.exception.BadHTTPStatusException;
import com.restnet.irequest.exception.CookieParseException;
import com.restnet.irequest.utils.MapUtils;
import com.restnet.irequest.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *  This class purpose as factory instantiating each type of request. Itself relies on GET request. All options cached due to URL-first manipulating (Cannot change url parameter when connection opened)
 */
public class Request extends GenericRequest<Request> {






    protected Request(String urlRaw) throws MalformedURLException, IOException {

        // body must not be null because of future conversion to json and etc.
        super(urlRaw, Method.GET);



    }


    @Override
    protected Request getThis() {
        return this;
    }

    public static FormRequest post(String urlRaw) throws MalformedURLException, IOException {

        return new FormRequest(urlRaw);
    }


    public static Request get(String urlRaw) throws MalformedURLException, IOException {

        return new Request(urlRaw);
    }




    public static JsonRequest put(String urlRaw) throws MalformedURLException, IOException {

        return new JsonRequest(urlRaw, Method.PUT);
    }


    public static JsonRequest json(String urlRaw, Method method) throws MalformedURLException, IOException {

        return new JsonRequest(urlRaw, method);
    }



}
