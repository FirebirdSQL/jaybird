// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   TestModule.java

package jdbcTest.harness;

import java.sql.*;
import java.util.Enumeration;
import java.util.Vector;

// Referenced classes of package jdbcTest.harness:
//            AssertionException, INIFile, Log, SupportedSQLType,
//            TestCase, TestDriverInterface, TestModuleInterface, TestCaseInterface

public abstract class TestModule
    implements Runnable, TestModuleInterface
{

    public TestModule()
    {
        tmTestDriver = null;
        tmThread = null;
        tmThreadGroup = null;
        tmDesc = null;
        verified = true;
        stopping = false;
        tmModuleName = getClass().getName();
        typesSupported = null;
        supportedType = null;
        tmURL = null;
        tmPassWord = null;
        tmSignonID = null;
    }

    public boolean assert(boolean flag)
        throws AssertionException
    {
        if(executingTestCase != null)
            executingTestCase.assert(flag, "");
        return flag;
    }

    public boolean assert(boolean flag, String s)
        throws AssertionException
    {
        if(executingTestCase != null)
            executingTestCase.assert(flag, s);
        return flag;
    }

    public void compareResults(String s)
    {
    }

    public final TestCase createTestCase(String s)
    {
        return new TestCase(this, s);
    }

    public final TestCase createTestCase(String s, String s1)
    {
        return new TestCase(this, s, s1);
    }

    public void doCleanup()
    {
        Log.println(tmModuleName + ": Running cleanup");
        if(tmResources != null)
            try
            {
                Vector vector = tmResources.getSectionValues("Cleanup");
                if(vector == null)
                    return;
                String as[] = new String[vector.size()];
                int i = 0;
                for(Enumeration enumeration = vector.elements(); enumeration.hasMoreElements();)
                    as[i++] = ((String)enumeration.nextElement()).trim();

                trySQL(as);
            }
            catch(Exception exception1)
            {
                Log.println(tmModuleName + ": Exception on cleanup");
                exception1.printStackTrace(Log.out);
            }
    }

    public void doSetup()
        throws SQLException, Exception
    {
        Log.println(tmModuleName + ": Running setup");
        if(tmResources == null)
            return;
        try
        {
            Vector vector = tmResources.getSectionValues("Setup");
            if(vector == null)
                return;
            String as[] = new String[vector.size()];
            int i = 0;
            for(Enumeration enumeration = vector.elements(); enumeration.hasMoreElements();)
            {
                as[i] = ((String)enumeration.nextElement()).trim();
                i++;
            }

            executeSQL(as);
        }
        catch(SQLException sqlexception)
        {
            exception(sqlexception);
            throw sqlexception;
        }
        catch(Exception exception1)
        {
            exception1.printStackTrace(Log.out);
            throw exception1;
        }
    }

    public void exception(Exception exception1)
    {
        if(executingTestCase != null)
        {
            executingTestCase.exception(exception1);
        } else
        {
            Log.println("\n" + tmModuleName + ": *** Exception caught ***\n");
            Log.println(tmModuleName + ": Message: " + exception1.getMessage());
            failed();
        }
    }

    public void exception(SQLException sqlexception)
    {
        if(executingTestCase != null)
        {
            executingTestCase.exception(sqlexception);
        } else
        {
            Log.println("\n" + tmModuleName + ": *** SQLException caught ***\n");
            Log.println(tmModuleName + ": Desc: " + tmDesc);
            for(; sqlexception != null; sqlexception = sqlexception.getNextException())
            {
                String s = new String();
                s = tmModuleName + ": SQLState: " + sqlexception.getSQLState();
                Log.println(s);
                s = tmModuleName + ": Message:  " + sqlexception.getMessage();
                Log.println(s);
                s = tmModuleName + ": Vendor:   " + sqlexception.getErrorCode();
                Log.println(s);
            }

            failed();
        }
    }

    public void execTestCase(TestCase testcase)
    {
        executingTestCase = testcase;
    }

    public void execTestCase(TestCaseInterface testcaseinterface)
    {
        executingTestCase = (TestCase)testcaseinterface;
    }

    public void executeSQL(String s)
        throws SQLException
    {
        Connection connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
        executeSQL(connection, s);
        connection.close();
    }

    public void executeSQL(Connection connection, String s)
        throws SQLException
    {
        Log.println(tmModuleName + ": Executing Sql:" + s);
        Object obj = null;
        Connection connection1 = connection;
        if(connection1 == null)
            connection1 = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
        Statement statement = connection1.createStatement();
        int i = statement.executeUpdate(s);
        statement.close();

        //david jencks8-12-2001 for firebird create table semantics
        if (s.startsWith("create")||s.startsWith("drop")||s.startsWith("alter")) {
            connection.commit();
        }
        //david jencks end
        if(connection == null)
        {
            connection1.close();
            connection1 = null;
        }
    }

    public void executeSQL(Connection connection, String as[])
        throws SQLException
    {
        Connection connection1 = connection;
        if(connection1 == null)
            connection1 = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
        for(int i = 0; i < as.length; i++)
            executeSQL(connection1, as[i]);

        if(connection == null)
        {
            connection1.close();
            connection1 = null;
        }
    }

    public void executeSQL(String as[])
        throws SQLException
    {
        Connection connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
        executeSQL(connection, as);
        connection.close();
    }

    public void failed()
    {
        if(executingTestCase != null)
            executingTestCase.failed();
    }

    private int foundSupportedType(int i)
    {
        int j = -1;
        for(int k = 0; k < typeValues.length; k++)
            if(typeValues[k] == i)
                j = k;

        return j;
    }

    public String getDescription()
    {
        return tmDesc;
    }

    public String getPassword()
    {
        return tmPassWord;
    }

    public String getSignon()
    {
        return tmSignonID;
    }

    public SupportedSQLType getSupportedSQLType(int i)
        throws SQLException
    {
        if(typesSupported == null)
        {
            typesSupported = new boolean[typeValues.length];
            supportedType = new SupportedSQLType[typesSupported.length];
            for(int j = 0; j < typesSupported.length; j++)
                typesSupported[j] = false;

            Connection connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            ResultSet resultset;
            for(resultset = databasemetadata.getTypeInfo(); resultset.next();)
            {
                String s = resultset.getString(1);
                int l = resultset.getInt(2);
                int i1 = foundSupportedType(l);
                if(i1 >= 0 && !typesSupported[i1])
                {
                    typesSupported[i1] = true;
                    supportedType[i1] = new SupportedSQLType(s, (short)l, resultset.getInt(3), resultset.getString(4), resultset.getString(5), resultset.getString(6), resultset.getShort(7), resultset.getBoolean(8), resultset.getShort(9), resultset.getBoolean(10), resultset.getBoolean(11), resultset.getBoolean(12), resultset.getString(13), resultset.getShort(14), resultset.getShort(15), resultset.getInt(18));
                }
            }

            resultset.close();
            connection.close();
        }
        int k = foundSupportedType(i);
        if(k >= 0 && typesSupported[k])
            return (SupportedSQLType)supportedType[k].clone();
        else
            return null;
    }

    public String getUrl()
    {
        return tmURL;
    }

    public void loadResourceFile()
    {
        try
        {
            String s = new String(getClass().getName());
            String s1 = tmTestDriver.getString("Resources", "Path", "");
            s = s + ".ini";
            tmResources = new INIFile(s, s1);
        }
        catch(Exception _ex)
        {
            tmResources = null;
            Log.println(tmModuleName + ": No resource file found for " + getClass().getName());
        }
    }

    public void logResultSet(ResultSet resultset)
    {
        if(executingTestCase != null)
            executingTestCase.logResultSet(resultset);
    }

    public boolean next(ResultSet resultset)
        throws SQLException
    {
        boolean flag;
        if(resultset == null)
        {
            verify(false, "The ResultSet should not be null");
            flag = false;
        } else
        {
            flag = resultset.next();
            if(!flag)
                verify(false, "The ResultSet should contain this row");
        }
        return flag;
    }

    public void passed()
    {
        if(executingTestCase != null)
            if(verified)
                executingTestCase.passed();
            else
                executingTestCase.failed();
    }

    public void result(byte byte0)
    {
        if(executingTestCase != null)
            executingTestCase.result(byte0);
    }

    public void result(char c)
    {
        if(executingTestCase != null)
            executingTestCase.result(c);
    }

    public void result(double d)
    {
        if(executingTestCase != null)
            executingTestCase.result(d);
    }

    public void result(int i)
    {
        if(executingTestCase != null)
            executingTestCase.result(i);
    }

    public void result(long l)
    {
        if(executingTestCase != null)
            executingTestCase.result(l);
    }

    public void result(Object obj)
    {
        if(executingTestCase != null)
            executingTestCase.result(obj);
    }

    public void result(String s)
    {
        if(executingTestCase != null)
            executingTestCase.result(s);
    }

    public void result(Connection connection)
    {
        if(executingTestCase != null)
            executingTestCase.result(connection);
    }

    public void result(ResultSet resultset)
    {
        if(executingTestCase != null)
            executingTestCase.result(resultset);
    }

    public void result(Statement statement)
    {
        if(executingTestCase != null)
            executingTestCase.result(statement);
    }

    public void result(boolean flag)
    {
        if(executingTestCase != null)
            executingTestCase.result(flag);
    }

    public abstract void run();

    public void setDescription(String s)
    {
        tmDesc = s;
    }

    public final void setDriver(TestDriverInterface testdriverinterface, ThreadGroup threadgroup, String s, String s1, String s2)
    {
        tmTestDriver = testdriverinterface;
        tmURL = s;
        tmPassWord = s2;
        tmSignonID = s1;
        tmThreadGroup = threadgroup;
    }

    public final void start()
    {
        if(tmThread == null)
        {
            Log.println(tmModuleName + ": Starting Module " + tmDesc);
            loadResourceFile();
            tmThread = new Thread(tmThreadGroup, this, getClass().getName());
            tmThread.start();
        }
    }

    public final void stop()
    {
        if(tmThread != null)
        {
            if(tmTestDriver != null)
            {
                stopping = true;
                synchronized(tmTestDriver)
                {
                    tmTestDriver.notify();
                }
            }
            Log.println(tmModuleName + ": Stopping Module " + tmDesc);
            tmThread.stop();
            tmThread = null;
        }
    }

    public void stopTestCase(TestCaseInterface testcaseinterface)
    {
        executingTestCase = null;
    }

    public void test(Object obj, String s)
    {
        if(executingTestCase != null)
            executingTestCase.test(obj, s);
    }

    public void trySQL(String s)
    {
        try
        {
            Connection connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            executeSQL(connection, s);
            connection.close();
        }
        catch(SQLException _ex) { }
        catch(Exception _ex) { }
    }

    public void trySQL(Connection connection, String s)
    {
        Log.println(tmModuleName + ": Executing Sql:" + s);
        Connection connection1 = connection;
        try
        {
            if(connection == null)
                connection1 = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            Statement statement = connection1.createStatement();
            int i = statement.executeUpdate(s);
            statement.close();
            if(connection == null)
            {
                connection1.close();
                connection1 = null;
            }
        }
        catch(SQLException _ex) { }
        catch(Exception _ex) { }
    }

    public void trySQL(Connection connection, String as[])
    {
        if(connection == null)
        {
            try
            {
                connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
                for(int i = 0; i < as.length; i++)
                    trySQL(connection, as[i]);

                connection.close();
                connection = null;
            }
            catch(SQLException _ex) { }
            catch(Exception _ex) { }
        } else
        {
            for(int j = 0; j < as.length; j++)
                trySQL(connection, as[j]);

        }
    }

    public void trySQL(String as[])
    {
        try
        {
            Connection connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            for(int i = 0; i < as.length; i++)
                try
                {
                    executeSQL(connection, as[i]);
                }
                catch(SQLException _ex) { }

            connection.close();
            connection = null;
        }
        catch(SQLException _ex) { }
        catch(Exception _ex) { }
    }

    public boolean verify(boolean flag)
    {
        if(executingTestCase != null)
        {
            if(!flag)
                verified = false;
            executingTestCase.verify(flag, "");
        }
        return flag;
    }

    public boolean verify(boolean flag, String s)
    {
        if(executingTestCase != null)
        {
            if(!flag)
                verified = false;
            executingTestCase.verify(flag, s);
        }
        return flag;
    }

    private TestDriverInterface tmTestDriver;
    private String tmURL;
    private String tmPassWord;
    private String tmSignonID;
    private Thread tmThread;
    private ThreadGroup tmThreadGroup;
    private String tmDesc;
    private TestCase executingTestCase;
    private INIFile tmResources;
    private boolean verified;
    boolean stopping;
    String tmModuleName;
    static int typeValues[] = {
        -7, -6, 5, 4, -5, 6, 7, 8, 2, 3,
        1, 12, -1, 91, 92, 93, -2, -3, -4, 0,
        1111
    };
    boolean typesSupported[];
    SupportedSQLType supportedType[];

}
