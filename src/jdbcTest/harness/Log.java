// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   Log.java

package jdbcTest.harness;

import java.io.*;

// Referenced classes of package jdbcTest.harness:
//            Console

public class Log
{

    public Log()
    {
    }

    public static void printOnAll(String s)
    {
        out.println(s + "\n");
        passfail.println(s + "\n");
        Console.println(s);
    }

    public static void println(String s)
    {
        out.println(s + "\n");
        Console.println(s);
    }

    public static void setOutputFile(String s)
    {
        try
        {
            FileOutputStream fileoutputstream = new FileOutputStream(s);
            out = new PrintStream(new BufferedOutputStream(fileoutputstream, 128), true);
        }
        catch(Exception _ex)
        {
            throw new Error("unable to set output file to " + s);
        }
    }

    public static void setPassFailFile(String s)
    {
        try
        {
            FileOutputStream fileoutputstream = new FileOutputStream(s);
            passfail = new PrintStream(new BufferedOutputStream(fileoutputstream, 128), true);
        }
        catch(Exception _ex)
        {
            throw new Error("unable to set pass fail file to " + s);
        }
    }

    public static PrintStream out;
    public static PrintStream passfail;

    static
    {
        try
        {
            out = new PrintStream(new BufferedOutputStream(new FileOutputStream(FileDescriptor.out), 128), true);
            passfail = new PrintStream(new BufferedOutputStream(new FileOutputStream(FileDescriptor.out), 128), true);
        }
        catch(Exception _ex)
        {
            throw new Error("can't initialize logs to stdio");
        }
    }
}
