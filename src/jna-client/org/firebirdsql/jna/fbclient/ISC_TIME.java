package org.firebirdsql.jna.fbclient;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class ISC_TIME extends Structure implements Structure.ByValue
{
    public int value;

    @Override
    protected List<String> getFieldOrder()
    {
        return Arrays.asList("value");
    }
}
