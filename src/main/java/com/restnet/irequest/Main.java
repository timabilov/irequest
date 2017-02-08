package com.restnet.irequest;

import com.restnet.irequest.exception.BadHTTPStatusException;
import com.restnet.irequest.request.FormRequest;
import com.restnet.irequest.request.Request;
import com.sun.org.apache.regexp.internal.RE;

import java.io.IOException;

/**
 * Created by Asus on 2/8/2017.
 */
public class Main {


    public static void main(String[] args) throws IOException, BadHTTPStatusException {


        String result = Request.get("google.az")
                .proxy("112.214.73.253", 80)
                .fetch();
        String result2 = Request.get("day.az")
                .proxy("60.194.100.51", 80)
                .fetch();
        String result3 = Request.post("http://www.posttestserver.com/post.php")
                .header("ASDF", "AA")
                .param("param1", "value1?~'")
                .fetch();

        System.out.println(result);
        System.out.println(result2);

        System.out.println(result3);

    }
}
