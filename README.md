# iRequest
Thin HTTP Client **based on native HttpURLConnection**  encapsulates, includes  simple routines. Not uses other http lib's, thus **do not** support GET requests with body.

http://stackoverflow.com/questions/18664413/can-i-do-an-http-get-and-include-a-body-using-httpurlconnection



Easy to implement and build without additional http dependencies for your needs.


```java

public class Main{
    
    public static void main(String[] args)  {
    
            try {
                String result = Request.get("google.az")
                        .proxy("112.214.73.253", 80) // enables proxy for this request session
                        .fetch(); // finally fires and gets result
    
                String result2 = Request.get("day.az")
                        .proxy("60.194.100.51", 80)
                        .fetch();
                String example = Request.post("http://www.posttestserver.com/post.php")
                        .header("Header", "Header-Value")
                        .param("formParam", "value1?~'")
                        .fetch();
    
    
                String result4 = Request.post("http://www.posttestserver.com/post.php")
                        .proxy("112.214.73.253", 80) 
                        .param("name", "John")
                        .jsonify() // previous params also converted to json
                        .param("jsonKey", MapUtils.mapOf("nestedKey", "nestedValue"))
                        .saveProxyGlobal()  //save session proxy settings globally until overwritten
                        .fetch();
    
                String result5 = Request.post("http://www.posttestserver.com/post.php")
                        .multipart("UTF-8") // charset
                        //.proxy("112.214.73.253", 80) no need because  previous global settings
                        .param("phone", "+994558272948")
                        .param("id", "170591")
                        .param("file", new File("C:/Finish.log"))
                        .fetch();
    
                JsonNode json  = Request.get("http://httpbin.org/basic-auth/username/password123") //
                        .basicAuth("username", "password123")
                        .timeout(10) //read and connect timeout, maybe destructed with overloaded method.
                        .fetchJson(); // result as json node
    
    
                System.out.println(json.toString());
                
                Request.get("http://httpbin.org/response-headers?key=val") //
                                    .send() // return Response object
                                    .printHeaders(); //print all headers including status line
    
            } catch (IOException ioe){
    
                System.err.println("I/O exception");
                System.err.println(ioe);
            } catch (BadHTTPStatusException bhs) {
    
                System.err.printf(bhs.getMessage());
    
            }
    }
    
}
  



