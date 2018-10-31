package com.github.timabilov.irequest.request;

import com.github.timabilov.irequest.utils.MapUtils;
import com.github.timabilov.irequest.utils.Utils;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Response {

    private HashMap<String, String> headers = new HashMap<String, String>();

    private String body;

    private int httpStatus;


    private String requestName;




    Response(Map<String, List<String>> headers, String body, int httpStatus, String requestName){

        this.httpStatus = httpStatus;
        this.requestName = requestName;

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


    /**
     * Dump response meta to file.
     * @param fileName
     * @param append
     * @throws IOException
     */
    public Response dump(String fileName, boolean append) throws IOException {

        FileOutputStream fos = new FileOutputStream(new File(fileName), append);

        Utils.write(fos, new ByteArrayInputStream(requestName.concat(new Date().toString()).concat("\n\n").concat(rawHeaders()).concat("\n").getBytes("UTF-8")));
        return this;
    }


    /**
     * Store response as a file.
     * @param fileName
     * @return
     * @throws IOException
     */

    public Response store(String fileName) throws IOException {


        FileOutputStream fos = new FileOutputStream(fileName);

        Utils.write(fos, new ByteArrayInputStream(body.getBytes("UTF-8")));
        return this;
    }

    public int getHttpStatus() {
        return httpStatus;
    }




    public Response printHeaders() {


        System.out.print(new Date().toString() + "   <<<\n\n");
        System.out.println(rawHeaders());
        return this;
    }


    String rawHeaders(){

        return MapUtils.join(headers,"\n", ": ");
    }

}
