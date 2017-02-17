package com.restnet.irequest.test.request;

import com.restnet.irequest.exception.BadHTTPStatusException;
import com.restnet.irequest.request.Request;
import com.restnet.irequest.request.Response;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by iRequest on 2/17/2017.
 */


public class RequstTest {


    @Before
    public void setUp(){


    }



    @Test
    public void commonTest() throws IOException, BadHTTPStatusException {

        Request r = Request.get("https://github.com/mainstream95/irequest");
        assertTrue(r.getBody().toString().isEmpty());
        assertTrue(!r.isDebug());
        assertTrue(r.getHeaders().size() == 0);
        r.header("header", "value");
        assertTrue(r.getHeaders().size() == 2);

        assertTrue(!r.isSuppressHttpFail());
        assertTrue(!r.isPrintRawAtTheEnd());
        assertTrue(r.getQueryParams().size() == 0);
        r.arg("queryParam", "queryValue");
        assertTrue(r.getQueryParams().size() == 1);

        Response response = r.send();

        assertTrue(response.getHeaders().size() > 0);
        assertTrue(response.getBody().length() > 0);
        assertTrue(response.getHttpStatus() > 0);

    }
}
