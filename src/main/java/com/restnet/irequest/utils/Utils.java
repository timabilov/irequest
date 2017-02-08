package com.restnet.irequest.utils;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by Asus on 2/8/2017.
 */
public class Utils {


    public static String join(List<String> items, String separator){

        StringBuilder result = new StringBuilder();
        for ( String item: items){

            result.append(item.concat(separator));

        }

        result.deleteCharAt(result.length() - 1);
        return result.toString();

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
    public static String read(InputStream inputStream) throws IOException {
        String str = "";
        try (InputStream is = inputStream) {

            byte[] data = new byte[is.available()];
            int byteCount = is.read(data); // TODO count assert?
            str = new String(data, StandardCharsets.UTF_8);
            return str;

        }
    }



    public static  void write(OutputStream outputStream, String data) throws IOException {
        String str = "";
        try (OutputStream os = outputStream) {

            os.write(data.getBytes(StandardCharsets.UTF_8));


        }
    }
}
