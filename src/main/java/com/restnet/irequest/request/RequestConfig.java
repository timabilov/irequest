package com.restnet.irequest.request;

/**
 *
 */

public class RequestConfig {

    Request request;



    protected RequestConfig(){


    }


    protected void holdRequest(Request request){
        this.request = request;
    }

    public RequestConfig globalProxyServer(String host, int port, boolean isSOCKS){

       RequestConfig.setJVMProxyServer(host, port, isSOCKS);
        return this;

    }

    public static RequestConfig setJVMProxyServer(String host, int port, boolean isSOCKS){

        if (!isSOCKS) {
            System.setProperty("http.proxyHost", host);
            System.setProperty("http.proxyPort", port + "");
            System.setProperty("https.proxyHost", host);
            System.setProperty("https.proxyPort", port + "");
        } else {

            System.setProperty("socksProxyHost", host);
            System.setProperty("SocksProxyPort", port + "");

        }

        return new RequestConfig();

    }






}
