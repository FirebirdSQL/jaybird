// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setNull.java

package jdbcTest.preparedStatement;

import java.sql.*;
import jdbcTest.harness.SupportedSQLType;
import jdbcTest.harness.TestModule;

public class setNull extends TestModule
{

    public setNull()
    {
    }

    public void run()
    {
        Connection connection = null;
        PreparedStatement preparedstatement = null;
        ResultSet resultset = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test PreparedStatement.setNull");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            preparedstatement = connection.prepareStatement("INSERT INTO JDBC_NULL_TEST (ID, CHARCOL, VCHARCOL, DECIMALCOL, NUMERICCOL, SMALLCOL, INTEGERCOL, REALCOL, FLOATCOL, DOUBLECOL, BITCOL, TINYINTCOL, BIGINTCOL, BINCOL, VARBINCOL, DATECOL, TIMECOL, TSCOL) VALUES('PreparedStatement',?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            test(preparedstatement, "setNull()");
            preparedstatement.setNull(1, 1);
            SupportedSQLType supportedsqltype;
            if((supportedsqltype = getSupportedSQLType(12)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(2, 1);
            else
                preparedstatement.setNull(2, 12);
            if((supportedsqltype = getSupportedSQLType(3)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(3, 1);
            else
                preparedstatement.setNull(3, 3);
            if((supportedsqltype = getSupportedSQLType(2)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(4, 1);
            else
                preparedstatement.setNull(4, 2);
            if((supportedsqltype = getSupportedSQLType(5)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(5, 1);
            else
                preparedstatement.setNull(5, 5);
            if((supportedsqltype = getSupportedSQLType(4)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(6, 1);
            else
                preparedstatement.setNull(6, 4);
            if((supportedsqltype = getSupportedSQLType(7)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(7, 1);
            else
                preparedstatement.setNull(7, 7);
            if((supportedsqltype = getSupportedSQLType(6)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(8, 1);
            else
                preparedstatement.setNull(8, 6);
            if((supportedsqltype = getSupportedSQLType(8)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(9, 1);
            else
                preparedstatement.setNull(9, 8);
            if((supportedsqltype = getSupportedSQLType(-7)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(10, 1);
            else
                preparedstatement.setNull(10, -7);
            if((supportedsqltype = getSupportedSQLType(-6)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(11, 1);
            else
                preparedstatement.setNull(11, -6);
            if((supportedsqltype = getSupportedSQLType(-5)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(12, 1);
            else
                preparedstatement.setNull(12, -5);
            if((supportedsqltype = getSupportedSQLType(-2)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(13, 1);
            else
                preparedstatement.setNull(13, -2);
            if((supportedsqltype = getSupportedSQLType(-3)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(14, 1);
            else
                preparedstatement.setNull(14, -3);
            if((supportedsqltype = getSupportedSQLType(91)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(15, 1);
            else
                preparedstatement.setNull(15, 91);
            if((supportedsqltype = getSupportedSQLType(92)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(16, 1);
            else
                preparedstatement.setNull(16, 92);
            if((supportedsqltype = getSupportedSQLType(93)) == null || supportedsqltype.NULLABLE() == 0)
                preparedstatement.setNull(17, 1);
            else
                preparedstatement.setNull(17, 93);
            preparedstatement.executeUpdate();
            preparedstatement.close();
            Statement statement = connection.createStatement();
            resultset = statement.executeQuery("SELECT CHARCOL, VCHARCOL, DECIMALCOL, NUMERICCOL, SMALLCOL, INTEGERCOL, REALCOL, FLOATCOL, DOUBLECOL, BITCOL, TINYINTCOL, BIGINTCOL, BINCOL, VARBINCOL, DATECOL, TIMECOL, TSCOL FROM JDBC_NULL_TEST WHERE ID = 'PreparedStatement'");
            if(next(resultset))
            {
                String s = resultset.getString(1);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(2);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(3);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(4);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(5);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(6);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(7);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(8);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(9);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(10);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(11);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(12);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(13);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(14);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(15);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(16);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
                s = resultset.getString(17);
                result(s);
                verify(resultset.wasNull(), "A NULL value must be found");
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
                if(preparedstatement != null)
                    preparedstatement.close();
                if(connection != null)
                    connection.close();
                if(resultset != null)
                    resultset.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}
