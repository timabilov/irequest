package com.restnet.irequest.test.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.restnet.irequest.exception.BadHTTPStatusException;
import com.restnet.irequest.request.FormRequest;
import com.restnet.irequest.request.Request;
import com.restnet.irequest.request.Response;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by iRequest on 2/17/2017.
 */


public class GenericRequstTest {


    @Before
    public void setUp(){


    }



    @Test
    public void testCommon() throws IOException, BadHTTPStatusException {

        Request r = Request.get("https://github.com/mainstream95/irequest");
        assertTrue(r.getBody().toString().isEmpty());
        assertTrue(!r.isDebug());
        assertTrue(r.getHeaders().size() == 0);

        assertTrue(!r.isSuppressHttpFail());
        assertTrue(!r.isPrintRawAtTheEnd());
        assertTrue(r.getQueryParams().size() == 0);


        Response response = r.send();
        System.out.println(r.getHeaders());
        assertEquals(r.getHeaders().get("User-Agent"), "iRequest Agent");
        assertTrue(response.getHeaders().size() > 0);
        assertTrue(response.getBody().length() > 0);
        assertTrue(response.getHttpStatus() > 0);

    }

    @Test
    public void testFetch() throws IOException, BadHTTPStatusException {

        Request r = Request.get("https://github.com/mainstream95/irequest");
        assertTrue(r.getBody().toString().isEmpty());
        assertTrue(!r.isDebug());
        assertTrue(r.getHeaders().size() == 0);

        assertTrue(!r.isSuppressHttpFail());
        assertTrue(!r.isPrintRawAtTheEnd());
        assertTrue(r.getQueryParams().size() == 0);


        String response = r.fetch();
        assertTrue(response.length() > 0);

    }

    @Test
    public void testFetchJson() throws IOException, BadHTTPStatusException {

        Request r = Request.get("https://httpbin.org/ip");
        assertTrue(r.getBody().toString().isEmpty());
        assertTrue(!r.isDebug());
        assertTrue(r.getHeaders().size() == 0);

        assertTrue(!r.isSuppressHttpFail());
        assertTrue(!r.isPrintRawAtTheEnd());
        assertTrue(r.getQueryParams().size() == 0);


        JsonNode jsonNode = r.fetchJson();
        assertNotNull(jsonNode.get("origin"));

    }

    @Test()
    public void testRequestHeader() throws IOException, BadHTTPStatusException {

        Request r = Request.get("https://httpbin.org/headers");
        r.header("Key1", "Value1");
        assertTrue(r.getHeaders().size() == 1);
        r.header("Key1", "Value2");
        assertTrue(r.getHeaders().size() == 1);
        r.header("Key2", "value3");
        assertTrue(r.getHeaders().size() == 2);



    }

    @Test
    public void testRequestHeaderTransfer() throws IOException, BadHTTPStatusException {

        Request r = Request.get("https://httpbin.org/headers");
        r.header("Key1", "Value1");
        r.header("Key1", "Value2");
        r.header("Key2", "Value3");




        JsonNode expectedHeaders = r.snapshot().fetchJson();

        System.out.println(expectedHeaders);
        assertTrue(expectedHeaders.size() > 0);
        assertNotNull(expectedHeaders.get("headers").get("Key1"));
        assertEquals(expectedHeaders.get("headers").get("Key1").textValue(), "Value2");

        assertNotNull(expectedHeaders.get("headers").get("Key2"));
        assertEquals(expectedHeaders.get("headers").get("Key2").textValue(), "Value3");
    }




    @Test
    public void testAppendScheme() throws IOException, BadHTTPStatusException {


        Request r = Request.get("httpbin.org/headers");
        assertEquals("http://httpbin.org/headers", r.getUrl());

    }


    @Test
    public void testQueryParams() throws IOException, BadHTTPStatusException {

        Request r = Request.get("https://httpbin.org/get")
                .arg("arg1", "value")
                .arg("arg2", "value2");

        assertTrue(r.getQueryParams().size() == 2);

        JsonNode node = r.fetchJson();
        assertNotNull(node.get("args"));
        assertNotNull(node.get("args").get("arg1"));
        assertNotNull(node.get("args").get("arg2"));
        assertEquals(node.get("args").get("arg1").textValue(), "value");
        assertEquals(node.get("args").get("arg2").textValue(), "value2");

    }


    @Test
    public void testInURLQueryParam() throws IOException, BadHTTPStatusException {

        Request r = Request.get("https://httpbin.org/get?a=4")
                .arg("b", "3")
                .arg("arg2", "value2");

        assertTrue(r.getQueryParams().size() == 3);

        JsonNode node = r.fetchJson();
        assertNotNull(node.get("args"));
        assertNotNull(node.get("args").get("b"));
        assertNotNull(node.get("args").get("arg2"));
        assertNotNull(node.get("args").get("a"));
        assertEquals(node.get("args").get("b").textValue(), "3");
        assertEquals(node.get("args").get("arg2").textValue(), "value2");
        assertEquals(node.get("args").get("a").textValue(), "4");

        r = Request.get("https://httpbin.org/get?a=4")
                .arg("a", "5");

        assertTrue(r.getQueryParams().size() == 1);


        node = r.fetchJson();
        assertNotNull(node.get("args"));

        assertNotNull(node.get("args").get("a"));

        assertEquals(node.get("args").get("a").textValue(), "5");

    }

    @Test
    public void testPost() throws IOException, BadHTTPStatusException {

        JsonNode r = Request.post("https://httpbin.org/post")
                .param("param1", "value1")
                .param("param2", "value2")
                .fetchJson();

        assertNotNull(r.get("form"));
        assertNotNull(r.get("form").get("param1"));
        assertNotNull(r.get("form").get("param2"));
        assertEquals(r.get("form").get("param1").textValue(), "value1");
        assertEquals(r.get("form").get("param2").textValue(), "value2");


    }




    @Test
    public void testPostMeta() throws IOException, BadHTTPStatusException {

        JsonNode r = Request.post("https://httpbin.org/post")
                .param("param1", "value1")
                .param("param2", "value2")
                .fetchJson();

        JsonNode resultH = r.get("headers");
        System.out.println(resultH);
        assertNotNull(resultH.get("Content-Type"));
        assertEquals(resultH.get("Content-Type").textValue(), "application/x-www-form-urlencoded");
        assertNotNull(resultH.get("Content-Length"));
        assertTrue(Integer.parseInt(resultH.get("Content-Length").textValue()) > 0);


    }


    @Test
    public void testPostAndGetData() throws IOException, BadHTTPStatusException {

        JsonNode r = Request.post("https://httpbin.org/post?a=4&b=2")
                .param("a", "4")
                .param("b", "2")
                .arg("b", "3")
                .fetchJson();

        assertNotNull(r.get("form").get("a"));
        assertNotNull(r.get("form").get("b"));
        assertEquals(r.get("form").get("a").textValue(), "4");
        assertEquals(r.get("form").get("b").textValue(), "2");


        assertNotNull(r.get("args").get("a"));
        assertNotNull(r.get("args").get("b"));
        assertEquals(r.get("args").get("a").textValue(), "4");
        assertEquals(r.get("args").get("b").textValue(), "3");

    }

    @Test
    public void testEmptyParamGet() throws IOException, BadHTTPStatusException {

        Request request = Request.get("https://httpbin.org/get");
        assertTrue(request.getQueryParams().size() == 0);

        JsonNode r = request.fetchJson();
        assertNotNull(r.get("args").size() == 0);

    }


}
