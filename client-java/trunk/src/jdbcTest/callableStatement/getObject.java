// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getObject.java

package jdbcTest.callableStatement;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import jdbcTest.harness.TestModule;

public class getObject extends TestModule
{

    public getObject()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        Object obj1 = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.getObject");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
            {
                result("Does not support Stored Procedures");
            } else
            {
                if(getSupportedSQLType(-2) == null)
                {
                    result("Does not support BINARY SQL type");
                } else
                {
                    CallableStatement callablestatement = connection.prepareCall("{call JDBC_GET_BYTES(?)}");
                    callablestatement.registerOutParameter(1, -2);
                    callablestatement.execute();
                    test(callablestatement, "getObject()");
                    byte abyte0[] = (byte[])callablestatement.getObject(1);
                    result(abyte0.length);
                    verify(abyte0.length == 10, "The BINARY length must match (Some databases do not provide the actual length of BINARY out params; in this case a failure is expected)");
                    result(Integer.toString(abyte0[0], 16));
                    result(Integer.toString(abyte0[1], 16));
                    verify(abyte0[0] == 98 && abyte0[1] == 99, "The BINARY value must match");
                }
                if(getSupportedSQLType(-3) == null)
                {
                    result("Does not support VARBINARY SQL type");
                } else
                {
                    CallableStatement callablestatement1 = connection.prepareCall("{call JDBC_GET_VBYTES(?)}");
                    callablestatement1.registerOutParameter(1, -3);
                    callablestatement1.execute();
                    test(callablestatement1, "getObject()");
                    byte abyte1[] = (byte[])callablestatement1.getObject(1);
                    result(abyte1.length);
                    verify(abyte1.length == 2, "The VARBINARY length must match");
                    result(Integer.toString(abyte1[0], 16));
                    result(Integer.toString(abyte1[1], 16));
                    verify(abyte1[0] == 98 && abyte1[1] == 99, "The VARBINARY value must match");
                }
                if(getSupportedSQLType(2) == null && getSupportedSQLType(3) == null)
                {
                    result("Does not support NUMERIC or DECIMAL SQL type");
                } else
                {
                    CallableStatement callablestatement2 = connection.prepareCall("{call JDBC_GET_BIGDECIMAL(?)}");
                    callablestatement2.registerOutParameter(1, 2, 5);
                    callablestatement2.execute();
                    test(callablestatement2, "getObject()");
                    BigDecimal bigdecimal = (BigDecimal)callablestatement2.getObject(1);
                    result(bigdecimal);
                    BigDecimal bigdecimal1 = new BigDecimal("234567.90100");
                    verify(bigdecimal.equals(bigdecimal1), "BigDecimal value must be 234567.90100");
                    callablestatement2.close();
                }
                if(getSupportedSQLType(1) == null)
                {
                    result("Does not support CHAR SQL type");
                } else
                {
                    CallableStatement callablestatement3 = connection.prepareCall("{call JDBC_GET_STRING(?)}");
                    callablestatement3.registerOutParameter(1, 1);
                    callablestatement3.execute();
                    test(callablestatement3, "getObject()");
                    String s = (String)callablestatement3.getObject(1);
                    result(s);
                    verify(s.equals("bc        "), "The CHAR value must be 'bc        ' (Some databases do not provide the actual length of CHAR out params; in this case a failure is expected)");
                    callablestatement3.close();
                }
                if(getSupportedSQLType(12) == null)
                {
                    result("Does not support VARCHAR SQL type");
                } else
                {
                    CallableStatement callablestatement4 = connection.prepareCall("{call JDBC_GET_VSTRING(?)}");
                    callablestatement4.registerOutParameter(1, 12);
                    callablestatement4.execute();
                    test(callablestatement4, "getObject()");
                    String s1 = (String)callablestatement4.getObject(1);
                    result(s1);
                    verify(s1.equals("bc"), "VARCHAR value must be 'bc'");
                    callablestatement4.close();
                }
                if(getSupportedSQLType(-7) == null)
                {
                    result("Does not support BIT SQL type");
                } else
                {
                    CallableStatement callablestatement5 = connection.prepareCall("{call JDBC_GET_BOOLEAN(?)}");
                    callablestatement5.registerOutParameter(1, -7);
                    callablestatement5.execute();
                    test(callablestatement5, "getObject()");
                    Boolean boolean1 = (Boolean)callablestatement5.getObject(1);
                    result(boolean1.booleanValue());
                    verify(boolean1.booleanValue(), "Boolean value must be true");
                    callablestatement5.close();
                }
                if(getSupportedSQLType(-6) == null)
                {
                    result("Does not support TINYINT SQL type");
                } else
                {
                    CallableStatement callablestatement6 = connection.prepareCall("{call JDBC_GET_BYTE(?)}");
                    callablestatement6.registerOutParameter(1, -6);
                    callablestatement6.execute();
                    test(callablestatement6, "getObject()");
                    Integer integer = (Integer)callablestatement6.getObject(1);
                    result(integer.intValue());
                    verify(integer.intValue() == 1, "TINYINT value must be 1");
                    callablestatement6.close();
                }
                if(getSupportedSQLType(5) == null)
                {
                    result("Does not support SMALLINT SQL type");
                } else
                {
                    CallableStatement callablestatement7 = connection.prepareCall("{call JDBC_GET_SHORT(?)}");
                    callablestatement7.registerOutParameter(1, 5);
                    callablestatement7.execute();
                    test(callablestatement7, "getObject()");
                    Integer integer1 = (Integer)callablestatement7.getObject(1);
                    result(integer1.intValue());
                    verify(integer1.intValue() == -2, "SMALLINT value must be -2");
                    callablestatement7.close();
                }
                if(getSupportedSQLType(4) == null)
                {
                    result("Does not support INTEGER SQL type");
                } else
                {
                    CallableStatement callablestatement8 = connection.prepareCall("{call JDBC_GET_INT(?)}");
                    callablestatement8.registerOutParameter(1, 4);
                    callablestatement8.execute();
                    test(callablestatement8, "getObject()");
                    Integer integer2 = (Integer)callablestatement8.getObject(1);
                    result(integer2.intValue());
                    verify(integer2.intValue() == 2, "Integer value must be 2");
                    callablestatement8.close();
                }
                if(getSupportedSQLType(-5) == null)
                {
                    result("Does not support BIGINT SQL type");
                } else
                {
                    CallableStatement callablestatement9 = connection.prepareCall("{call JDBC_GET_LONG(?)}");
                    callablestatement9.registerOutParameter(1, -5);
                    callablestatement9.execute();
                    test(callablestatement9, "getObject()");
                    Long long1 = (Long)callablestatement9.getObject(1);
                    result(long1.longValue());
                    verify(long1.longValue() == 2L, "BIGINT value must be 2");
                    callablestatement9.close();
                }
                if(getSupportedSQLType(8) == null)
                {
                    result("Does not support DOUBLE SQL type");
                } else
                {
                    CallableStatement callablestatement10 = connection.prepareCall("{call JDBC_GET_DOUBLE(?)}");
                    callablestatement10.registerOutParameter(1, 8);
                    callablestatement10.execute();
                    test(callablestatement10, "getObject()");
                    Double double1 = (Double)callablestatement10.getObject(1);
                    result(double1.doubleValue());
                    verify((double)double1.floatValue() > 6.2831000000000001D && (double)double1.floatValue() < 6.2831999999999999D, "Double value must be close to 6.28318");
                    callablestatement10.close();
                }
                if(getSupportedSQLType(7) == null)
                {
                    result("Does not support REAL SQL type");
                } else
                {
                    CallableStatement callablestatement11 = connection.prepareCall("{call JDBC_GET_FLOAT(?)}");
                    callablestatement11.registerOutParameter(1, 7);
                    callablestatement11.execute();
                    test(callablestatement11, "getObject()");
                    Float float1 = (Float)callablestatement11.getObject(1);
                    result(float1.floatValue());
                    verify((double)float1.floatValue() > 1.3903000000000001D && (double)float1.floatValue() < 1.3904000000000001D, "Float value must be close to 1.39036");
                    callablestatement11.close();
                }
                if(getSupportedSQLType(91) == null)
                {
                    result("Does not support DATE SQL type");
                } else
                {
                    CallableStatement callablestatement12 = connection.prepareCall("{call JDBC_GET_DATE(?)}");
                    callablestatement12.registerOutParameter(1, 91);
                    callablestatement12.execute();
                    test(callablestatement12, "getObject()");
                    Date date = (Date)callablestatement12.getObject(1);
                    result(date);
                    verify(date.equals(Date.valueOf("1981-02-02")), "The data value must be 1981-02-02");
                    callablestatement12.close();
                }
                if(getSupportedSQLType(92) == null)
                {
                    result("Does not support TIME SQL type");
                } else
                {
                    CallableStatement callablestatement13 = connection.prepareCall("{call JDBC_GET_TIME(?)}");
                    callablestatement13.registerOutParameter(1, 92);
                    callablestatement13.execute();
                    test(callablestatement13, "getObject()");
                    Time time = (Time)callablestatement13.getObject(1);
                    result(time);
                    verify(time.equals(Time.valueOf("01:01:01")), "Time value must be 01:01:01");
                    callablestatement13.close();
                }
                if(getSupportedSQLType(93) == null)
                {
                    result("Does not support TIMESTAMP SQL type");
                } else
                {
                    CallableStatement callablestatement14 = connection.prepareCall("{call JDBC_GET_TIMESTAMP(?)}");
                    callablestatement14.registerOutParameter(1, 93);
                    callablestatement14.execute();
                    test(callablestatement14, "getObject()");
                    Timestamp timestamp = (Timestamp)callablestatement14.getObject(1);
                    result(timestamp);
                    verify(timestamp.equals(Timestamp.valueOf("1981-02-02 01:01:01")), "The Timestamp value must be 1981-02-02 01:01:01");
                    callablestatement14.close();
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
