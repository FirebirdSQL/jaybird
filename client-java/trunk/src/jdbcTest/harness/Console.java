// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   Console.java

package jdbcTest.harness;

import java.io.PrintStream;

// Referenced classes of package jdbcTest.harness:
//            ConsoleOutput

public class Console
{

    public Console()
    {
    }

    public static synchronized void Clear()
    {
        if(consoleOutput != null)
            consoleOutput.Clear();
    }

    public static synchronized void println(String s)
    {
        if(consoleOutput != null)
            consoleOutput.Print(s + "\n");
        else
            System.out.println(s);
    }

    public static synchronized void setOutput(ConsoleOutput consoleoutput)
    {
        consoleOutput = consoleoutput;
    }

    static ConsoleOutput consoleOutput = null;

}
