package com.github.timabilov.irequest.test.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.timabilov.irequest.exception.BadHTTPStatusException;
import com.github.timabilov.irequest.request.GetRequest;
import com.github.timabilov.irequest.request.Request;
import com.github.timabilov.irequest.request.Response;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by iRequest on 2/17/2017.
 */


public class GenericRequstTest {





    @Test
    public void testCommon() throws IOException, BadHTTPStatusException {

        GetRequest r = Request.get("https://github.com/timabilov/irequest");
        assertTrue(r.getBody().toString().isEmpty());
        assertTrue(!r.isDebug());
        assertTrue(r.getHeaders().size() == 0);

        assertTrue(!r.isSuppressHttpFail());
        assertTrue(!r.isPrintRawAtTheEnd());
        assertTrue(r.getQueryParams().size() == 0);


        Response response = r.send();
        assertEquals(r.getHeaders().get("User-Agent"), "iRequest Agent");
        assertTrue(response.getHeaders().size() > 0);
        assertTrue(response.getBody().length() > 0);
        assertTrue(response.getHttpStatus() > 0);

    }

    @Test
    public void testFetch() throws IOException, BadHTTPStatusException {

        GetRequest r = Request.get("https://github.com/timabilov/irequest");
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

        GetRequest r = Request.get("https://httpbin.org/ip");
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

        GetRequest r = Request.get("https://httpbin.org/headers");
        r.header("Key1", "Value1");
        assertTrue(r.getHeaders().size() == 1);
        r.header("Key1", "Value2");
        assertTrue(r.getHeaders().size() == 1);
        r.header("Key2", "value3");
        assertTrue(r.getHeaders().size() == 2);



    }

    @Test
    public void testRequestHeaderTransfer() throws IOException, BadHTTPStatusException {

        GetRequest r = Request.get("https://httpbin.org/headers");
        r.header("Key1", "Value1");
        r.header("Key1", "Value2");
        r.header("Key2", "Value3");




        JsonNode expectedHeaders = r.fetchJson();

        assertTrue(expectedHeaders.size() > 0);
        assertNotNull(expectedHeaders.get("headers").get("Key1"));
        assertEquals("Value2", expectedHeaders.get("headers").get("Key1").textValue());

        assertNotNull(expectedHeaders.get("headers").get("Key2"));
        assertEquals("Value3", expectedHeaders.get("headers").get("Key2").textValue());
    }




    @Test
    public void testAppendScheme() throws IOException, BadHTTPStatusException {


        GetRequest r = Request.get("httpbin.org/headers");
        assertEquals("http://httpbin.org/headers", r.getUrl());

    }


    @Test
    public void testQueryParams() throws IOException, BadHTTPStatusException {

        GetRequest r = Request.get("https://httpbin.org/get")
                .arg("arg1", "value")
                .arg("arg2", "value2");

        assertTrue(r.getQueryParams().size() == 2);

        JsonNode node = r.fetchJson();
        assertNotNull(node.get("args"));
        assertNotNull(node.get("args").get("arg1"));
        assertNotNull(node.get("args").get("arg2"));
        assertEquals("value", node.get("args").get("arg1").textValue());
        assertEquals("value2", node.get("args").get("arg2").textValue());

    }


    @Test
    public void testInURLQueryParam() throws IOException, BadHTTPStatusException {

        GetRequest r = Request.get("https://httpbin.org/get?a=4")
                .arg("b", "3")
                .arg("arg2", "value2");

        assertTrue(r.getQueryParams().size() == 3);

        JsonNode node = r.fetchJson();
        assertNotNull(node.get("args"));
        assertNotNull(node.get("args").get("b"));
        assertNotNull(node.get("args").get("arg2"));
        assertNotNull(node.get("args").get("a"));
        assertEquals("3", node.get("args").get("b").textValue());
        assertEquals("value2", node.get("args").get("arg2").textValue() );
        assertEquals("4", node.get("args").get("a").textValue() );

        r = Request.get("https://httpbin.org/get?a=4")
                .arg("a", "5");

        assertTrue(r.getQueryParams().size() == 1);


        node = r.fetchJson();
        assertNotNull(node.get("args"));

        assertNotNull(node.get("args").get("a"));

        assertEquals("5", node.get("args").get("a").textValue());

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
        assertEquals("value1", r.get("form").get("param1").textValue());
        assertEquals("value2", r.get("form").get("param2").textValue());


    }




    @Test
    public void testPostMeta() throws IOException, BadHTTPStatusException {

        JsonNode r = Request.post("https://httpbin.org/post")
                .param("param1", "value1")
                .param("param2", "value2")
                .fetchJson();

        JsonNode resultH = r.get("headers");
        assertNotNull(resultH.get("Content-Type"));
        assertEquals("application/x-www-form-urlencoded", resultH.get("Content-Type").textValue() );
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
        assertEquals("4", r.get("form").get("a").textValue());
        assertEquals("2", r.get("form").get("b").textValue());


        assertNotNull(r.get("args").get("a"));
        assertNotNull(r.get("args").get("b"));
        assertEquals("4", r.get("args").get("a").textValue());
        assertEquals("3", r.get("args").get("b").textValue());

    }

    @Test
    public void testEmptyParamGet() throws IOException, BadHTTPStatusException {

        GetRequest request = Request.get("https://httpbin.org/get");
        assertTrue(request.getQueryParams().size() == 0);

        JsonNode r = request.fetchJson();
        assertNotNull(r.get("args").size() == 0);

    }


}
