package org.firebirdsql.jna.fbclient;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class ISC_TIME_TZ extends Structure implements Structure.ByValue {

    public ISC_TIME time;
    public int time_zone;

    @Override
    protected List<String> getFieldOrder()
    {
        return Arrays.asList("time", "time_zone");
    }
}
