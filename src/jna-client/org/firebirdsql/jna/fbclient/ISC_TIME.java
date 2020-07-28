package org.firebirdsql.jna.fbclient;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * JNA wrapper for ISC_TIME.
 * <p>
 * This file was modified manually, <strong>do not automatically regenerate!</strong>
 * </p>
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public class ISC_TIME extends Structure implements Structure.ByValue
{
    public int value;

    @Override
    protected List<String> getFieldOrder()
    {
        return Arrays.asList("value");
    }
}
