// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setBinaryStream.java

package jdbcTest.preparedStatement;

import java.io.IOException;
import java.io.InputStream;

// Referenced classes of package jdbcTest.preparedStatement:
//            setBinaryStream

class BinaryInputStream extends InputStream
{

    public BinaryInputStream()
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
        102, 103, 104, 105, 106, 107
    };
    private int count;
    private int length;
}
