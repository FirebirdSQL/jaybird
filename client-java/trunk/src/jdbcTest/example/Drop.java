// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   Drop.java

package jdbcTest.example;

import jdbcTest.harness.TestModule;

public class Drop extends TestModule
{

    public Drop()
    {
    }

    public void run()
    {
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test example.Drop");
            execTestCase(testcase);
            doCleanup();
            passed();
        }
        catch(Exception exception1)
        {
            exception(exception1);
        }
        finally
        {
            stop();
        }
    }
}
