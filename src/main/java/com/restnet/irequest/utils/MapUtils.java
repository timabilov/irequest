package com.restnet.irequest.utils;

import com.restnet.irequest.exception.ParseToMapException;

import java.io.File;
import java.util.*;

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


    public static String join(Map<String, String> data, String delimiter, String pairDelimiter){

        delimiter = Utils.defaultOr(delimiter, ", ");
        pairDelimiter = Utils.defaultOr(pairDelimiter, ", ");
        StringBuilder result = new StringBuilder();
        for ( Map.Entry<String, String> item: data.entrySet()){

            String key = Utils.emptyOr(item.getKey());
            String value = Utils.emptyOr(item.getValue());
            result.append((key.concat(key.isEmpty()? "" : pairDelimiter).concat(value)).concat(delimiter));

        }

        if (result.length() > 0)

            result.deleteCharAt(result.length() - 1);
        return result.toString();


    }

    public static String join( String delimiter, String pairDelimiter, Map<String, List<String>> data){

        delimiter = Utils.defaultOr(delimiter, ", ");
        pairDelimiter = Utils.defaultOr(pairDelimiter, ", ");

        StringBuilder result = new StringBuilder();
        for ( Map.Entry<String, List<String>> item: data.entrySet()){

            String key = Utils.emptyOr(item.getKey());


            result.append((key.concat(pairDelimiter).concat(Utils.join(item.getValue(), ",")).concat(delimiter)));

        }

        if (result.length() > 0)

            result.deleteCharAt(result.length() - 1);
        return result.toString();


    }


    public static String join(Map<String, List<String>> data, String delimiter, String pairDelimiter, String listDelimiter){

        delimiter = Utils.defaultOr(delimiter, ", ");
        pairDelimiter = Utils.defaultOr(pairDelimiter, ", ");

        StringBuilder result = new StringBuilder();
        for ( Map.Entry<String, List<String>> item: data.entrySet()){

            String key = Utils.emptyOr(item.getKey());

            result.append((key.concat(pairDelimiter).concat(Utils.join(item.getValue(), listDelimiter)).concat(delimiter)));

        }

        if (result.length() > 0)

            result.deleteCharAt(result.length() - 1);
        return result.toString();


    }


    public static String join(Map<String, List<String>> data, String delimiter, String pairDelimiter, String listDelimiter, String... ignoreKeys){

        delimiter = Utils.defaultOr(delimiter, ", ");
        pairDelimiter = Utils.defaultOr(pairDelimiter, ", ");

        StringBuilder result = new StringBuilder();
        Set<String> ignoredSetKeys = new HashSet<>(Arrays.asList(ignoreKeys));
        for ( Map.Entry<String, List<String>> item: data.entrySet()){

            String key = Utils.emptyOr(item.getKey());
            if (!ignoredSetKeys.contains(key))
                result.append((key.concat(pairDelimiter).concat(Utils.join(item.getValue(), listDelimiter)).concat(delimiter)));

        }

        if (result.length() > 0)

            result.deleteCharAt(result.length() - 1);
        return result.toString();


    }


    public static Map<String, Object> merge(Map<String, Object> map, Map<String, Object> toMerge){


        for (Map.Entry<String, Object> pair: toMerge.entrySet()){

            map.put(pair.getKey(), pair.getValue());
        }

        return map;

    }

    public static void parse(Map<String, String> map, String raw, String delimiter, String pairDelimiter, boolean forceStrict){
        int i = 0;
        for (String pairRaw: raw.split(delimiter)){
            String[] pair = pairRaw.trim().split(pairDelimiter, 2);
            if (pair.length != 2){
                if (forceStrict)
                    throw new ParseToMapException(pairRaw, i);
            }  else {

                map.put(pair[0], pair[1]);
            }
            i++;

        }

    }



}
