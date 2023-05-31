package org.firebirdsql.jna.fbclient;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * JNA wrapper for ISC_TIMESTAMP_TZ.
 * <p>
 * This file was modified manually, <strong>do not automatically regenerate!</strong>
 * </p>
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 6.0
 */
public class ISC_TIMESTAMP_TZ extends Structure implements Structure.ByValue {

    public ISC_TIMESTAMP timestamp;
    public int time_zone;

    @Override
    protected List<String> getFieldOrder()
    {
        return Arrays.asList("timestamp", "time_zone");
    }
}
