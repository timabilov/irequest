package com.restnet.irequest.request;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restnet.irequest.exception.BadHTTPStatusException;
import com.restnet.irequest.exception.BodyNotWritableException;
import com.restnet.irequest.exception.ProxyAuthorizationRequiredException;
import com.restnet.irequest.utils.Utils;

import java.io.IOException;
import java.net.*;
import java.util.*;

abstract class GenericRequest<T extends GenericRequest> {


    protected HttpURLConnection http;
    protected String url;

    protected Method method;

    protected StringBuilder body = new StringBuilder();

    boolean debug;

    protected static Set<InetSocketAddress> proxies;

    // just for tracking
    HashMap<String, String> headers = new HashMap<String, String>();

    private static boolean isProxyGlobal = false;

    private boolean attemptProxiesIfFail = false;
    protected abstract T getThis();


    /**
     *  Needed for only conversion to Childs with already initted config's (HttpURLConnection)
     */

    protected GenericRequest(HttpURLConnection http, String url, Method method, StringBuilder body){


        this.http = http;
        this.url = url;
        this.method = method;
        this.body = body;

        if (!isProxyGlobal)
            setJVMProxyServer("", 0, false); // reset

    }

    /**
     * used for super instantiating , cloning Request instance
     * @param http
     * @param url
     * @param method
     * @param body
     * @param headers
     */
    protected GenericRequest(HttpURLConnection http, String url, Method method, StringBuilder body, HashMap<String, String> headers){


        this.http = http;
        this.url = url;
        this.method = method;
        this.body = body;
        this.headers = headers;

        if (!isProxyGlobal)
            setJVMProxyServer("", 0, false); // reset

    }

    protected GenericRequest(String urlRaw, Method method) throws MalformedURLException, IOException { // explicit pointing malform for  readability


        this(null, urlRaw, method, new StringBuilder());

        if (!urlRaw.replace("s", "").startsWith("http://") ){ // drop s from https
            urlRaw = "http://" + urlRaw;
        }

        URL url = new URL(urlRaw);

        http = ((HttpURLConnection)url.openConnection());
        if (! (method == Method.GET))
            http.setDoOutput(true);
        http.setDoInput(true); // just explicit
        http.setUseCaches(false);
        http.setRequestProperty("User-Agent" , "CodeJava Agent");
        http.setRequestMethod(method.name());

    }



    public static void setJVMProxyServer(String host, int port, boolean isSOCKS){

        if (!isSOCKS) {
            System.setProperty("http.proxyHost", host);
            System.setProperty("http.proxyPort", port + "");
            System.setProperty("https.proxyHost", host);
            System.setProperty("https.proxyPort", port + "");
        } else {

            System.setProperty("socksProxyHost", host);
            System.setProperty("SocksProxyPort", port + "");

        }

    }

    public static void setProxyList(Collection<InetSocketAddress> proxyList){

        proxies = new HashSet<InetSocketAddress>(proxyList);

    }


    // TODO convert formparams to hashmap
    public JsonRequest jsonify(){

        return new JsonRequest(this);
    }



    final public T body(String content) throws BodyNotWritableException {
        if (method == Method.GET)
            throw new BodyNotWritableException();

        this.body = new StringBuilder(content);
        return getThis();
    }

    public T  header(String key, String value){

        http.setRequestProperty(key, value);
        headers.put(key, value);
        return getThis();
    }


    public T basicAuth(String username, String password){

        String credentialsEncoded = Utils.encodeBASE64(username.concat(":").concat(password));
        return header("Authorization", "Basic ".concat(credentialsEncoded));
    }


    public T saveProxyGlobal(){

        isProxyGlobal = true;
        return getThis();
    }

    /**
     * This method intended to setting proxy with basic(!) auth , java.net.authenticator  and system properties. Anyway explicit setting  system properties  recommended. For SOCKS proxy applies the same(set explicit)  except - basic auth is not needed.
     * @param username
     * @param password
     * @return
     */
    public T proxy(String host, int port, final String username, final String password, boolean isSOCKS){

        // this.setJVMProxyServer(host, port, isSOCKS);


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

        return proxyAuth(username, password);

    }




    public T proxy(String host, int port){

        setJVMProxyServer(host, port, false);


        return getThis();
    }


    public T proxyAuth(String username, String password){

        String credentialsEncoded = Utils.encodeBASE64(username.concat(":").concat(password));
        header("Proxy-Connection", "keep-alive");
        return header("Proxy-Authorization", "Basic ".concat(credentialsEncoded));

    }


    public T tryAllProxies(){

        attemptProxiesIfFail = true;
        return getThis();
    }


    public T readTimeout(int sec){

        http.setReadTimeout(1000*sec);

        return getThis();

    }

    public T connectTimeout(int sec){

        http.setConnectTimeout(1000*sec);

        return getThis();

    }

    public T timeout(int sec){
        connectTimeout(sec);
        return readTimeout(sec);
    }

    public T timeout(int readSec, int connectSec){
        connectTimeout(connectSec);
        return readTimeout(readSec);
    }


    public String fetch() throws IOException, BadHTTPStatusException, ProxyAuthorizationRequiredException {


        // TODO get header


        header("Content-Length", body.length() + "");
        if (debug)
            for (Map.Entry<String, List<String>> item: http.getRequestProperties().entrySet()){

                System.out.println(item.getKey() + ": " + Utils.join(item.getValue(), " ; "));
            }
        if (http.getDoOutput())
            Utils.write(http.getOutputStream(), body.toString());

        int status = http.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            String responseBody = Utils.read(http.getInputStream());
            return responseBody;

        } else if (status == HttpURLConnection.HTTP_PROXY_AUTH ) {

            throw new ProxyAuthorizationRequiredException();

        } else{
            String errorData = Utils.read(http.getErrorStream());

            if (attemptProxiesIfFail){

                Request r = null;
                for (InetSocketAddress proxy: proxies){


                    r = new Request(this); //clone request
                    r.fetch(); // proxy session?

                }
            }
            throw new BadHTTPStatusException(status, errorData);
        }

    }


    public JsonNode fetchJson() throws IOException, BadHTTPStatusException, ProxyAuthorizationRequiredException {

        String raw = fetch();

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(raw,JsonNode.class);

    }


    public <T> T fetchJson(Class<T> type) throws IOException, BadHTTPStatusException, ProxyAuthorizationRequiredException {

        String raw = fetch();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(raw, type);

    }


    public Response send() throws IOException, BadHTTPStatusException, ProxyAuthorizationRequiredException {
        String raw = fetch();
        return new Response( http.getHeaderFields() , raw);
    }

    public static boolean isProxyGlobal() {
        return isProxyGlobal;
    }
}
