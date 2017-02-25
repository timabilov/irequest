# iRequest
Thinnest HTTP Client ***based on native HttpURLConnection*** provides you - wraps all functionality in soft manner with fluent-like interface.

This client abstracts you from all boilerplate that you write within native API i.e. Intended to use due to  DRY pain of native API  which many of us encounter's i think) 

 
**DO NOT** supports get requests with body  because of specification and etc.

http://stackoverflow.com/questions/18664413/can-i-do-an-http-get-and-include-a-body-using-httpurlconnection






                    

Stream response to anywhere directly or store after

    Request.get("http://httpbin.org/ip")
            .snapshot() 
            .proxy("27.48.5.68", 8080)
            .pipe(System.out);
            
    Request.get("https://www.google.az/favicon.ico?key=initial")
            .arg("key2", "additional") // url args
            .snapshot() // prints request before send
            .pipe("favicon.ico");

    Request.get("https://www.google.az/")
            .header("User-Agent", "iRequest Agent") // it's default user-agent
            .snapshot() // prints request before send
            .send() // Response object
            .printHeaders()
            .store("google.html");


Fire requests asynchronously : 

    Request.get("http://httpbin.org/headers")
            .async(new ResponseHandler() {

                public void success(Response r) {
                    System.out.println("Async fetch!");
                    System.out.println(r.getBody());
                }

                public void error(BrokenRequestException failedRequest) {
                    System.err.println("Sorry bro");
                    System.out.println(failedRequest.getHeaders());

                }
            });
    
Also you can try re-use the same request.
 
    Request.get("http://httpbin.org/hidden-basic-auth/user/passwd")
            .async(new ResponseHandler() {

                public void success(Response r) {
                    System.out.println(r.getBody());
                }

                public void error(BrokenRequestException failedRequest) {
                    // you have to authorize
                    System.out.println(failedRequest.getHeaders());

                    try {
                        failedRequest.repair()
                                .header("ThisHeaderChangesEverything", "Really")
                                .basicAuth("user", "passwd")
                                .pipe("repaired.json");

                    } catch (Exception e){
                        e.printStackTrace();
                        System.out.println("Damn!");
                    }

                }
            });
            
Use `fetch()` to get result immediately. Also to fetch json use ` fetchJson()` instead. 

    JsonNode json  = Request.get("http://httpbin.org/basic-auth/username/password123")
            .basicAuth("username", "password123")
            .timeout(10) //read and connect timeout
            .fetchJson(); // result as json node

    Request.json("http://httpbin.org:80/put", Method.PUT)
            .param("key", "value")
            .pipe("put.json");

`jsonify()` will adapt your form request with params to json request  
 
     String result = Request.post("http://www.posttestserver.com/post.php")
                 .param("name", "John")
                 .jsonify() // { "name": "John" }
                 .fetch()


Post request built with both form and query params:
 
    Request.post("http://www.posttestserver.com/post.php")
            .header("Header", "Header-Value")
            .param("formParam", "formValue")
            .arg("urlParam", "UrlValue")
            .snapshot() // prints raw request
            .pipe("post.txt");


You can convert post request with converted params from plain form to multipart and json respectively   

    result = Request.post("http://www.posttestserver.com/post.php")
            //.proxy("112.214.73.253", 80)
            .param("phone", "+994XXYYYYYYY")
            .param("id", "123456")
            .param("file", new File("C:/Finish.log")) // Upload file. Implicitly casts to multipart(!).
            .jsonify() // force convert to json request(not multipart anymore) with file translation encoded
            // BASE64 body { ... "file":{"Finish.log":"RmluaXNoIA0K"}}
            .snapshot()
            .fetch();


Proxy can be set locally or globally. Fetch used to get result immediately.

    String result = Request.post("http://www.posttestserver.com/post.php")
            .proxy("27.48.5.68", 8080, true) // you can set one-time like this - and also do like down below
            .saveProxy()  // save this request proxy settings globally until overwritten
            .param("name", "John")
            .jsonify() // previous params also converted to json
            .param("jsonKey", MapUtils.mapOf("nestedKey", "nestedValue"))
            .fetch(); // finally fires and gets result immediately


    Request.get("httpbin.org/ip")
            .pipe(System.out); // Prints last saved proxy


    Request.forgetProxy(); //reset global proxy

    Request.get("httpbin.org/ip")
            .pipe(System.out); // Prints our origin.

Fail silently with bad http codes. No http related exception will be thrown. Instead error body will be considered with response : 

    Request.get("http://httpbin.org/status/fake") // return 500
            .suppressFail()
            .timeout(10)
            .pipe("failed.html");
            
By default handles gzipped and deflated content. You can set your decode provider `withReader(YourInputStream.class)` which used only if none of the available are fit.

    Request.get("http://httpbin.org/gzip")    
            .pipe("gzipped.json");

Log you responses, store somewhere and etc.

    Request.get("http://httpbin.org/response-headers")
            .arg("arg", "argValue")
            .send()
            .dump("log.txt", true) // appends log/dump response metadata
            .store("response.json")
            .printHeaders();

Set body directly. Not allowed with get requests.

    result = Request.post("http://httpbin.org:80/post")
            .cookie("name", "3")
            .body("Native content") 
            .cookie("lang", "en")
            .snapshot()
            .fetch();


By default cookies are validated.

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

      



  



