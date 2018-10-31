package com.github.timabilov.irequest.enums;

import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 *
 */
public enum HttpCompressType {

    GZIP(GZIPInputStream.class), DEFLATE(InflaterInputStream.class);


    Class decompressorClazz;


    HttpCompressType(Class decompressor){

        this.decompressorClazz = decompressor;
    }


    public Class getDecompressorClazz() {
        return decompressorClazz;
    }
}
