package com.restnet.irequest.request;

/**
 * Created by iRequest on 3/13/2017.
 */
public class Method {


    final static Method GET = new Method("GET");
    public final static Method POST = new Method("POST");
    public final static Method PUT = new Method("PUT");
    public final static Method HEAD = new Method("HEAD");
    public final static Method PATCH = new Method("PATCH");


    public String name;


    private Method(String name){

        this.name = name;
    }



    public String name(){

        return name;
    }


    @Override
    public String toString() {
        return name;
    }
}
