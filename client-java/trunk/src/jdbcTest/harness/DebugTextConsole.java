// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   DebugTextConsole.java

package jdbcTest.harness;

import java.awt.TextArea;
import java.awt.TextComponent;

// Referenced classes of package jdbcTest.harness:
//            ConsoleOutput

public class DebugTextConsole extends TextArea
    implements ConsoleOutput
{

    public DebugTextConsole()
    {
        super("", 10000, 120);
        setEditable(false);
    }

    public void Clear()
    {
        replaceText("", 0, getText().length());
    }

    public void Print(String s)
    {
        insertText(s, getText().length());
    }
}
