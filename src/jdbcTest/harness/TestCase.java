// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   TestCase.java

package jdbcTest.harness;

import java.io.PrintStream;
import java.sql.*;

// Referenced classes of package jdbcTest.harness:
//            AssertionException, Log, TestCaseInterface, TestModuleInterface

public class TestCase
    implements TestCaseInterface
{

    public TestCase(TestModuleInterface testmoduleinterface)
    {
        tcResult = "";
        tcModule = testmoduleinterface;
        tcDesc = tcModule.getDescription();
        tcID = genID();
        tcModuleName = tcModule.getClass().getName();
    }

    public TestCase(TestModuleInterface testmoduleinterface, String s)
    {
        tcResult = "";
        tcDesc = s;
        tcModule = testmoduleinterface;
        tcID = genID();
        tcModuleName = tcModule.getClass().getName();
    }

    public TestCase(TestModuleInterface testmoduleinterface, String s, String s1)
    {
        tcResult = "";
        tcDesc = s1;
        tcModule = testmoduleinterface;
        tcID = s;
        tcModuleName = tcModule.getClass().getName();
    }

    public boolean assert(boolean flag)
        throws AssertionException
    {
        return assert(flag, "");
    }

    public boolean assert(boolean flag, String s)
        throws AssertionException
    {
        if(flag)
        {
            Log.println(tcModuleName + ": Assertion: PASSED  Text:" + s);
        } else
        {
            Log.printOnAll(tcModuleName + ": Assertion: FAILED  Text:" + s);
            throw new AssertionException("");
        }
        return flag;
    }

    public void exception(Exception exception1)
    {
        if(exception1 instanceof SQLException)
            exception((SQLException)exception1);
        else
        if(exception1 instanceof AssertionException)
        {
            failed();
        } else
        {
            Log.println("\n" + tcModuleName + ": *** Exception caught ***\n");
            Log.println(tcModuleName + ": " + exception1.getMessage());
            Log.println(tcModuleName + ": Object: " + tcObject);
            Log.println(tcModuleName + ": Function: " + tcFunction);
            exception1.printStackTrace(Log.out);
            Log.passfail.println(tcModuleName + ": " + exception1.getMessage());
            failed();
        }
    }

    public void exception(SQLException sqlexception)
    {
        Log.println("\n" + tcModuleName + ": *** SQLException caught ***\n");
        Log.println(tcModuleName + ": Object: " + tcObject);
        Log.println(tcModuleName + ": Function: " + tcFunction);
        for(; sqlexception != null; sqlexception = sqlexception.getNextException())
        {
            String s = new String();
            s = tcModuleName + ": SQLState: " + sqlexception.getSQLState();
            Log.println(s);
            s = tcModuleName + ": Message:  " + sqlexception.getMessage();
            Log.println(s);
            s = tcModuleName + ": Vendor:   " + sqlexception.getErrorCode();
            Log.println(s);
            sqlexception.printStackTrace(Log.out);
            Log.passfail.println(tcModuleName + ": " + sqlexception.getMessage());
        }

        failed();
    }

    public void failed()
    {
        Log.printOnAll(tcModuleName + ": TestCase:  **** FAILED **** ");
    }

    private String formatIdentifier()
    {
        return "Module: " + tcModuleName + " TestCase: " + tcID + " ";
    }

    private synchronized String genID()
    {
        return "TC" + ++caseCount;
    }

    public String getID()
    {
        return new String(tcID);
    }

    public void logResultSet(ResultSet resultset)
    {
        try
        {
            if(resultset == null)
            {
                Log.out.println("**** ResultSet is null ****");
                return;
            }
            String s = new String();
            ResultSetMetaData resultsetmetadata = resultset.getMetaData();
            int i = resultsetmetadata.getColumnCount();
            s = "";
            for(int j = 1; j <= i; j++)
            {
                if(j > 1)
                    s = s + ",";
                s = s + resultsetmetadata.getColumnLabel(j);
            }

            Log.out.println(s);
            int k = 0;
            String s1;
            for(; resultset.next(); Log.out.println(s1))
            {
                if(k++ > 25)
                    break;
                s1 = "";
                for(int l = 1; l <= i; l++)
                {
                    if(l > 1)
                        s1 = s1 + ",";
                    s1 = s1 + resultset.getString(l);
                }

            }

        }
        catch(SQLException sqlexception)
        {
            sqlexception.printStackTrace(Log.out);
        }
    }

    public void passed()
    {
        Log.printOnAll(tcModuleName + ": TestCase:  PASSED  ");
    }

    public void reset()
    {
        tcModule.stopTestCase(this);
    }

    public void result(byte byte0)
    {
        result(byte0);
    }

    public void result(char c)
    {
        result((new Character(c)).toString());
    }

    public void result(double d)
    {
        result(Double.toString(d));
    }

    public void result(int i)
    {
        result(Integer.toString(i));
    }

    public void result(long l)
    {
        result(Long.toString(l));
    }

    public void result(Object obj)
    {
        if(obj == null)
            result("null");
        else
            result("instance of " + obj.getClass().getName() + " = " + obj.toString());
    }

    public void result(String s)
    {
        if(s == null)
        {
            result("null");
        } else
        {
            tcResult = s;
            Log.println(tcModuleName + ": Returned value = '" + s + "'");
        }
    }

    public void result(Connection connection)
    {
        result("instance of " + connection.getClass().getName());
        try
        {
            SQLWarning sqlwarning = connection.getWarnings();
            if(sqlwarning != null)
            {
                warnings(sqlwarning);
                connection.clearWarnings();
            }
        }
        catch(SQLException _ex)
        {
            return;
        }
    }

    public void result(ResultSet resultset)
    {
        result("instance of " + resultset.getClass().getName());
        try
        {
            SQLWarning sqlwarning = resultset.getWarnings();
            if(sqlwarning != null)
            {
                warnings(sqlwarning);
                resultset.clearWarnings();
            }
        }
        catch(SQLException _ex)
        {
            return;
        }
    }

    public void result(Statement statement)
    {
        result("instance of " + statement.getClass().getName());
        try
        {
            SQLWarning sqlwarning = statement.getWarnings();
            if(sqlwarning != null)
            {
                warnings(sqlwarning);
                statement.clearWarnings();
            }
        }
        catch(SQLException _ex)
        {
            return;
        }
    }

    public void result(boolean flag)
    {
        result((new Boolean(flag)).toString());
    }

    public void setFunction(String s)
    {
        tcFunction = s;
    }

    public void setObject(Object obj)
    {
        if(obj == null)
            tcObject = new String("null");
        else
            tcObject = obj.getClass().getName();
    }

    public void test()
    {
        tcModule.execTestCase(this);
    }

    public void test(Object obj, String s)
    {
        setObject(obj);
        setFunction(s);
        tcModule.execTestCase(this);
        Log.println(tcModuleName + ": Executing Object: " + tcObject + " Function: " + tcFunction);
    }

    public boolean verify(boolean flag)
    {
        return verify(flag, "");
    }

    public boolean verify(boolean flag, String s)
    {
        if(flag)
            Log.println(tcModuleName + ": Assertion: PASSED  Text:" + s);
        else
            Log.printOnAll(tcModuleName + ": Assertion: FAILED  Text:" + s);
        return flag;
    }

    public void warnings(SQLWarning sqlwarning)
    {
        Log.out.println("\n*** SQLWarning ***");
        Log.out.println("Object: " + tcObject + " Function: " + tcFunction);
        for(; sqlwarning != null; sqlwarning = sqlwarning.getNextWarning())
        {
            Log.out.println("SQLState: " + sqlwarning.getSQLState());
            Log.out.println("Message:  " + sqlwarning.getMessage());
            Log.out.println("Vendor:   " + sqlwarning.getErrorCode());
        }

    }

    private String tcFunction;
    private String tcID;
    private TestModuleInterface tcModule;
    private String tcModuleName;
    private String tcDesc;
    private static int caseCount = 0;
    private String tcResult;
    private String tcObject;

}
