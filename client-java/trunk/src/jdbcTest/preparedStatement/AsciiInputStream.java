// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setAsciiStream.java

package jdbcTest.preparedStatement;

import java.io.IOException;
import java.io.InputStream;

// Referenced classes of package jdbcTest.preparedStatement:
//            setAsciiStream

class AsciiInputStream extends InputStream
{

    public AsciiInputStream()
    {
        sValue = "cde       ";
        bValue = new byte[10];
        count = 0;
        length = sValue.length();
        sValue.getBytes(0, sValue.length(), bValue, 0);
    }

    public int read()
        throws IOException
    {
        if(count < length)
            return bValue[count++];
        else
            return -1;
    }

    private String sValue;
    private byte bValue[];
    private int count;
    private int length;
}
