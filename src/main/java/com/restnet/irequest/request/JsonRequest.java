package com.restnet.irequest.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restnet.irequest.exception.BadHTTPStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

/**
 *
 */
public class JsonRequest extends GenericRequest<JsonRequest> {


    HashMap<String, Object> json = new HashMap<String, Object>();


    protected JsonRequest(GenericRequest r){
        super(r.http, r.url, r.method,  r.body);

        super.header("Content-Type", "application/json");
    }
    protected JsonRequest(String urlRaw) throws MalformedURLException, IOException {

        super(urlRaw, Method.POST);

        super.header("Content-Type", "application/json");
    }


    @Override
    protected JsonRequest getThis() {
        return this;
    }

    @Override
    public JsonRequest header(String key, String value) {
        // ignore content types because based upon
        if (key.equals("Content-Type"))
            return this;
        super.header(key, value);
        return this;
    }


    public JsonRequest with(HashMap<String, Object> json){

        this.json = json;
        return this;
    }


    private void buildBody() throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String rawJson = mapper.writeValueAsString(json);
        this.body = new StringBuilder(rawJson);


    }

    public JsonRequest param(String key, Object value){

        json.put(key, value);
        return this;

    }


    @Override
    public String fetch() throws IOException, BadHTTPStatusException {
        buildBody();
        return super.fetch();
    }
}
