package com.restnet.irequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.restnet.irequest.exception.BadHTTPStatusException;
import com.restnet.irequest.exception.CookieParseException;
import com.restnet.irequest.request.Method;
import com.restnet.irequest.request.Request;
import com.restnet.irequest.request.Response;
import com.restnet.irequest.request.ResponseHandler;
import com.restnet.irequest.utils.MapUtils;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class Main {


    public static void main(String[] args)  {

        try {



            Request.get("http://httpbin.org/headers")
                    .then(new ResponseHandler() {
                        public void handle(Response r) {
                            System.out.println("Async fetch!");
                            System.out.println(r.getBody());
                        }
                    });

            Request.get("https://www.google.az/favicon.ico?key=initial")
                    .arg("key2", "additional") // url args
                    .snapshot() // prints request before send
                    .pipe("favicon.ico");


            Request.get("https://www.google.az/")
                    .header("User-Agent", "iRequest Agent") // it's default user-agent
                    .snapshot()
                    .send() // Response object
                    .printHeaders()
                    .store("google.html");



            Request.get("http://httpbin.org/ip")
                    .snapshot() // prints request before send
                    .proxy("27.48.5.68", 8080)
                    .pipe(System.out);



            String result = Request.post("http://www.posttestserver.com/post.php")
                    .proxy("27.48.5.68", 8080, true) // you can save like this - or down below
                    .saveProxy()  // save session proxy settings globally until overwritten
                    .param("name", "John")
                    .jsonify() // previous params also converted to json
                    .param("jsonKey", MapUtils.mapOf("nestedKey", "nestedValue"))
                    .fetch(); // finally fires and gets result immediately


            Request.get("httpbin.org/ip")
                    .pipe(System.out); // Prints last saved proxy


            Request.forgetProxy(); //reset global proxy


            Request.get("httpbin.org/ip")
                    .pipe(System.out); // Prints our origin.

            Request.post("http://www.posttestserver.com/post.php")
                    .header("Header", "Header-Value")
                    .param("formParam", "formValue")
                    .arg("urlParam", "UrlValue")
                    .snapshot()
                    .pipe("post.txt");

            result = Request.post("http://www.posttestserver.com/post.php")
                    //.proxy("112.214.73.253", 80)
                    .param("phone", "+994XXYYYYYYY")
                    .param("id", "123456")
                    .param("file", new File("C:/Finish.log")) // Upload file. Implicitly casts to multipart(!).
                    .jsonify() // force convert to json request(not multipart anymore) with file translation encoded
                    // BASE64 body { ... "file":{"Finish.log":"RmluaXNoIA0K"}}
                    .snapshot()
                    .fetch();

            JsonNode json  = Request.get("http://httpbin.org/basic-auth/username/password123")
                    .basicAuth("username", "password123")
                    .timeout(10) //read and connect timeout
                    .fetchJson(); // result as json node

            Request.json("http://httpbin.org:80/put", Method.PUT)
                    .param("key", "value")
                    .pipe("put.json");



            Request.get("http://httpbin.org/status/fake") // return 500
                    .suppressFail() //  Fail silently with bad http codes. No exception will be thrown. Instead error body will be considered with response
                    .timeout(10)
                    .pipe("failed.html");

            Request.get("http://httpbin.org/gzip")  // supports gzipped content
                    .pipe("gzipped.json");



            Request.get("http://httpbin.org/response-headers")
                    .arg("arg", "argValue")
                    .send()
                    .dump("log.txt", true) // appends log/dump response metadata
                    .store("response.json")
                    .printHeaders();


            result = Request.post("http://httpbin.org:80/post")
                    .cookie("name", "3")
                    .body("Native content") // Not allowed with get requests.
                    .cookie("lang", "en")
                    .snapshot()
                    .fetch();

            try {
                result = Request.get("http://httpbin.org:80/cookies")
                        .header("Cookie", "UnparseableCookie;;;") // throws CookieParseException. By default validated.
                        .snapshot()
                        .fetch();
            } catch (CookieParseException cpe){

                cpe.printStackTrace();
            }

            Request.skipCookieValidation(true);

            result = Request.get("http://httpbin.org:80/cookies")

                    .header("Cookie", "UnparseableCookie;;;") // now allowed
                    .snapshot()
                    .fetch();

            System.out.println(result);

        } catch (IOException ioe){

            System.err.println("I/O exception!");
            System.err.println(ioe);
        } catch (BadHTTPStatusException bhs) {

            System.err.printf(bhs.getMessage());

        }
    }
}
