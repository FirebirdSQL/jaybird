// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setObject.java

package jdbcTest.callableStatement;

import java.math.BigDecimal;
import java.sql.*;
import jdbcTest.harness.TestModule;

public class setObject extends TestModule
{

    public setObject()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        Object obj1 = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.setInt");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
            {
                result("Does not support Stored Procedures");
            } else
            {
                if(getSupportedSQLType(4) == null)
                {
                    result("Does not support INTEGER SQL type");
                } else
                {
                    CallableStatement callablestatement = connection.prepareCall("{call JDBC_SET_INT(?,?)}");
                    Integer integer = new Integer(2);
                    test(callablestatement, "setObject()");
                    callablestatement.setObject(1, integer);
                    callablestatement.registerOutParameter(2, 4);
                    callablestatement.execute();
                    int k = callablestatement.getInt(2);
                    result(k);
                    verify(k == 2, "Row 2 for INTEGER must be found");
                    callablestatement.close();
                }
                if(getSupportedSQLType(1) == null)
                {
                    result("Does not support CHAR SQL type");
                } else
                {
                    CallableStatement callablestatement1 = connection.prepareCall("{call JDBC_SET_STRING(?,?)}");
                    String s = new String("cde       ");
                    test(callablestatement1, "setObject()");
                    callablestatement1.setObject(1, s, 1);
                    callablestatement1.registerOutParameter(2, 4);
                    callablestatement1.execute();
                    int l = callablestatement1.getInt(2);
                    result(l);
                    verify(l == 2, "Row 2 for CHAR must be found");
                    callablestatement1.close();
                }
                if(getSupportedSQLType(12) == null)
                {
                    result("Does not support VARCHAR SQL type");
                } else
                {
                    CallableStatement callablestatement2 = connection.prepareCall("{call JDBC_SET_VSTRING(?,?)}");
                    String s1 = new String("cde");
                    test(callablestatement2, "setObject()");
                    callablestatement2.setObject(1, s1);
                    callablestatement2.registerOutParameter(2, 4);
                    callablestatement2.execute();
                    int i1 = callablestatement2.getInt(2);
                    result(i1);
                    verify(i1 == 2, "Row 2 for VARCHAR must be found");
                    callablestatement2.close();
                }
                if(getSupportedSQLType(-2) == null)
                {
                    result("Does not support BINARY SQL type");
                } else
                {
                    CallableStatement callablestatement3 = connection.prepareCall("{call JDBC_SET_BYTES(?,?)}");
                    byte abyte0[] = {
                        99, 100, 101
                    };
                    test(callablestatement3, "setObject()");
                    callablestatement3.setObject(1, abyte0, -2);
                    callablestatement3.registerOutParameter(2, 4);
                    callablestatement3.execute();
                    int j1 = callablestatement3.getInt(2);
                    result(j1);
                    verify(j1 == 2, "Row 2 for BINARY must be found");
                    callablestatement3.close();
                }
                if(getSupportedSQLType(-3) == null)
                {
                    result("Does not support VARBINARY SQL type");
                } else
                {
                    CallableStatement callablestatement4 = connection.prepareCall("{call JDBC_SET_VBYTES(?,?)}");
                    byte abyte1[] = {
                        99, 100, 101
                    };
                    test(callablestatement4, "setObject()");
                    callablestatement4.setObject(1, abyte1);
                    callablestatement4.registerOutParameter(2, 4);
                    callablestatement4.execute();
                    int k1 = callablestatement4.getInt(2);
                    result(k1);
                    verify(k1 == 2, "Row 2 for VARBINARY must be found");
                    callablestatement4.close();
                }
                if(getSupportedSQLType(2) == null && getSupportedSQLType(3) == null)
                {
                    result("Does not support NUMERIC or DECIMAL SQL type");
                } else
                {
                    CallableStatement callablestatement5 = connection.prepareCall("{call JDBC_SET_BIGDECIMAL(?,?)}");
                    BigDecimal bigdecimal = new BigDecimal("234567.90100");
                    test(callablestatement5, "setObject()");
                    callablestatement5.setObject(1, bigdecimal);
                    callablestatement5.registerOutParameter(2, 4);
                    callablestatement5.execute();
                    int l1 = callablestatement5.getInt(2);
                    result(l1);
                    verify(l1 == 2, "Row 2 for NUMERIC must be found");
                    callablestatement5.close();
                }
                if(getSupportedSQLType(8) == null)
                {
                    result("Does not support DOUBLE SQL type");
                } else
                {
                    CallableStatement callablestatement6 = connection.prepareCall("{call JDBC_SET_DOUBLE(?,?)}");
                    test(callablestatement6, "setObject(1, new Double(9.4247)");
                    callablestatement6.setObject(1, new Double(9.4246999999999996D));
                    callablestatement6.registerOutParameter(2, 4);
                    callablestatement6.execute();
                    int i = callablestatement6.getInt(2);
                    result(i);
                    verify(i == 3, "Row 3 for Double must be found");
                    callablestatement6.close();
                }
                if(getSupportedSQLType(7) == null)
                {
                    result("Does not support REAL SQL type");
                } else
                {
                    CallableStatement callablestatement7 = connection.prepareCall("{call JDBC_SET_FLOAT(?,?)}");
                    test(callablestatement7, "setObject()");
                    callablestatement7.setObject(1, new Float(1.3903F));
                    callablestatement7.registerOutParameter(2, 4);
                    callablestatement7.execute();
                    int j = callablestatement7.getInt(2);
                    result(j);
                    verify(j == 2, "Row 2 for REAL must be found");
                }
                if(getSupportedSQLType(-7) == null)
                {
                    result("Does not support BIT SQL type");
                } else
                {
                    CallableStatement callablestatement8 = connection.prepareCall("{call JDBC_SET_BOOLEAN(?,?)}");
                    Boolean boolean1 = new Boolean(true);
                    test(callablestatement8, "setObject()");
                    callablestatement8.setObject(1, boolean1);
                    callablestatement8.registerOutParameter(2, 4);
                    callablestatement8.execute();
                    int i2 = callablestatement8.getInt(2);
                    result(i2);
                    verify(i2 == 1, "Row 1 for BIT must be found");
                    callablestatement8.close();
                }
                if(getSupportedSQLType(5) == null)
                {
                    result("Does not support SMALLINT SQL type");
                } else
                {
                    CallableStatement callablestatement9 = connection.prepareCall("{call JDBC_SET_SHORT(?,?)}");
                    Integer integer1 = new Integer(-2);
                    test(callablestatement9, "setObject()");
                    callablestatement9.setObject(1, integer1, 5);
                    callablestatement9.registerOutParameter(2, 4);
                    callablestatement9.execute();
                    int j2 = callablestatement9.getInt(2);
                    result(j2);
                    verify(j2 == 1, "Row 1 for SMALLINT must be found");
                    callablestatement9.close();
                }
                if(getSupportedSQLType(91) == null)
                {
                    result("Does not support DATE SQL type");
                } else
                {
                    CallableStatement callablestatement10 = connection.prepareCall("{call JDBC_SET_DATE(?,?)}");
                    Date date = Date.valueOf("1982-03-03");
                    test(callablestatement10, "setObject()");
                    callablestatement10.setObject(1, date);
                    callablestatement10.registerOutParameter(2, 4);
                    callablestatement10.execute();
                    int k2 = callablestatement10.getInt(2);
                    result(k2);
                    verify(k2 == 2, "Row 2 for DATE must be found");
                    callablestatement10.close();
                }
                if(getSupportedSQLType(92) == null)
                {
                    result("Does not support TIME SQL type");
                } else
                {
                    CallableStatement callablestatement11 = connection.prepareCall("{call JDBC_SET_TIME(?,?)}");
                    Time time = Time.valueOf("02:02:02");
                    test(callablestatement11, "setObject()");
                    callablestatement11.setObject(1, time);
                    callablestatement11.registerOutParameter(2, 4);
                    callablestatement11.execute();
                    int l2 = callablestatement11.getInt(2);
                    result(l2);
                    verify(l2 == 2, "Row 2 for TIME must be found");
                    callablestatement11.close();
                }
                if(getSupportedSQLType(93) == null)
                {
                    result("Does not support TIMESTAMP SQL type");
                } else
                {
                    CallableStatement callablestatement12 = connection.prepareCall("{call JDBC_SET_TIMESTAMP(?,?)}");
                    Timestamp timestamp = Timestamp.valueOf("1982-03-03 02:02:02");
                    test(callablestatement12, "setObject()");
                    callablestatement12.setObject(1, timestamp);
                    callablestatement12.registerOutParameter(2, 4);
                    callablestatement12.execute();
                    int i3 = callablestatement12.getInt(2);
                    result(i3);
                    verify(i3 == 2, "Row 2 for TIMESTAMP must be found");
                    callablestatement12.close();
                }
            }
            passed();
        }
        catch(Exception exception1)
        {
            exception(exception1);
        }
        finally
        {
            try
            {
                if(connection != null)
                    connection.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}
