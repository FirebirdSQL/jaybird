// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   CreateTables.java

package jdbcTest.example;

import jdbcTest.harness.TestModule;

public class CreateTables extends TestModule
{

    public CreateTables()
    {
    }

    public void run()
    {
        Object obj = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test example.CreateTables");
            execTestCase(testcase);
            doSetup();
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
