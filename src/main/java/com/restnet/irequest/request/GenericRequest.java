package com.restnet.irequest.request;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restnet.irequest.exception.*;
import com.restnet.irequest.utils.MapUtils;
import com.restnet.irequest.utils.Utils;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

abstract class GenericRequest<T extends GenericRequest> {



    private static boolean isProxyGlobal = false;
    private static boolean forceValidateCookies = true;

    HttpURLConnection http;
    String url;

    Method method;

    StringBuilder body = new StringBuilder();

    HashMap<String, String> cookies = new HashMap<String, String>();



    boolean debug;

    boolean printRawAtTheEnd;

    boolean supressHttpFail = false;

    String name;

    /**
     *  Decorated InputStream implementation class. For gzip, deflate reading and etc.
     */
    Class userStreamDecorator = null;



    protected abstract T getThis();


    /**
     *  Only for conversion purposes to transfer current state.
     */

    protected GenericRequest(HttpURLConnection http, String url, Method method, StringBuilder body, String name, boolean printRawAtTheEnd){


        this.http = http;
        this.url = url;
        this.method = method;
        this.body = body;
        this.name = name;
        this.printRawAtTheEnd = printRawAtTheEnd;


    }


    /**
     * all initial state passes here
     * @param urlRaw
     * @param method
     * @throws MalformedURLException
     * @throws IOException
     */
    protected GenericRequest(String urlRaw, Method method) throws MalformedURLException, IOException { // explicit pointing malform for  readability


        this(null, urlRaw, method, new StringBuilder(), "Plain request", false);

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

        if (!isProxyGlobal)
            setJVMProxyServer("", 0, false); // reset

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

    public static void setForceValidateCookies(boolean skip){

        forceValidateCookies = skip;
    }


    // TODO convert formparams to hashmap
    public JsonRequest jsonify(){

        return new JsonRequest(this);
    }



    T body(String content) throws BodyNotWritableException {

        if (method == Method.GET)
            throw new BodyNotWritableException();

        this.body = new StringBuilder(content);
        return getThis();
    }

    public T  header(String key, String value) throws CookieParseException {

        if (key.equals("Cookie")) {
            try {
                MapUtils.parse(cookies, value, ";", "=", forceValidateCookies);
            } catch (ParseToMapException ptme){
                throw new CookieParseException("Error parsing cookie through ';'. \n Chunk index: " + ptme.getErrorIndex() + ". Chunk: ".concat(ptme.getPart()).concat(" \n.To reduce the severity level turn off global validate cookies"));
            }
        }

        http.setRequestProperty(key, value);
        return getThis();
    }


    public T cookie(String key, String value){

        cookies.put(key, value);
        return getThis();
    }

    public T name(String requestName){

        this.name = requestName;
        return getThis();
    }


    public T basicAuth(String username, String password){

        String credentialsEncoded = Utils.encodeBASE64(username.concat(":").concat(password));
        return header("Authorization", "Basic ".concat(credentialsEncoded));
    }


    public T saveProxy(){

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


    private T acceptEncoding(String encoding){

        return header("Accept-Encoding", encoding);
    }

    private  <S extends InputStream> T withReader(Class<S> reader){

        this.userStreamDecorator = reader;

        return getThis();

    }


    public T suppressFail(){

        this.supressHttpFail = true;
        return getThis();
    }
    public String fetch() throws IOException, BadHTTPStatusException {


        // TODO have to solve efficient policy for large bytes read and write?.


        header("Content-Length", body.length() + "");
        acceptEncoding("gzip");
        withReader(GZIPInputStream.class);

        if (cookies.size() > 0)
            header("Cookie", MapUtils.join(cookies, ";", "="));
        if (debug)
            for (Map.Entry<String, List<String>> item: http.getRequestProperties().entrySet()){

                System.out.println(item.getKey() + ": " + Utils.join(item.getValue(), " ; "));
            }

        if (printRawAtTheEnd)
            snapshot();

        if (http.getDoOutput())
            Utils.write(http.getOutputStream(), body.toString());

        int status = http.getResponseCode(); //force connect


        if (status < 400) {

            InputStream inputStream = http.getInputStream();

            if (userStreamDecorator != null && http.getContentEncoding() !=null && http.getContentEncoding().equals("gzip")){
                try {
                    inputStream = Utils.decorate(userStreamDecorator, http.getInputStream());
                } catch (NoSuchMethodException e) {

                    e.printStackTrace();
                    throw new ReaderInitException("Cannot init your reader. " + e.getMessage());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new ReaderInitException("Cannot init your reader. " + e.getMessage());
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    throw new ReaderInitException("Cannot init your reader. " + e.getMessage());
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    throw new ReaderInitException("Cannot init your reader. " + e.getMessage());
                }
            }


            String responseBody = Utils.read(inputStream, "UTF-8");
            return responseBody;

        } else {

            InputStream inputStream = http.getErrorStream();

            if (inputStream == null) {

                 if (supressHttpFail)
                     return "";
                 throw new BadHTTPStatusException(status, "");
            }
            if (userStreamDecorator != null && http.getContentEncoding() !=null && http.getContentEncoding().equals("gzip")){
                try {
                    inputStream = Utils.decorate(userStreamDecorator, http.getInputStream());
                } catch (NoSuchMethodException e) {

                    e.printStackTrace();
                    throw new ReaderInitException("Cannot init your reader. " + e.getMessage());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new ReaderInitException("Cannot init your reader. " + e.getMessage());
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    throw new ReaderInitException("Cannot init your reader. " + e.getMessage());
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    throw new ReaderInitException("Cannot init your reader. " + e.getMessage());
                }
            }

            String errorData = Utils.read(http.getErrorStream(), "UTF-8");
            if (supressHttpFail)
                return errorData;
            throw new BadHTTPStatusException(status, errorData);
        }

    }


    /**
     * Force fetch and dump to file
     * @param filename
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws BadHTTPStatusException
     */
    public void fetchAndSaveTo(String filename) throws FileNotFoundException, IOException, BadHTTPStatusException {


        String raw = fetch();
        FileOutputStream fos = new FileOutputStream(new File(filename));

        Utils.write(fos, raw);

    }

    public JsonNode fetchJson() throws IOException, BadHTTPStatusException {

        String raw = fetch();

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(raw,JsonNode.class);

    }


    public <O> O fetchJson(Class<O> type) throws IOException, BadHTTPStatusException {

        String raw = fetch();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(raw, type);

    }


    public Response send() throws IOException, BadHTTPStatusException {
        String raw = fetch();
        return new Response( http.getHeaderFields() , raw, http.getResponseCode(), name);
    }

    public static boolean isProxyGlobal() {
        return isProxyGlobal;
    }

    public HashMap<String, String> getCookies(){
        return cookies;
    }


    @Override
    public String toString() {

        StringBuilder raw = new StringBuilder();
        URL url = http.getURL();
        String port = "" + (url.getPort() == -1 ? 80: url.getPort()); // Todo verify

        raw.append("\nName: ").append(name).append("\n\n  ");

        //Request line Todo protocol version
        raw.append(method.name()).append(" ").append(http.getURL().getPath()).append(" ").append(url.getProtocol().toUpperCase() +"/1.1").append("\n  ");

        //Host
        raw.append("Host: ").append(url.getHost().concat(":".concat(port))).append("\n  ");
        try {
            buildBody();
        } catch (IOException ioe){

            throw new RuntimeException("Cannot build body.".concat(ioe.getMessage()) );
        }
        raw.append(MapUtils.join(http.getRequestProperties(), "\n  ", ": ", "; ", "Cookie"))
                .append(cookies.size() > 0 ? " Cookie: ": "" )
                .append(MapUtils.join(cookies,"; ", "=").concat("\n\n"))
                .append(body.toString().replaceAll("(?m)^", "  ").concat("\n"));

        raw.append("----");

        return  raw.toString();
    }

    protected void buildBody() throws IOException{

        //
    }

/*    /**
     * Print raw request String. Used to considering that final stage has own changes to request.
     * @param late Print now  or late (final build stage)
     * @return
     *//*
    public T snapshot(boolean late){

        if (!(printRawAtTheEnd = late))
            System.out.println(toString());
        return getThis();
    }*/

    /**
     * Prints raw request snapshot only with initial(!) state. Consider that final stage has own changes to request.
     * @return
     */
    public T snapshot(){


        //System.out.println(toString());
        return getThis();
    }
}
