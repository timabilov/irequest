package com.restnet.irequest.request;

import com.restnet.irequest.utils.MapUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

/**
 * Utility class inherited from Request to work with directly 'multipart/form-data' and put appropriate parameters convenient way.
 * Used web-kit style boundary for no reason.
 * @author TamerlanA
 */

public class FormRequest extends GenericRequest<FormRequest> {


    protected FormRequest(String urlRaw) throws MalformedURLException, IOException {

        super(urlRaw, Method.POST);
        header("Content-Type", "application/x-www-form-urlencoded");
        //super.header( "charset", "utf-8");
    }

    @Override
    protected FormRequest getThis() {
        return this;
    }

    @Override
    public FormRequest header(String key, String value) {
        // ignore content types because based upon
        if (key.equals("Content-Type"))
            return this;
        super.header(key, value);
        return this;
    }


    public FormRequest body(String content){
        super.body(content);
        return getThis();
    }

    @Override
    public JsonRequest jsonify(){

        return new JsonRequest(this).with(new HashMap<String, Object>(params));
    }

    public FormRequest param(String name, String value){

        params.put(name, value);
        return this;

    }

    /**
     * Implicitly converts to multipart(!)
     * @param name
     * @param file
     * @return
     */
    public MultipartRequest param(String name, File file){


        return multipart("UTF-8").setParams(params).param(name, file);

    }

    /**
     *  Converts form request to multipart with passed form params and headers
     * @return
     */

    public MultipartRequest multipart(String charset) {

        return new MultipartRequest(this, charset).setParams(params);

    }


    protected void pack() throws IOException {

        super.pack();

        if (params != null && params.size() > 0) {
            String transformed = constructParams(MapUtils.stringify(params));

            body = new StringBuilder(transformed);
        }

    }

}
