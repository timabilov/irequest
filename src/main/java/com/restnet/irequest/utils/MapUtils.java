package com.restnet.irequest.utils;

import java.util.HashMap;

/**
 *
 */
public class MapUtils {





    public static HashMap<String,String> mapOf(String... args){

        HashMap<String, String> map = new HashMap<String, String>();
        if (args.length < 2 || args.length % 2 != 0 ){
            throw  new IllegalArgumentException("Arguments must be even!");
        }
        for (int i = 0; i < args.length; i+=2){

            map.put(args[i], args[i + 1]);
        }

        return map;


    }
}
