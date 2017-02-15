package com.restnet.irequest.request;



import com.restnet.irequest.exception.BadHTTPStatusException;
import com.restnet.irequest.exception.FileRelocationException;
import com.restnet.irequest.utils.MapUtils;
import com.restnet.irequest.utils.Utils;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MultipartRequest extends GenericRequest<MultipartRequest> {



    HashMap<String, File> files = new HashMap<String, File>();
    String charset;
    private  final String boundary;

    private  static final String LINE_FEED = "\r\n";


    /**
     *  Only for converting to other requests multipart  purpose
     * @param r
     */
    protected MultipartRequest(FormRequest r, String charset)  {

        super(r.http, r.url, r.method,  r.body, r.name, r.userStreamDecoratorClazz, r.printRawAtTheEnd, r.suppressHttpFail);
        this.params = new HashMap<String, Object>(r.params);
        this.charset = charset;


        boundary = "----WebKitFormBoundary".concat(Utils.randomString(16)); // used webkit style boundary  for no reason;
        super.header("Content-Type",
                "multipart/form-data; boundary=" + boundary);


    }


    public MultipartRequest(String urlRaw, String charset) throws MalformedURLException, IOException {

        super(urlRaw, Method.POST);
        byte[] rand = new byte[36]; // rest of boundary must be 16 symbol - UTF_8


        boundary = "----WebKitFormBoundary".concat(Utils.randomString(16)); // used webkit style boundary  for no reason;


        super.header("Content-Type",
                "multipart/form-data; boundary=" + boundary);

    }

    @Override
    public MultipartRequest header(String key, String value) {
        // ignore content types because based upon
        if (key.equals("Content-Type"))
            return this;
        super.header(key, value);
        return this;
    }

    public MultipartRequest body(String content){
        super.body(content);
        return getThis();
    }


    @Override
    public JsonRequest jsonify() throws FileRelocationException{

        JsonRequest jsonRequest = new JsonRequest(this).with(new HashMap<String, Object>(params));

        for (Map.Entry<String, File> file: files.entrySet()){
            try {

                FileInputStream fis = new FileInputStream(file.getValue());
                jsonRequest.param(file.getKey(), MapUtils.mapOf(file.getValue().getName(), DatatypeConverter.printBase64Binary(Utils.read(fis, "UTF-8").getBytes())));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new FileRelocationException("File not found during file translations: ".concat(file.getKey()).concat(":").concat(file.getValue().getName()));
            } catch (IOException ioe){
                ioe.printStackTrace();
                throw new FileRelocationException("Stream read error during file translations: ".concat(file.getKey()).concat(":").concat(file.getValue().getName()));

            }
        }

        return jsonRequest;
    }

    public MultipartRequest param(String name, File file){


        files.put(name, file);
        return this;
    }




    protected void pack() throws IOException {

        super.pack();

        for (Map.Entry<String, Object> pair: params.entrySet()){

            body.append(toMultipartField(pair.getKey(), (String)pair.getValue()));
        }

        for (Map.Entry<String, File> pair: files.entrySet()){

            body.append(toMultipartFile(pair.getKey(), pair.getValue()));
        }
        body.append(LINE_FEED);

        body.append("--").append(boundary).append("--").append(LINE_FEED);
        if (debug) {
            System.out.println("Builded multipart.");
            System.out.println(body.toString());
        }



    }


    private String toMultipartField(String name, String value){


        StringBuilder builder  = new StringBuilder();
        builder.append("--" ).append(boundary).append(LINE_FEED);
        builder.append("Content-Disposition: ").append("form-data; name=\"").append(name).append("\"").append(LINE_FEED);
        builder.append("Content-Type: ").append("text/plain; charset=").append(charset).append(LINE_FEED);
        builder.append(LINE_FEED);
        builder.append(value).append(LINE_FEED);
        return  builder.toString();
    }


    private String toMultipartFile(String name, File file) throws FileNotFoundException, IOException {


        String fileName = file.getName();
        StringBuilder builder  = new StringBuilder();
        builder.append("--" ).append(boundary).append(LINE_FEED);
        builder.append("Content-Disposition: ").append("form-data; name=\"").append(name).
                append("\"; filename=\"").append(fileName).append("\"").append(LINE_FEED);


        String contentType = URLConnection.guessContentTypeFromName(fileName);
        if (contentType == null)
            contentType = "application/octet-stream";
        builder.append("Content-Type: ").append(contentType).append(LINE_FEED);

        builder.append(LINE_FEED);


        FileInputStream fis = new FileInputStream(file);

        builder.append(Utils.read(fis));

        builder.append(LINE_FEED);

        return  builder.toString();
    }

    @Override
    protected MultipartRequest getThis() {
        return this;
    }


    @Override
    public String fetch() throws IOException, BadHTTPStatusException {


        return super.fetch();
    }
}
