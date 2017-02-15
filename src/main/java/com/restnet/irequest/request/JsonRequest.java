package com.restnet.irequest.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restnet.irequest.exception.BadHTTPStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

/**
 *
 */
public class JsonRequest extends GenericRequest<JsonRequest> {





    protected JsonRequest(GenericRequest r)  {
        super(r.http, r.url, r.method,  r.body, r.name, r.userStreamDecoratorClazz, r.printRawAtTheEnd, r.suppressHttpFail);

        super.header("Content-Type", "application/json");
    }
    protected JsonRequest(String urlRaw, Method method) throws MalformedURLException, IOException {

        super(urlRaw, method);

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

    public JsonRequest body(String content) {
        super.body(content);
        return getThis();
    }

    public JsonRequest with(HashMap<String, Object> json){

        this.params = json;
        return this;
    }


    protected void pack() throws IOException {


        super.pack();

        ObjectMapper mapper = new ObjectMapper();
        String rawJson = mapper.writeValueAsString(params);
        this.body = new StringBuilder(rawJson);

    }

    public JsonRequest param(String key, Object value){

        params.put(key, value);
        return this;

    }

    public HashMap<String, Object> getParams(){

        return this.params;
    }

    @Override
    public String fetch() throws IOException, BadHTTPStatusException {
        return super.fetch();
    }
}
