package com.github.timabilov.irequest.request;



import com.github.timabilov.irequest.exception.BadHTTPStatusException;
import com.github.timabilov.irequest.exception.FileRelocationException;
import com.github.timabilov.irequest.utils.MapUtils;
import com.github.timabilov.irequest.utils.Utils;
import com.github.timabilov.irequest.utils.FileDTO;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MultipartRequest extends GenericRequest<MultipartRequest> {



    HashMap<String,FileDTO> files = new HashMap<String, FileDTO>();
    String charset;
    private  final String boundary;

    private  static final String LINE_FEED = "\r\n";


    /**
     *  Only for converting to other requests multipart  purpose
     * @param r
     */
    protected MultipartRequest(PostRequest r, String charset)  {

        super(r.http, r.url, r.method,  r.body, r.name, r.userStreamDecoratorClazz, r.printRawAtTheEnd, r.suppressHttpFail);
        this.params = new HashMap<String, Object>(r.params);
        this.charset = charset;


        boundary = "----WebKitFormBoundary".concat(Utils.randomString(16)); // used webkit style boundary  for no reason;
        super.header("Content-Type",
                "multipart/form-data; boundary=" + boundary);


    }


    public MultipartRequest(String urlRaw, String charset) throws MalformedURLException, IOException {

        super(urlRaw, Method.POST);

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
    public JsonRequest jsonify() throws FileRelocationException {

        JsonRequest jsonRequest = new JsonRequest(this).with(new HashMap<String, Object>(params));

        for (Map.Entry<String, FileDTO> file: files.entrySet()){
            try {


                jsonRequest.param(file.getKey(), MapUtils.mapOf("name", file.getValue().getName(),"body", DatatypeConverter.printBase64Binary(Utils.read(new ByteArrayInputStream(file.getValue().getBody()), "UTF-8").getBytes())));

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

    public MultipartRequest param(String name, File file) throws IOException {


        files.put(name, new FileDTO(file.getName(), Utils.read(new FileInputStream(file)).toByteArray()));
        return this;
    }


    public MultipartRequest param(String name, FileDTO fileDTO) throws IOException {


        files.put(name, new FileDTO(fileDTO.getName(), fileDTO.getBody()));
        return this;
    }




    protected void pack() throws IOException {

        super.pack();

        for (Map.Entry<String, Object> pair: params.entrySet()){

            body.append(Utils.toBase64(toMultipartField(pair.getKey(), (String)pair.getValue()).getBytes()));
        }


        // encoded to base64 with binary
        for (Map.Entry<String, FileDTO> pair: files.entrySet()){

            body.append(toMultipartFile(pair.getKey(), pair.getValue()));
        }
        body.append(Utils.toBase64((
                "--" + boundary + "--" + LINE_FEED).getBytes()));
        if (debug) {
            System.out.println("Builded multipart.");
            System.out.println(body.toString());
        }



    }


    @Override
    byte[] transformToBytes(String data) {

        return Utils.fromBase64(data);
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


    private String toMultipartFile(String name, FileDTO file) throws FileNotFoundException, IOException {


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

        builder = new StringBuilder(Utils.toBase64(builder.toString().getBytes()));


        builder.append(Utils.toBase64(file.getBody()))
        .append(Utils.toBase64(LINE_FEED.getBytes()));



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
