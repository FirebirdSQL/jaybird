// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   GetLongs.java

package jdbcTest.resultSet;

import java.io.IOException;
import java.io.InputStream;

// Referenced classes of package jdbcTest.resultSet:
//            CharInputStream, GetLongs

class BinaryInputStream extends InputStream
{

    public BinaryInputStream(byte abyte0[], int i)
    {
        charCount = 0;
        chunkCount = 0;
        chunk = abyte0;
        length = i;
    }

    public int read()
        throws IOException
    {
        if(charCount++ < length)
        {
            if(chunkCount > chunk.length - 1)
                chunkCount = 0;
            return chunk[chunkCount++];
        } else
        {
            return -1;
        }
    }

    private byte chunk[];
    private int charCount;
    private int chunkCount;
    private int length;
}
