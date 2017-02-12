package com.restnet.irequest.utils;

import com.restnet.irequest.exception.ReaderInitException;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

/**
 * Created by Asus on 2/8/2017.
 */
public class Utils {


    public static String join(List<String> items, String separator){

        StringBuilder result = new StringBuilder();
        for ( String item: items){

            result.append(item.concat(separator));

        }

        if (items.size() > 0)
            result.deleteCharAt(result.length() - separator.length());
        return result.toString();

    }

    public static boolean isNull(Object o){

        return  o == null;
    }

    public static String defaultOr(String str, String defaultReplaced){

        return isNull(str) ? defaultReplaced : str;
    }

    public static String emptyOr(String string){

        return isNull(string) ? "" : string;
    }
    public static String join(String[] items, String separator){

        StringBuilder result = new StringBuilder();
        for ( String item: items){

            result.append(item.concat(separator));

        }

        result.deleteCharAt(result.length() - 1);
        return result.toString();

    }

    public static String encodeBASE64(String input){

        return DatatypeConverter.printBase64Binary(input.getBytes(StandardCharsets.UTF_8));
    }
    public static String read(InputStream inputStream, String encoding) throws IOException {
        String str = "";
        byte[] data;
        try (InputStream is = inputStream) {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];

            int read = is.read(buffer);
            while ( read != -1 ) {

                bos.write(buffer,0, read);
                read = is.read(buffer);
            }

            return bos.toString(encoding);



        }


    }



    public static byte[] read(InputStream inputStream) throws IOException {
        String str = "";
        byte[] data;
        try (InputStream is = inputStream) {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];

            int read = is.read(buffer);
            while ( read != -1 ) {

                bos.write(buffer,0, read);
                read = is.read(buffer);
            }

            return bos.toByteArray();



        }


    }


    public static String randomString(int length){
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < length; i++){
            b.append(base.charAt(random.nextInt(base.length())));
        }
        return b.toString();
    }

    private static String base = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabsdefghijklmnopqrstuvwxyz";
    private static Random random = new Random();

    public static  void write(OutputStream outputStream, String data) throws IOException {
        String str = "";
        try (OutputStream os = outputStream) {

            os.write(data.getBytes(StandardCharsets.UTF_8));


        }
    }


    public static <S extends InputStream> InputStream decorate(Class<S>  wrapWith, InputStream stream) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {


            Class<S> clazz =  wrapWith;
            Constructor<S> constructor = clazz.getDeclaredConstructor(InputStream.class);
            constructor.setAccessible(true);
            InputStream instance = constructor.newInstance(stream);

            return instance;

    }


}
