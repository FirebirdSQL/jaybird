// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   CreateTables.java

package jdbcTest.data;

import java.sql.Connection;
import java.sql.SQLException;
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
            jdbcTest.harness.TestCase testcase = createTestCase("Test data.CreateTables");
            execTestCase(testcase);
            doSetup();
            passed();
        }
        catch(SQLException sqlexception)
        {
            exception(sqlexception);
        }
        catch(Exception exception1)
        {
            exception1.printStackTrace();
            failed();
        }
        finally
        {
            /*try//wtf??
            {
                if(obj != null)
                    obj.close();
            }
            catch(SQLException _ex) { }*/
            stop();
        }
    }
}
