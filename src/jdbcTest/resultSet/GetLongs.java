// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   GetLongs.java

package jdbcTest.resultSet;

import java.io.InputStream;
import java.sql.*;
import jdbcTest.harness.*;

// Referenced classes of package jdbcTest.resultSet:
//            BinaryInputStream, CharInputStream

public class GetLongs extends TestModule
{

    public GetLongs()
    {
    }

    public void run()
    {
        Connection connection = null;
        String s = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        byte abyte0[] = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            10, 11, 12, 13, 14, 15
        };
        int i = 50000;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test ResultSet.getLongs");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            SupportedSQLType supportedsqltype = getSupportedSQLType(-1);
            SupportedSQLType supportedsqltype1 = getSupportedSQLType(-4);
            if(supportedsqltype == null)
            {
                result("Does not support LONGVARCHAR SQL type");
            } else
            {
                trySQL(connection, "drop table getlvchar");
                executeSQL(connection, "create table getlvchar(achar " + supportedsqltype.getTypeName() + ")");
                int j;
                if(i > supportedsqltype.getPrecision())
                    j = supportedsqltype.getPrecision();
                else
                    j = i;
                PreparedStatement preparedstatement = connection.prepareStatement("insert into getlvchar values(?)");
                preparedstatement.setAsciiStream(1, new CharInputStream(s, j), j);
                preparedstatement.executeUpdate();
                Statement statement = connection.createStatement();
                ResultSet resultset = statement.executeQuery("select * from getlvchar");
                if(next(resultset))
                {
                    String s1 = resultset.getString(1);
                    assert(s1.length() == j, "The full length inserted:" + j + " must match the length of the String received:" + s1.length());
                    CharInputStream charinputstream = new CharInputStream(s, j);
                    for(int j2 = 0; j2 < s1.length(); j2++)
                        if(s1.charAt(j2) != charinputstream.read())
                            assert(false, "The LONGVARCHAR value retrieved via getString must match the inserted value");

                    assert(true, "The LONGVARCHAR value retrieved via getString must match the inserted value");
                }
                resultset = statement.executeQuery("select * from getlvchar");
                if(next(resultset))
                {
                    InputStream inputstream = resultset.getAsciiStream(1);
                    CharInputStream charinputstream1 = new CharInputStream(s, j);
                    int l;
                    do
                    {
                        l = charinputstream1.read();
                        int k1 = inputstream.read();
                        if(l != k1)
                            assert(false, "The LONGVARCHAR value retrieved via getAsciiStream must match the inserted value");
                    } while(l != -1);
                    assert(true, "The LONGVARCHAR value retrieved via getAsciiStream must match the inserted value");
                }
                resultset = statement.executeQuery("select * from getlvchar");
                if(next(resultset))
                {
                    InputStream inputstream1 = resultset.getUnicodeStream(1);
                    CharInputStream charinputstream2 = new CharInputStream(s, j);
                    do
                    {
                        int l1 = inputstream1.read();
                        if(l1 == -1 && inputstream1.read() == -1)
                            break;
                        if(l1 != 0)
                            assert(false, "The LONGVARCHAR value retrieved via getUnicodeStream must have the high order byte set to zero");
                        int i1 = charinputstream2.read();
                        l1 = inputstream1.read();
                        if(i1 != l1)
                            assert(false, "The LONGVARCHAR value retrieved via getUnicodeStream must match the inserted value");
                    } while(true);
                    assert(true, "The LONGVARCHAR value retrieved via getUnicodeStream must match the inserted value");
                }
            }
            if(supportedsqltype == null)
            {
                result("Does not support LONGVARBINARY SQL type");
            } else
            {
                trySQL(connection, "drop table getlvbin");
                executeSQL(connection, "create table getlvbin(achar " + supportedsqltype1.getTypeName() + ")");
                int k;
                if(i > supportedsqltype1.getPrecision())
                    k = supportedsqltype1.getPrecision();
                else
                    k = i;
                PreparedStatement preparedstatement1 = connection.prepareStatement("insert into getlvbin values(?)");
                preparedstatement1.setBinaryStream(1, new BinaryInputStream(abyte0, k), k);
                preparedstatement1.executeUpdate();
                Statement statement1 = connection.createStatement();
                ResultSet resultset1 = statement1.executeQuery("select * from getlvbin");
                if(next(resultset1))
                {
                    byte abyte1[] = resultset1.getBytes(1);
                    assert(abyte1.length == k, "The full length of a LONGVARBINARY should be returned by getBytes");
                    BinaryInputStream binaryinputstream = new BinaryInputStream(abyte0, k);
                    for(int k2 = 0; k2 < abyte1.length; k2++)
                        if(abyte1[k2] != binaryinputstream.read())
                            assert(false, "The LONGVARBINARY value retrieved by getBytes must match the inserted value");

                }
                assert(true, "The LONGVARBINARY value retrieved via getBytes must match the inserted value");
                resultset1 = statement1.executeQuery("select * from getlvbin");
                if(next(resultset1))
                {
                    InputStream inputstream2 = resultset1.getBinaryStream(1);
                    BinaryInputStream binaryinputstream1 = new BinaryInputStream(abyte0, k);
                    int j1;
                    do
                    {
                        j1 = binaryinputstream1.read();
                        int i2 = inputstream2.read();
                        if(j1 != i2)
                            assert(false, "The LONGVARBINARY value retrieved via getBinaryStream must match the inserted value");
                    } while(j1 != -1);
                }
                assert(true, "The LONGVARBINARY value retrieved via getBinaryStream must match the inserted value");
            }
            passed();
        }
        catch(SQLException sqlexception)
        {
            exception(sqlexception);
        }
        catch(Exception exception1)
        {
            exception1.printStackTrace(Log.out);
            failed();
        }
        finally
        {
            if(connection != null)
                trySQL(connection, "drop table getlvchar");
            if(connection != null)
                trySQL(connection, "drop table getlvbin");
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
