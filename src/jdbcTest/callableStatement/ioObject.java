// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ioObject.java

package jdbcTest.callableStatement;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import jdbcTest.harness.TestModule;

public class ioObject extends TestModule
{

    public ioObject()
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
                    CallableStatement callablestatement = connection.prepareCall("{call JDBC_IO_INT(?)}");
                    Integer integer = new Integer(0x3b9aca00);
                    test(callablestatement, "setObject()");
                    callablestatement.setObject(1, integer);
                    callablestatement.registerOutParameter(1, 4);
                    callablestatement.executeUpdate();
                    int i = callablestatement.getInt(1);
                    result(i);
                    verify(i == 0x77359400, "The output value must be 2000000000");
                }
                if(getSupportedSQLType(1) == null)
                {
                    result("Does not support CHAR SQL type");
                } else
                {
                    CallableStatement callablestatement1 = connection.prepareCall("{call JDBC_IO_STRING(?)}");
                    String s = new String("cde   ");
                    test(callablestatement1, "setObject()");
                    callablestatement1.setObject(1, s, 1);
                    callablestatement1.registerOutParameter(1, 1);
                    callablestatement1.executeUpdate();
                    String s2 = callablestatement1.getString(1);
                    result(s2);
                    verify(s2.equals("fgh   "), "The output value must be 'fgh   '");
                }
                if(getSupportedSQLType(12) == null)
                {
                    result("Does not support VARCHAR SQL type");
                } else
                {
                    CallableStatement callablestatement2 = connection.prepareCall("{call JDBC_IO_VSTRING(?)}");
                    String s1 = new String("cde");
                    test(callablestatement2, "setObject()");
                    callablestatement2.setObject(1, s1, 1);
                    callablestatement2.registerOutParameter(1, 1);
                    callablestatement2.executeUpdate();
                    String s3 = callablestatement2.getString(1);
                    result(s3);
                    verify(s3.equals("fgh"), "The output value must be 'fgh'");
                }
                if(getSupportedSQLType(-2) == null)
                {
                    result("Does not support BINARY SQL type");
                } else
                {
                    CallableStatement callablestatement3 = connection.prepareCall("{call JDBC_IO_BYTES(?)}");
                    byte abyte0[] = {
                        102, 103, 104, 105, 106, 107
                    };
                    test(callablestatement3, "setObject()");
                    callablestatement3.setObject(1, abyte0, -2);
                    callablestatement3.registerOutParameter(1, -2);
                    callablestatement3.executeUpdate();
                    byte abyte2[] = callablestatement3.getBytes(1);
                    result(abyte2);
                    verify(abyte2[0] == 106 && abyte2[1] == 107 && abyte2.length == 10, "The bytes output must be 0x6A6B");
                }
                if(getSupportedSQLType(-3) == null)
                {
                    result("Does not support VARBINARY SQL type");
                } else
                {
                    CallableStatement callablestatement4 = connection.prepareCall("{call JDBC_IO_VBYTES(?)}");
                    byte abyte1[] = {
                        102, 103, 104, 105, 106, 107
                    };
                    test(callablestatement4, "setObject()");
                    callablestatement4.setObject(1, abyte1);
                    callablestatement4.registerOutParameter(1, -3);
                    callablestatement4.executeUpdate();
                    byte abyte3[] = callablestatement4.getBytes(1);
                    result(abyte3);
                    verify(abyte3[0] == 106 && abyte3[1] == 107 && abyte3.length == 2, "The bytes output must be 0x6A6B");
                }
                if(getSupportedSQLType(2) == null && getSupportedSQLType(3) == null)
                {
                    result("Does not support NUMERIC or DECIMAL SQL type");
                } else
                {
                    CallableStatement callablestatement5 = connection.prepareCall("{call JDBC_IO_BIGDECIMAL(?)}");
                    BigDecimal bigdecimal = new BigDecimal("56789.12340");
                    test(callablestatement5, "setObject()");
                    callablestatement5.setObject(1, bigdecimal);
                    callablestatement5.registerOutParameter(1, 2);
                    callablestatement5.executeUpdate();
                    BigDecimal bigdecimal1 = callablestatement5.getBigDecimal(1, 5);
                    result(bigdecimal1);
                    verify(bigdecimal1.equals(new BigDecimal("12340.12340")), "BigDecimal value must be 12340.1234");
                }
                if(getSupportedSQLType(8) == null)
                {
                    result("Does not support DOUBLE SQL type");
                } else
                {
                    CallableStatement callablestatement6 = connection.prepareCall("{call JDBC_IO_DOUBLE(?)}");
                    test(callablestatement6, "setObject(1, new Double(9.4247123456789)");
                    callablestatement6.setObject(1, new Double(9.4247123456789001D));
                    callablestatement6.registerOutParameter(1, 8);
                    callablestatement6.executeUpdate();
                    double d = callablestatement6.getDouble(1);
                    result(d);
                    verify(d > 6.4247123456787998D && d < 6.4247123456789996D, "The output value must be ~6.4247123456789");
                }
                if(getSupportedSQLType(7) == null)
                {
                    result("Does not support REAL SQL type");
                } else
                {
                    CallableStatement callablestatement7 = connection.prepareCall("{call JDBC_IO_FLOAT(?)}");
                    test(callablestatement7, "setObject()");
                    callablestatement7.setObject(1, new Float(9.4247119999999995D));
                    callablestatement7.registerOutParameter(1, 7);
                    callablestatement7.executeUpdate();
                    float f = callablestatement7.getFloat(1);
                    result(f);
                    verify((double)f > 6.4247110000000003D && (double)f < 6.4247129999999997D, "The output value must be ~6.424712");
                }
                if(getSupportedSQLType(-7) == null)
                {
                    result("Does not support BIT SQL type");
                } else
                {
                    CallableStatement callablestatement8 = connection.prepareCall("{call JDBC_IO_BOOLEAN(?)}");
                    Boolean boolean1 = new Boolean(true);
                    test(callablestatement8, "setObject()");
                    callablestatement8.setObject(1, boolean1);
                    callablestatement8.registerOutParameter(1, -7);
                    callablestatement8.executeUpdate();
                    boolean flag = callablestatement8.getBoolean(1);
                    result(flag);
                    verify(!flag, "Output value must be false");
                }
                if(getSupportedSQLType(5) == null)
                {
                    result("Does not support SMALLINT SQL type");
                } else
                {
                    CallableStatement callablestatement9 = connection.prepareCall("{call JDBC_IO_SHORT(?)}");
                    Integer integer1 = new Integer(-2);
                    test(callablestatement9, "setObject()");
                    callablestatement9.setObject(1, integer1, 5);
                    callablestatement9.registerOutParameter(1, 5);
                    callablestatement9.executeUpdate();
                    short word0 = callablestatement9.getShort(1);
                    result(word0);
                    verify(word0 == -4, "The output value must be -4");
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
                    callablestatement10.registerOutParameter(1, 91);
                    callablestatement10.executeUpdate();
                    Date date1 = callablestatement10.getDate(1);
                    result(date1);
                    verify(date1.equals(Date.valueOf("1982-05-05")), "Date output value must be 1982-05-05");
                }
                if(getSupportedSQLType(92) == null)
                {
                    result("Does not support TIME SQL type");
                } else
                {
                    CallableStatement callablestatement11 = connection.prepareCall("{call JDBC_IO_TIME(?)}");
                    Time time = Time.valueOf("02:02:02");
                    test(callablestatement11, "setObject()");
                    callablestatement11.setObject(1, time);
                    callablestatement11.registerOutParameter(1, 92);
                    callablestatement11.executeUpdate();
                    Time time1 = callablestatement11.getTime(1);
                    result(time1);
                    verify(time1.equals(Date.valueOf("04:04:04")), "Time output value must be 04:04:04");
                }
                if(getSupportedSQLType(93) == null)
                {
                    result("Does not support TIMESTAMP SQL type");
                } else
                {
                    CallableStatement callablestatement12 = connection.prepareCall("{call JDBC_IO_TIMESTAMP(?)}");
                    Timestamp timestamp = Timestamp.valueOf("1982-03-03 02:02:02");
                    test(callablestatement12, "setObject()");
                    callablestatement12.setObject(1, timestamp);
                    callablestatement12.registerOutParameter(1, 93);
                    callablestatement12.executeUpdate();
                    Timestamp timestamp1 = callablestatement12.getTimestamp(1);
                    result(timestamp1);
                    verify(timestamp1.equals(Timestamp.valueOf("1982-05-05 04:04:04")), "Timestamp output value must be 1982-05-05 04:04:04");
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
