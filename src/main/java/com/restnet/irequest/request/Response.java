package com.restnet.irequest.request;

import com.restnet.irequest.utils.MapUtils;
import com.restnet.irequest.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
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
     * @throws IOException
     */
    public Response dump(String fileName) throws IOException {

        FileOutputStream fos = new FileOutputStream(new File(fileName), true);

        Utils.write(fos, requestName.concat("\n\n").concat(rawHeaders()).concat("\n"));
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

        Utils.write(fos, body);
        return this;
    }

    public int getHttpStatus() {
        return httpStatus;
    }




    public Response printHeaders() {

        System.out.print("\n");

        System.out.println(rawHeaders());
        return this;
    }


    String rawHeaders(){

        return MapUtils.join(headers,"\n", ": ");
    }

}
