// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setMaxFieldSize.java

package jdbcTest.preparedStatement;

import java.io.IOException;
import java.io.InputStream;

// Referenced classes of package jdbcTest.preparedStatement:
//            setMaxFieldSize

class CharInputStream extends InputStream
{

    public CharInputStream(String s, int i)
    {
        charCount = 0;
        chunkCount = 0;
        chunk = s;
        length = i;
    }

    public int read()
        throws IOException
    {
        if(charCount++ < length)
        {
            if(chunkCount > chunk.length() - 1)
                chunkCount = 0;
            return chunk.charAt(chunkCount++);
        } else
        {
            return -1;
        }
    }

    private String chunk;
    private int charCount;
    private int chunkCount;
    private int length;
}
