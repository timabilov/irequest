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



    public static ByteArrayOutputStream read(InputStream inputStream) throws IOException {

        BufferedInputStream is = new BufferedInputStream(inputStream);


        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];

        int read = is.read(buffer);
        while ( read != -1 ) {

            bos.write(buffer,0, read);
            read = is.read(buffer);
        }
        bos.flush();
        return bos;


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

    public static  void write(OutputStream outputStream, InputStream is) throws IOException {
        String str = "";
        byte[] buffer = new byte[2048];
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream os = new BufferedOutputStream(outputStream);
        int readed = bis.read(buffer);


        while (readed != -1) {

            os.write(buffer, 0 , readed);
            readed = bis.read(buffer);
        }




        os.flush();
    }

    // TODO log and good handle
    public static <S extends InputStream> InputStream decorate(Class<S>  wrapWith, InputStream stream)  {


        Class<S> clazz =  wrapWith;
        Constructor<S> constructor = null;
        try {
            constructor = clazz.getDeclaredConstructor(InputStream.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        constructor.setAccessible(true);
        InputStream instance = null;
        try {
            instance = constructor.newInstance(stream);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return instance;

    }


}
