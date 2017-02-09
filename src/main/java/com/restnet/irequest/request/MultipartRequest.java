package com.restnet.irequest.request;



import com.restnet.irequest.exception.BadHTTPStatusException;
import com.restnet.irequest.utils.Utils;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 */
public class MultipartRequest extends GenericRequest<MultipartRequest> {


    HashMap<String, String> params = new HashMap<String, String>();
    HashMap<String, File> files = new HashMap<String, File>();
    String charset;
    private  final String boundary;

    private  static final String LINE_FEED = "\r\n";


    /**
     *  Only for converting to other requests multipart  purpose
     * @param r
     */
    protected MultipartRequest(FormRequest r, String charset){

        super(r.http, r.url, r.method,  r.body);
        this.params = r.params;
        this.charset = charset;


        boundary = "----WebKitFormBoundary".concat("CYnIFJFo8csRRtJX"); // used webkit style boundary  for no reason;
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


    public MultipartRequest param(String name, String value){

        params.put(name, value);
        return this;
    }

    public MultipartRequest param(String name, File file){


        files.put(name, file);
        return this;
    }

    public MultipartRequest params(HashMap<String, String> params){

        this.params = params;
        return this;
    }


    private void buildBody() throws IOException {


        for (Map.Entry<String, String> pair: params.entrySet()){

            body.append(toMultipartField(pair.getKey(), pair.getValue()));
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


        buildBody();
        return super.fetch();
    }
}
