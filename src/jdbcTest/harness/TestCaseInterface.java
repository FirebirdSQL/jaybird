// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   TestCaseInterface.java

package jdbcTest.harness;

import java.sql.*;

public interface TestCaseInterface
{

    public abstract boolean assert(boolean flag)
        throws Exception;

    public abstract boolean assert(boolean flag, String s)
        throws Exception;

    public abstract void exception(SQLException sqlexception);

    public abstract void failed();

    public abstract String getID();

    public abstract void logResultSet(ResultSet resultset);

    public abstract void passed();

    public abstract void reset();

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

    public abstract void setFunction(String s);

    public abstract void setObject(Object obj);

    public abstract void test();

    public abstract void test(Object obj, String s);

    public abstract boolean verify(boolean flag);

    public abstract boolean verify(boolean flag, String s);

    public abstract void warnings(SQLWarning sqlwarning);
}
