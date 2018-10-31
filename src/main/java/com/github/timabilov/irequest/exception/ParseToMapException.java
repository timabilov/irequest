package com.github.timabilov.irequest.exception;

/**
 * Created by Asus on 2/11/2017.
 */
public class ParseToMapException extends RuntimeException {

    String part;

    int errorIndex;

    public ParseToMapException(String part, int errorIndex){

        super("Cannot parse map ".concat(part).concat("..."));
        this.part = part;
        this.errorIndex = errorIndex;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public int getErrorIndex() {
        return errorIndex;
    }

    public void setErrorIndex(int errorIndex) {
        this.errorIndex = errorIndex;
    }
}
