package com.restnet.irequest.request;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restnet.irequest.exception.BadHTTPStatusException;
import com.restnet.irequest.exception.BodyNotWritableException;
import com.restnet.irequest.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Request extends RequestConfig {


    protected HttpURLConnection http;
    protected String url;

    protected Method method;

    protected StringBuilder body = new StringBuilder();

    boolean debug;

    public Request(String urlRaw, Method method) throws MalformedURLException, IOException { // explicit pointing malform for  readability

        this.url = urlRaw;
        this.method = method;
        if (!urlRaw.replace("s", "").startsWith("http://") ){ // drop s from https
            urlRaw = "http://" + urlRaw;
        }
        URL url = new URL(urlRaw);

        http = ((HttpURLConnection)url.openConnection());
        if (method == Method.POST)
            http.setDoOutput(true);
        http.setDoInput(true); // just explicit
        http.setUseCaches(false);
        http.setRequestProperty("User-Agent" , "JavaCode Agent");
        http.setRequestMethod("GET");

    }


    public static Request get(String urlRaw) throws MalformedURLException, IOException {

        return new Request(urlRaw, Method.GET);
    }


    public static FormRequest post(String urlRaw) throws MalformedURLException, IOException {

        return new FormRequest(urlRaw);
    }



    public Request header(String key, String value){

        http.setRequestProperty(key, value);
        return this;
    }


    public Request basicAuth(String username, String password){

        String credentialsEncoded = Utils.encodeBASE64(username.concat(":").concat(password));
        return header("Authorization", "Basic ".concat(credentialsEncoded));
    }


    /**
     * This method intended to setting proxy with basic(!) auth , java.net.authenticator  and system properties. Anyway explicit setting  system properties  recommended. For SOCKS proxy applies the same(set explicit)  except - basic auth is not needed.
     * @param username
     * @param password
     * @return
     */
    public Request proxy(String host, int port, final String username, final String password, boolean isSOCKS){

        this.setJVMProxyServer(host, port, isSOCKS);
        String credentialsEncoded = Utils.encodeBASE64(username.concat(":").concat(password));

//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(InetAddress.getByAddress()))
        Authenticator.setDefault(
            new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            username, password.toCharArray());
                }
            }
        );
        return header("Proxy-Authorization", "Basic ".concat(credentialsEncoded))
                .header("Proxy-Connection", "keep-alive");

    }


    public Request proxy(String host, int port){

        this.setJVMProxyServer(host, port, false);


        return this;
    }


    final public Request body(String content) throws BodyNotWritableException {
        if (method == Method.GET)
            throw new BodyNotWritableException();

        this.body = new StringBuilder(content);
        return this;
    }


    public Request readTimeout(int sec){

        http.setReadTimeout(1000*sec);

        return this;

    }

    public Request connectTimeout(int sec){

        http.setConnectTimeout(1000*sec);

        return this;

    }

    public Request timeout(int sec){

        return connectTimeout(sec).readTimeout(sec);
    }


    public String fetch() throws IOException, BadHTTPStatusException {

        header("Content-Length", body.length() + "");
        if (http.getDoOutput())
            Utils.write(http.getOutputStream(), body.toString());

        int status = http.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            String responseBody = Utils.read(http.getInputStream());
            return responseBody;

        } else {
            String errorData = Utils.read(http.getErrorStream());
            throw new BadHTTPStatusException(status, errorData);
        }

    }


    public JsonNode fetchJson() throws IOException, BadHTTPStatusException {

        String raw = fetch();

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(raw,JsonNode.class);

    }


    public <T> T fetchJson(Class<T> type) throws IOException, BadHTTPStatusException {

        String raw = fetch();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(raw, type);

    }

}
