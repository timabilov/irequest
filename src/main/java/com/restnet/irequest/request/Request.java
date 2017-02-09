package com.restnet.irequest.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restnet.irequest.exception.BadHTTPStatusException;
import com.restnet.irequest.utils.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

/**
 *
 */
public class Request extends GenericRequest<Request> {






    protected Request(String urlRaw, Method method) throws MalformedURLException, IOException {

        super(urlRaw, method);

    }


    @Override
    protected Request getThis() {
        return this;
    }

    public static FormRequest post(String urlRaw) throws MalformedURLException, IOException {

        return new FormRequest(urlRaw);
    }


    public static Request get(String urlRaw) throws MalformedURLException, IOException {

        return new Request(urlRaw, Method.GET);
    }




}
