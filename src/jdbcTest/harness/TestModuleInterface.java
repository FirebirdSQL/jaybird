// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   TestModuleInterface.java

package jdbcTest.harness;

import java.sql.*;

// Referenced classes of package jdbcTest.harness:
//            TestCaseInterface

public interface TestModuleInterface
{

    public abstract boolean assert(boolean flag)
        throws Exception;

    public abstract boolean assert(boolean flag, String s)
        throws Exception;

    public abstract void compareResults(String s);

    public abstract void doSetup()
        throws SQLException, Exception;

    public abstract void exception(SQLException sqlexception);

    public abstract void execTestCase(TestCaseInterface testcaseinterface);

    public abstract void executeSQL(String s)
        throws SQLException;

    public abstract void executeSQL(Connection connection, String s)
        throws SQLException;

    public abstract void executeSQL(Connection connection, String as[])
        throws SQLException;

    public abstract void executeSQL(String as[])
        throws SQLException;

    public abstract void failed();

    public abstract String getDescription();

    public abstract String getPassword();

    public abstract String getSignon();

    public abstract String getUrl();

    public abstract void loadResourceFile();

    public abstract void logResultSet(ResultSet resultset);

    public abstract void passed();

    public abstract void result(byte byte0);

    public abstract void result(char c);

    public abstract void result(double d);

    public abstract void result(int i);

    public abstract void result(long l);

    public abstract void result(Object obj);

    public abstract void result(String s);

    public abstract void result(Connection connection);

    public abstract void result(ResultSet resultset);

    public abstract void result(Statement statement);

    public abstract void result(boolean flag);

    public abstract void setDescription(String s);

    public abstract void stopTestCase(TestCaseInterface testcaseinterface);

    public abstract void test(Object obj, String s);

    public abstract boolean verify(boolean flag);

    public abstract boolean verify(boolean flag, String s);
}
