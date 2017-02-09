package com.restnet.irequest.request;

import com.restnet.irequest.exception.BadHTTPStatusException;
import com.restnet.irequest.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class inherited from Request to work with directly 'multipart/form-data' and put appropriate parameters convenient way.
 * Used web-kit style boundary for no reason.
 * @author TamerlanA
 */

public class FormRequest extends GenericRequest<FormRequest> {

    HashMap<String, String> params = new HashMap<String, String>();


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




    public FormRequest param(String name, String value){

        params.put(name, value);
        return this;

    }

    public FormRequest params(HashMap<String, String > params){
        this.params = params;
        return this;
    }


    /**
     *  Converts form request to multipart with passed form params and headers
     * @return
     */

    public MultipartRequest multipart(String charset) {

        return new MultipartRequest(this, charset);

    }


    private void buildBody() throws UnsupportedEncodingException {

        List<String> paramPairs = new ArrayList<String>();
        for (Map.Entry<String, String> paramPair: params.entrySet()){

            paramPairs.add(URLEncoder.encode(paramPair.getKey(), "UTF-8").concat("=").concat(URLEncoder.encode(paramPair.getValue(), "UTF-8")));
        }


        String concated = Utils.join(paramPairs, "&");

        body = new StringBuilder(concated);

    }

    @Override
    public String fetch() throws IOException, BadHTTPStatusException {

        buildBody();
        return super.fetch();
    }
}
