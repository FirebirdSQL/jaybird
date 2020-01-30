package org.firebirdsql.jna.fbclient;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class ISC_TIMESTAMP_TZ extends Structure implements Structure.ByValue {

    public ISC_TIMESTAMP timestamp;
    public int time_zone;

    @Override
    protected List<String> getFieldOrder()
    {
        return Arrays.asList("timestamp", "time_zone");
    }
}
