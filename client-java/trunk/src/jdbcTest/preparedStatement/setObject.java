// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setObject.java

package jdbcTest.preparedStatement;

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
            jdbcTest.harness.TestCase testcase = createTestCase("Test PreparedStatement.setInt");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            if(getSupportedSQLType(4) == null)
            {
                result("Does not support INTEGER SQL type");
            } else
            {
                PreparedStatement preparedstatement = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE INTEGERCOL=?");
                Integer integer = new Integer(2);
                test(preparedstatement, "setObject()");
                preparedstatement.setObject(1, integer);
                ResultSet resultset = preparedstatement.executeQuery();
                if(next(resultset))
                {
                    int i = resultset.getInt(1);
                    result(i);
                    verify(i == 2, "The correct row for INTEGER is found");
                }
                preparedstatement.close();
            }
            if(getSupportedSQLType(1) == null)
            {
                result("Does not support CHAR SQL type");
            } else
            {
                PreparedStatement preparedstatement1 = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE CHARCOL=?");
                String s = new String("cde       ");
                test(preparedstatement1, "setObject()");
                preparedstatement1.setObject(1, s);
                ResultSet resultset1 = preparedstatement1.executeQuery();
                if(next(resultset1))
                {
                    int j = resultset1.getInt(1);
                    result(j);
                    verify(j == 2, "The correct row for CHAR is found");
                }
                preparedstatement1.close();
            }
            if(getSupportedSQLType(12) == null)
            {
                result("Does not support VARCHAR SQL type");
            } else
            {
                PreparedStatement preparedstatement2 = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE VCHARCOL=?");
                String s1 = new String("cde");
                test(preparedstatement2, "setObject()");
                preparedstatement2.setObject(1, s1, 12);
                ResultSet resultset2 = preparedstatement2.executeQuery();
                if(next(resultset2))
                {
                    int k = resultset2.getInt(1);
                    result(k);
                    verify(k == 2, "The correct row for VARCHAR is found");
                }
                preparedstatement2.close();
            }
            if(getSupportedSQLType(-2) == null)
            {
                result("Does not support BINARY SQL type");
            } else
            {
                PreparedStatement preparedstatement3 = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE BINCOL=?");
                byte abyte0[] = {
                    99, 100, 101
                };
                test(preparedstatement3, "setObject()");
                preparedstatement3.setObject(1, abyte0);
                ResultSet resultset3 = preparedstatement3.executeQuery();
                if(next(resultset3))
                {
                    int l = resultset3.getInt(1);
                    result(l);
                    verify(l == 2, "The correct row for BINARY is found");
                }
                preparedstatement3.close();
            }
            if(getSupportedSQLType(-3) == null)
            {
                result("Does not support VARBINARY SQL type");
            } else
            {
                PreparedStatement preparedstatement4 = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE VARBINCOL=?");
                byte abyte1[] = {
                    99, 100, 101
                };
                test(preparedstatement4, "setObject()");
                preparedstatement4.setObject(1, abyte1, -3);
                ResultSet resultset4 = preparedstatement4.executeQuery();
                if(next(resultset4))
                {
                    int i1 = resultset4.getInt(1);
                    result(i1);
                    verify(i1 == 2, "The correct row for VARBINARY is found");
                }
                preparedstatement4.close();
            }
            if(getSupportedSQLType(2) == null && getSupportedSQLType(3) == null)
            {
                result("Does not support NUMERIC or DECIMAL SQL type");
            } else
            {
                PreparedStatement preparedstatement5 = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE NUMERICCOL=?");
                BigDecimal bigdecimal = new BigDecimal("-2345.78900");
                test(preparedstatement5, "setObject()");
                preparedstatement5.setObject(1, bigdecimal);
                ResultSet resultset5 = preparedstatement5.executeQuery();
                if(next(resultset5))
                {
                    int j1 = resultset5.getInt(1);
                    result(j1);
                    verify(j1 == 1, "The correct row for NUMERIC is found");
                }
                preparedstatement5.close();
            }
            if(getSupportedSQLType(7) == null)
            {
                result("Does not support REAL SQL type");
            } else
            {
                PreparedStatement preparedstatement6 = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE FLOATCOL <=?");
                Float float1 = new Float(4D);
                test(preparedstatement6, "setObject()");
                preparedstatement6.setObject(1, float1);
                ResultSet resultset6 = preparedstatement6.executeQuery();
                if(next(resultset6))
                {
                    int k1 = resultset6.getInt(1);
                    result(k1);
                    verify(k1 == 1, "The correct row for REAL is found");
                }
                preparedstatement6.close();
            }
            if(getSupportedSQLType(-7) == null)
            {
                result("Does not support BIT SQL type");
            } else
            {
                PreparedStatement preparedstatement7 = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE INTEGERCOL=1 AND BITCOL =?");
                Boolean boolean1 = new Boolean(true);
                test(preparedstatement7, "setObject()");
                preparedstatement7.setObject(1, boolean1);
                ResultSet resultset7 = preparedstatement7.executeQuery();
                if(next(resultset7))
                {
                    int l1 = resultset7.getInt(1);
                    result(l1);
                    verify(l1 == 1, "The correct row for BIT is found");
                }
                preparedstatement7.close();
            }
            if(getSupportedSQLType(5) == null)
            {
                result("Does not support SMALLINT SQL type");
            } else
            {
                PreparedStatement preparedstatement8 = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE SMALLCOL=?");
                Integer integer1 = new Integer(-2);
                test(preparedstatement8, "setObject()");
                preparedstatement8.setObject(1, integer1, 5);
                ResultSet resultset8 = preparedstatement8.executeQuery();
                if(next(resultset8))
                {
                    int i2 = resultset8.getInt(1);
                    result(i2);
                    verify(i2 == 1, "The correct row for SMALLINT is found");
                }
                preparedstatement8.close();
            }
            if(getSupportedSQLType(91) == null)
            {
                result("Does not support DATE SQL type");
            } else
            {
                PreparedStatement preparedstatement9 = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE DATECOL=?");
                Date date = Date.valueOf("1982-03-03");
                test(preparedstatement9, "setObject()");
                preparedstatement9.setObject(1, date);
                ResultSet resultset9 = preparedstatement9.executeQuery();
                if(next(resultset9))
                {
                    int j2 = resultset9.getInt(1);
                    result(j2);
                    verify(j2 == 2, "The correct row for DATE is found");
                }
                preparedstatement9.close();
            }
            if(getSupportedSQLType(92) == null)
            {
                result("Does not support TIME SQL type");
            } else
            {
                PreparedStatement preparedstatement10 = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE TIMECOL=?");
                Time time = Time.valueOf("02:02:02");
                test(preparedstatement10, "setObject()");
                preparedstatement10.setObject(1, time);
                ResultSet resultset10 = preparedstatement10.executeQuery();
                if(next(resultset10))
                {
                    int k2 = resultset10.getInt(1);
                    result(k2);
                    verify(k2 == 2, "The correct row for TIME is found");
                }
                preparedstatement10.close();
            }
            if(getSupportedSQLType(93) == null)
            {
                result("Does not support TIMESTAMP SQL type");
            } else
            {
                PreparedStatement preparedstatement11 = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE TSCOL=?");
                Timestamp timestamp = Timestamp.valueOf("1982-03-03 02:02:02");
                test(preparedstatement11, "setObject()");
                preparedstatement11.setObject(1, timestamp);
                ResultSet resultset11 = preparedstatement11.executeQuery();
                if(next(resultset11))
                {
                    int l2 = resultset11.getInt(1);
                    result(l2);
                    verify(l2 == 2, "The correct row for TIMESTAMP is found");
                }
                preparedstatement11.close();
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
