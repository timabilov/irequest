# iRequest
Thin HTTP Client **based on native HttpURLConnection**  encapsulates, includes  simple routines. Not uses other http lib's, thus **do not** support GET requests with body.

http://stackoverflow.com/questions/18664413/can-i-do-an-http-get-and-include-a-body-using-httpurlconnection



Easy to implement and build without additional http dependencies for your needs.


```java

import com.fasterxml.jackson.databind.JsonNode;
import com.restnet.irequest.exception.BadHTTPStatusException;
import com.restnet.irequest.exception.BodyNotWritableException;
import com.restnet.irequest.request.Request;
import com.restnet.irequest.request.Response;
import com.restnet.irequest.utils.MapUtils;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class Main {


    public static void main(String[] args)  {

        try {


            Request.get("https://www.google.az/?gws_rd=cr,ssl&ei=FICgWI2yAsXE6QTrhZbYAQ")
                    .header("User-Agent", "iRequest Agent")
                    .snapshot() // prints request snapshot with the current state only. Consider that final stage has own changes to request.
                    .proxy("112.214.73.253", 80) // enables proxy for this request session
                    .send() // returns Response object
                    .printHeaders()
                    .store("google.html"); // store result as html file

            Request.get("day.az")
                    .proxy("60.194.100.51", 80)
                    .send()
                    .printHeaders();


            String result = Request.post("http://www.posttestserver.com/post.php")
                    .proxy("112.214.73.253", 80) 
                    .param("name", "John")
                    .jsonify() // previous params also converted to json
                    .param("jsonKey", MapUtils.mapOf("nestedKey", "nestedValue"))
                    .saveProxy()  //save session proxy settings globally until overwritten
                    .fetch(); // finally fires and gets result immediately

            Request.post("http://www.posttestserver.com/post.php")
                    .header("Header", "Header-Value")
                    .param("formParam", "formValue'")
                    .fetchAndSaveTo("post.json");

            result = Request.post("http://www.posttestserver.com/post.php")
                    //.proxy("112.214.73.253", 80) no need because of  previous global settings
                    .param("phone", "+994XXYYYYYYY")
                    .param("id", "123456")
                    .param("file", new File("C:/Finish.log")) // implicitly casts to multipart(!).
                    .jsonify() // force convert to json request(not multipart anymore) with file translate encoded BASE64 body { ... "file":{"Finish.log":"RmluaXNoIA0K"}}
                    .snapshot() 
                    .fetch();

            JsonNode json  = Request.get("http://httpbin.org/basic-auth/username/password123")
                    .basicAuth("username", "password123")
                    .timeout(10) //read and connect timeout
                    .fetchJson(); // result as json node

            
            json = Request.get("http://httpbin.org/basic-auth/username/password123")
                    .basicAuth("username", "password123")
                    .timeout(10) //read and connect timeout
                    .fetchJson();
            
            Request.json("http://httpbin.org:80/put", Method.PUT)
                    .param("key", "value")
                    .fetchAndSaveTo("put.json");
            


            Request.get("http://httpbin.org/status/fake") // return 500
                    .suppressFail() //  No exception will be thrown at http fail. Instead error body will be considered with response
                    .timeout(10) //read and connect timeout
                    .fetchAndSaveTo("failed.html");
            
            Request.get("http://httpbin.org/gzip")  // also gzipped content encoding is supported
                    .fetchAndSaveTo("gzipped.json");


            Request.get("http://httpbin.org/response-headers?key=val") 
                    .send() //send request and return Response object
                    .dump("log.txt") // dump response metadata
                    .store("response.json") // save response
                    .printHeaders(); 


            result = Request.post("http://httpbin.org:80/post")
                    .cookie("name", "3")
                    .body("Native content")
                    .cookie("lang", "en")
                    .snapshot()
                    .fetch();



            result = Request.get("http://httpbin.org:80/cookies")
                    .header("Cookie","UnparseableCookie;;;") // throws CookieParseException. By default cookie validated and parsed. May be turned off
                    .snapshot()
                    .fetch();



            System.out.printf(result);

        } catch (IOException ioe){

            System.err.println("I/O exception");
            System.err.println(ioe);
        } catch (BadHTTPStatusException bhs) {

            System.err.printf(bhs.getMessage());

        } catch (BodyNotWritableException e) {
            e.printStackTrace();
        }
    }
}

  



