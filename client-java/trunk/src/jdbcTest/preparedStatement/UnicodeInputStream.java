// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setUnicodeStream.java

package jdbcTest.preparedStatement;

import java.io.IOException;
import java.io.InputStream;

// Referenced classes of package jdbcTest.preparedStatement:
//            setUnicodeStream

class UnicodeInputStream extends InputStream
{

    public UnicodeInputStream()
    {
        count = 0;
        length = bValue.length;
    }

    public int read()
        throws IOException
    {
        if(count < length)
            return bValue[count++];
        else
            return -1;
    }

    private byte bValue[] = {
        0, 99, 0, 100, 0, 101, 0, 32, 0, 32,
        0, 32, 0, 32, 0, 32, 0, 32, 0, 32
    };
    private int count;
    private int length;
}
