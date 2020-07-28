package org.firebirdsql.jna.fbclient;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * JNA wrapper for FB_I128.
 * <p>
 * This file was modified manually, <strong>do not automatically regenerate!</strong>
 * </p>
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public class FB_I128 extends Structure {
    public long[] fb_data = new long[2];
    public FB_I128() {
        super();
    }
    protected List getFieldOrder() {
        return Arrays.asList("fb_data");
    }
    public FB_I128(long fb_data[]) {
        super();
        if ((fb_data.length != this.fb_data.length))
            throw new IllegalArgumentException("Wrong array size !");
        this.fb_data = fb_data;
    }
    public FB_I128(Pointer peer) {
        super(peer);
    }
    public static class ByReference extends FB_I128 implements Structure.ByReference {

    };
    public static class ByValue extends FB_I128 implements Structure.ByValue {

    };
}
