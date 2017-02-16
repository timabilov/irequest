package com.restnet.irequest.request;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restnet.irequest.enums.HttpCompressType;
import com.restnet.irequest.exception.*;
import com.restnet.irequest.utils.MapUtils;
import com.restnet.irequest.utils.Utils;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class GenericRequest<T extends GenericRequest> {


    HashMap<String, Object> params = new HashMap<String, Object>();

    HashMap<String, String> queryParams = new HashMap<String, String>();

    private static boolean isProxyGlobal = false;
    private static boolean skipCookieValidation = true;

    int readTimeout = -1;
    int connectTimeout = -1;

    HashMap<String, String> headers = new HashMap<String, String>();



    HttpURLConnection http;
    String url;

    Method method;

    StringBuilder body = new StringBuilder();

    HashMap<String, String> cookies = new HashMap<String, String>();



    boolean debug;

    boolean printRawAtTheEnd;

    boolean suppressHttpFail = false;

    String name;

    /**
     *  Decorated InputStream implementation class. For gzip, deflate reading and etc.
     */
    Class userStreamDecoratorClazz = null;



    protected abstract T getThis();


    /**
     *  Only for conversion purposes to transfer current state.
     */

    protected GenericRequest(HttpURLConnection http, String url, Method method, StringBuilder body, String name, Class userStreamDecoratorClazz, boolean printRawAtTheEnd, boolean supressHttpFail) {



        this.http = http;
        this.url = url;
        this.method = method;
        this.body = body;
        this.name = name;
        this.printRawAtTheEnd = printRawAtTheEnd;
        this.userStreamDecoratorClazz = userStreamDecoratorClazz;
        this.suppressHttpFail = supressHttpFail;

    }



    /**
     * all initial state passes here
     * @param urlRaw
     * @param method
     * @throws MalformedURLException
     * @throws IOException
     */
    protected GenericRequest(String urlRaw, Method method) throws MalformedURLException, IOException { // explicit pointing malform for  readability


        this(null, urlRaw, method, new StringBuilder(), "Plain request", null, false, false);

        String queryParams = new URL(url).getQuery();


        if (queryParams!=null){
            this.url = this.url.replace(queryParams, "").replace("?","");
            MapUtils.parse(this.queryParams, queryParams,"&", "=", false);
        }



    }


    protected void setupConnection() throws IOException {

        if (!url.replace("s", "").startsWith("http://") ){ // drop s from https(if) and check for scheme
            this.url = "http://" + this.url;
        }

        URL url = new URL(this.url);
        http = ((HttpURLConnection)url.openConnection());
        if (! (method == Method.GET))
            http.setDoOutput(true);
        http.setDoInput(true); // just explicit
        http.setUseCaches(false);
        http.setRequestProperty("User-Agent" , "iRequest Agent");
        http.setRequestMethod(method.name());

        if (!isProxyGlobal)
            setJVMProxyServer("", 0, false); // reset

        if (readTimeout > 0)
            http.setReadTimeout(readTimeout*1000);
        if (connectTimeout > 0)
            http.setConnectTimeout(connectTimeout*1000);

        migrateHeaders();

        if (http.getDoOutput())
            Utils.write(http.getOutputStream(), new ByteArrayInputStream(body.toString().getBytes("UTF-8")));

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

    public static void skipCookieValidation(boolean skip){

        skipCookieValidation = skip;
    }


    // TODO convert formparams to hashmap
    public JsonRequest jsonify(){

        return new JsonRequest(this);
    }

    /**
     * appends(!) url argument(query param)
     * @param key
     * @param value
     * @return
     */
    public T arg(String key, String value){


        queryParams.put(key, value);

        return getThis();

    }

    /**
     * merge with previously added params
     * @param queryParams
     * @return
     */
    public T args(HashMap<String, String> queryParams){

        MapUtils.merge(params, new HashMap<String,Object>(queryParams));
        return getThis();

    }

    protected T param(String key, String value){

        params.put(key, value);
        return getThis();

    }

    /**
     * merge with previously added params
     * @param params
     * @return
     */

    protected T params(HashMap<String, String> params){

        MapUtils.merge(this.params, new HashMap<String,Object>(params));
        return getThis();
    }

    T setParams(HashMap<String, Object> params){

        this.params = params;
        return getThis();
    }

    T body(String content){

        this.body = new StringBuilder(content);
        return getThis();
    }

    void setRequestProperty(String key, String value) {
        headers.put(key, value);
    }

    public T  header(String key, String value) throws CookieParseException {

        if (key.equals("Cookie"))
            sanitizeCookie(value);
        setRequestProperty(key, value);
        return getThis();
    }

    protected void sanitizeCookie( String value){

        try {
            MapUtils.parse(cookies, value, ";", "=", !skipCookieValidation); // force strict if we not want skip cookie validation
        } catch (ParseToMapException ptme){
            throw new CookieParseException("Error parsing cookie through ';'. \n Chunk index: " + ptme.getErrorIndex() + ". Chunk: ".concat(ptme.getPart()).concat(".\nTo reduce the severity level turn off global cookie validation"));
        }

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
     * This method intended to quick setting proxy with basic(!) auth , java.net.authenticator  and system properties. Anyway explicit setting  system properties  recommended. For SOCKS proxy applies the same(set explicit)  except - basic auth is not needed.
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





    /**
     * Due to GET request behavior this method just cache this value. Finally injected at fetch stage
     * @param sec
     * @return
     */
    public T readTimeout(int sec) {
        this.readTimeout = sec;
        return getThis();
    }

    /**
     * Due to GET request behavior this method just cache this value. Finally injected at fetch stage
     * @param sec
     * @return
     */
    public T connectTimeout(int sec) {
        this.connectTimeout = sec;
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

    public   <S extends InputStream> T withReader(Class<S> reader){

        this.userStreamDecoratorClazz = reader;

        return getThis();

    }

    /**
     * Fail silently with bad http codes
     * @return
     */
    public T suppressFail(){

        this.suppressHttpFail = true;
        return getThis();
    }

    private void prepareMeta() throws IOException {

        // migrate user attached headers to request object

        if ( method != Method.GET)
            header("Content-Length", body.length() + "");

        if (headers.get("Accept-Encoding") == null) {
            acceptEncoding("gzip"); // at the end auto injects available appropriate stream
        }

        if (cookies.size() > 0)
            header("Cookie", MapUtils.join(cookies, ";", "="));


    }


    private void finalHooks(){

        if (printRawAtTheEnd)
            System.out.println(toString());

    }

    public String fetch() throws IOException, BadHTTPStatusException {

        return new String(fetchBytes().toByteArray(), "UTF-8");
    }



    protected ByteArrayOutputStream fetchBytes() throws IOException, BadHTTPStatusException {

        // TODO have to solve efficient policy for large bytes read and write?

        pack(); // pack request data
        prepareMeta(); // meta headers etc.
        setupConnection(); // now we ready to connect

        finalHooks(); // just callbacks log and etc.


        int status;

        try {
            status = http.getResponseCode(); // force connect
        } catch (Exception e){

            e.printStackTrace();
            throw new ConnectException("Cannot connect to  " +http.getURL().toString() + "." +
                    " Probably server does not respond. Maybe proxy prevents connection?");
        }


        if (status < 400) {

            InputStream inputStream = http.getInputStream();

            if (http.getContentEncoding() != null){ // auto injected or user defined stream for decoding data

                Class decorator = resolveDecoder();

                if (decorator != null) // exists any decompress decorator
                    inputStream = Utils.decorate(decorator, http.getInputStream());

            }


            return Utils.read(inputStream);

        } else {

            InputStream errorStream = http.getErrorStream();

            if (errorStream == null) {

                 if (suppressHttpFail)
                     return new ByteArrayOutputStream(0);
                 throw new BadHTTPStatusException(status, "");
            }

            if (http.getContentEncoding() != null){

                Class decorator = resolveDecoder();

                if (decorator != null) // exists any decompress decorator
                    errorStream = Utils.decorate(decorator, http.getInputStream());

            }

            ByteArrayOutputStream bos = Utils.read(errorStream);
            if (suppressHttpFail)
                return bos;
            throw new BadHTTPStatusException(status, new String(bos.toByteArray(), "UTF-8"));
        }

    }


    private Class resolveDecoder(){

        try {

            Class decorator = null;
            try {
                HttpCompressType consciousCompressed = HttpCompressType.valueOf(http.getContentEncoding().toUpperCase());
                decorator = consciousCompressed.getDecompressorClazz();
            } catch (Exception e){

                decorator = userStreamDecoratorClazz;
                if (decorator == null){

                    System.err.println(http.getContentEncoding() + " - No suitable de-compressor class(InputStream) was founded to decode content. You can provide one.");
                }
            }




            return decorator;


        } catch (Exception e) {

            e.printStackTrace();
            throw new ReaderInitException("Cannot init your reader. " + e.getMessage());
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
    public void pipe(String filename) throws FileNotFoundException, IOException, BadHTTPStatusException {



        FileOutputStream fos = new FileOutputStream(new File(filename));

        Utils.write(fos, new ByteArrayInputStream(fetchBytes().toByteArray()));

    }


    public void pipe(OutputStream os) throws FileNotFoundException, IOException, BadHTTPStatusException {





        Utils.write(os, new ByteArrayInputStream(fetchBytes().toByteArray()));

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


    /**
     * Build form params with url encoder.
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    String constructParams(HashMap<String, String> params) throws UnsupportedEncodingException {

        List<String> paramPairs = new ArrayList<String>();
        for (Map.Entry<String, String> paramPair : params.entrySet()) {

            paramPairs.add(URLEncoder.encode(paramPair.getKey(), "UTF-8").concat("=").concat(URLEncoder.encode(paramPair.getValue(), "UTF-8")));
        }


        String concated = Utils.join(paramPairs, "&");
        return concated;

    }
    @Override
    public String toString() {

        StringBuilder raw = new StringBuilder();
        URL url = http.getURL();
        String port = "" + (url.getPort() == -1 ? 80: url.getPort()); // Todo verify

        raw.append("\nName: ").append(name).append("  >>> \n\n");

        String queryParams = url.getQuery() == null ? "" : "?".concat(url.getQuery());
        //Request line Todo protocol version
        raw.append(method.name()).append(" ").append(url.getPath()).append(queryParams).append(" ").append(url.getProtocol().toUpperCase().replace("S", "") +"/1.1").append("\n");

        //Host
        raw.append("Host: ").append(url.getHost().concat(":".concat(port))).append("\n");
        raw.append(MapUtils.join( headers, "\n", ": ","Cookie"))
                .append(cookies.size() > 0 ? " Cookie: ": "" )
                .append(MapUtils.join(cookies,"; ", "=").concat("\n\n"))
                .append(body.toString().replaceAll("(?m)^", "").concat("\n"));

        raw.append("\n-----");

        return  raw.toString();
    }

    private void migrateHeaders(){


        for (Map.Entry<String, String> entry: headers.entrySet()) {
            http.setRequestProperty(entry.getKey(), entry.getValue());
        }

    }


    /**
     * By default this method builds only GET Params. Subclasses override and must call super to both collect get params and post etc.
     * @throws IOException
     */
    protected void pack() throws IOException{

        if (queryParams != null && queryParams.size() > 0) {
            String transformed = constructParams(queryParams);
            URL url = new URL(this.url);
            boolean paramsExist = url.getQuery() != null;
            this.url = this.url.concat(paramsExist ? "&" :"?").concat(transformed);
        }
    }

/*    /**
     * Print raw request String. Used to considering that final stage has own changes to request.
     * @param late Print now  or late (final pack stage)
     * @return
     *//*
    public T snapshot(boolean late){

        if (!(printRawAtTheEnd = late))
            System.out.println(toString());
        return getThis();
    }*/

    /**
     * Prints raw request snapshot before send
     * @return
     */
    public T snapshot(){

        printRawAtTheEnd = true;
        //System.out.println(toString());
        return getThis();
    }
}
