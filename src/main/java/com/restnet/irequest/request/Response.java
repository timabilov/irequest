package com.restnet.irequest.request;

import com.restnet.irequest.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Response {

    private HashMap<String, String> headers = new HashMap<String, String>();

    private String body;






    Response(Map<String, List<String>> headers, String body){


        for (Map.Entry<String, List<String>> item: headers.entrySet()){

            this.headers.put(item.getKey(), Utils.join(item.getValue(), " "));
        }

        this.body = body;
    }


    public HashMap<String, String> getHeaders() {
        return headers;
    }



    public String getBody() {
        return body;
    }


    public void printHeaders() {

        System.out.print("\n");

        for (Map.Entry<String, String> item: headers.entrySet()){

            System.out.println((item.getKey() == null ? "" : item.getKey() + ": ") + item.getValue());
        }
    }

}
