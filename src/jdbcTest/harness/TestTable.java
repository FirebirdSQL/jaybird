// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   TestTable.java

package jdbcTest.harness;

import java.sql.*;
import java.util.Enumeration;
import java.util.Vector;

// Referenced classes of package jdbcTest.harness:
//            Log, TType

public class TestTable
{

    public TestTable(String s, int i, Connection connection)
        throws SQLException
    {
        tableTypes = new Vector(10, 10);
        name = "";
        rowCount = 0;
        ttConn = null;
        ResultSet resultset = null;
        String s1 = "";
        ttConn = connection;
        trySQL("Drop table " + s);
        DatabaseMetaData databasemetadata = connection.getMetaData();
        resultset = databasemetadata.getTypeInfo();
        name = s;
        rowCount = i;
        int j = 1;
        s1 = "Create table " + s + " (";
        while(resultset.next())
        {
            String s2 = resultset.getString(1);
            int k = resultset.getInt(2);
            int l = resultset.getInt(3);
            String s3 = resultset.getString(4);
            String s4 = resultset.getString(5);
            String s5 = resultset.getString(6);
            short word0 = resultset.getShort(7);
            if(resultset.wasNull())
                word0 = 0;
            boolean flag = resultset.getBoolean(8);
            short word1 = resultset.getShort(9);
            if(resultset.wasNull())
                word1 = 0;
            boolean flag1 = resultset.getBoolean(10);
            if(resultset.wasNull())
                flag1 = true;
            boolean flag2 = resultset.getBoolean(11);
            boolean flag3 = resultset.getBoolean(12);
            String s6 = resultset.getString(13);
            short word2 = resultset.getShort(14);
            short word3 = resultset.getShort(15);
            TType ttype = new TType(s2, k, l, s3, s4, s5, word0, flag, word1, flag1, flag2, flag3, s6, word2, word3);
            String s7 = createTable(ttype, j);
            s1 = s1 + s7;
            if(s7 != "")
                j++;
        }
        s1 = s1 + ")";
        Log.println(s1);
        resultset.close();
        executeSQL(s1);
        loadTable();
    }

    public boolean assert(boolean flag, String s)
        throws SQLException
    {
        if(flag)
        {
            Log.println(": Assertion: PASSED  Text:" + s);
        } else
        {
            Log.printOnAll(": Assertion: FAILED  Text:" + s);
            throw new SQLException();
        }
        return flag;
    }

    private String createTable(TType ttype, int i)
        throws SQLException
    {
        String s = "";
        if(duplicateType(ttype))
            return "";
        String s1 = ttype.getName();
        int j = ttype.getPrecision();
        short word0 = ttype.getMaxScale();
        short word1 = ttype.getMinScale();
        switch(ttype.getJavaType())
        {
        default:
            break;

        case -7:
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case -6:
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case 5: // '\005'
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case 4: // '\004'
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case -5:
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case 6: // '\006'
            assert(j > 0, "The FLOAT precision cannot be zero");
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case 7: // '\007'
            assert(j > 0, "The REAL precision cannot be zero");
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case 8: // '\b'
            assert(j > 0, "The DOUBLE precision cannot be zero");
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            s = s + "(" + ttype.getPrecision();
            s = s + ")";
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case 2: // '\002'
            assert(word0 >= 16, "The NUMERIC maximum scale should be atleast 16");
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            s = s + "(" + j;
            s = s + ",16";
            s = s + ")";
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case 3: // '\003'
            assert(word0 >= 16, "The DECIMAL maximum scale should be atleast 16");
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            s = s + "(" + j;
            s = s + ",16";
            s = s + ")";
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case 1: // '\001'
            assert(j > 0, "The CHAR precision cannot be zero");
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            int k = 256;
            if(ttype.getPrecision() < 256)
                k = ttype.getPrecision();
            s = s + "(" + k;
            s = s + ")";
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case 12: // '\f'
            assert(j > 0, "The VARCHAR precision cannot be zero");
            if(s1.compareTo("sysname") == 0)
                break;
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            int l = 256;
            if(ttype.getPrecision() < 256)
                l = ttype.getPrecision();
            s = s + "(" + l;
            s = s + ")";
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case -1:
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case 91: // '['
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case 92: // '\\'
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case 93: // ']'
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case -2:
            assert(j > 0, "The BINARY precision cannot be zero");
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            int i1 = 256;
            if(ttype.getPrecision() < 256)
                i1 = ttype.getPrecision();
            s = s + "(" + i1;
            s = s + ")";
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case -3:
            assert(j > 0, "The VARBINARY precision cannot be zero");
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + s1;
            int j1 = 256;
            if(ttype.getPrecision() < 256)
                j1 = ttype.getPrecision();
            s = s + "(" + j1;
            s = s + ")";
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;

        case -4:
            tableTypes.addElement(ttype);
            if(i++ > 1)
                s = s + ",";
            ttype.setColName("VAR" + i);
            s = s + "VAR" + i + " " + ttype.getName();
            if(ttype.isNullable() == 0)
                s = s + " not null";
            break;
        }
        return s;
    }

    private boolean duplicateType(TType ttype)
    {
        boolean flag = false;
        int i;
        switch(ttype.getJavaType())
        {
        case -1:
            i = -4;
            break;

        case -4:
            i = -1;
            break;

        case -3:
        case -2:
        default:
            i = ttype.getJavaType();
            break;
        }
        for(Enumeration enumeration = tableTypes.elements(); enumeration.hasMoreElements();)
        {
            TType ttype1 = (TType)enumeration.nextElement();
            if(ttype.getName().compareTo(ttype1.getName()) == 0 || ttype.getJavaType() == ttype1.getJavaType() || i == ttype1.getJavaType())
                flag = true;
        }

        return flag;
    }

    public void executeSQL(String s)
        throws SQLException
    {
        Log.println("jdbcTest.harness.TestTable: Executing Sql:" + s);
        Statement statement = ttConn.createStatement();
        int i = statement.executeUpdate(s);
        statement.close();
    }

    public String getColName(int i)
    {
        if(i == 0 || i > tableTypes.size())
            throw new IllegalArgumentException("invalid column number");
        TType ttype = (TType)tableTypes.elementAt(i - 1);
        if(ttype == null)
            throw new IllegalArgumentException("invalid column number");
        else
            return ttype.getColName();
    }

    public int getColumnCount()
    {
        return tableTypes.size();
    }

    public int getJavaType(int i)
    {
        if(i == 0 || i > tableTypes.size())
            throw new IllegalArgumentException("invalid column number");
        TType ttype = (TType)tableTypes.elementAt(i - 1);
        if(ttype == null)
            throw new IllegalArgumentException("invalid column number");
        else
            return ttype.getJavaType();
    }

    public int getMaxPrecision(int i)
    {
        if(i == 0 || i > tableTypes.size())
            throw new IllegalArgumentException("invalid column number");
        TType ttype = (TType)tableTypes.elementAt(i - 1);
        if(ttype == null)
            throw new IllegalArgumentException("invalid column number");
        else
            return ttype.getPrecision();
    }

    public int getMaxScale(int i)
    {
        if(i == 0 || i > tableTypes.size())
            throw new IllegalArgumentException("invalid column number");
        TType ttype = (TType)tableTypes.elementAt(i - 1);
        if(ttype == null)
            throw new IllegalArgumentException("invalid column number");
        else
            return ttype.getMaxScale();
    }

    public int getMinScale(int i)
    {
        if(i == 0 || i > tableTypes.size())
            throw new IllegalArgumentException("invalid column number");
        TType ttype = (TType)tableTypes.elementAt(i - 1);
        if(ttype == null)
            throw new IllegalArgumentException("invalid column number");
        else
            return ttype.getMinScale();
    }

    public String getTypeName(int i)
    {
        if(i == 0 || i > tableTypes.size())
            throw new IllegalArgumentException("invalid column number");
        TType ttype = (TType)tableTypes.elementAt(i - 1);
        if(ttype == null)
            throw new IllegalArgumentException("invalid column number");
        else
            return ttype.getTypeName();
    }

    public boolean isAutoincrement(int i)
    {
        if(i == 0 || i > tableTypes.size())
            throw new IllegalArgumentException("invalid column number");
        TType ttype = (TType)tableTypes.elementAt(i - 1);
        if(ttype == null)
            throw new IllegalArgumentException("invalid column number");
        else
            return ttype.isAutoincrement();
    }

    public boolean isCasesensitive(int i)
    {
        if(i == 0 || i > tableTypes.size())
            throw new IllegalArgumentException("invalid column number");
        TType ttype = (TType)tableTypes.elementAt(i - 1);
        if(ttype == null)
            throw new IllegalArgumentException("invalid column number");
        else
            return ttype.isCaseSensitive();
    }

    public boolean isCurrency(int i)
    {
        if(i == 0 || i > tableTypes.size())
            throw new IllegalArgumentException("invalid column number");
        TType ttype = (TType)tableTypes.elementAt(i - 1);
        if(ttype == null)
            throw new IllegalArgumentException("invalid column number");
        else
            return ttype.isCurrency();
    }

    public short isNullable(int i)
    {
        if(i == 0 || i > tableTypes.size())
            throw new IllegalArgumentException("invalid column number");
        TType ttype = (TType)tableTypes.elementAt(i - 1);
        if(ttype == null)
            throw new IllegalArgumentException("invalid column number");
        else
            return ttype.isNullable();
    }

    public short isSearchable(int i)
    {
        if(i == 0 || i > tableTypes.size())
            throw new IllegalArgumentException("invalid column number");
        TType ttype = (TType)tableTypes.elementAt(i - 1);
        if(ttype == null)
            throw new IllegalArgumentException("invalid column number");
        else
            return ttype.isSearchable();
    }

    public boolean isSigned(int i)
    {
        if(i == 0 || i > tableTypes.size())
            throw new IllegalArgumentException("invalid column number");
        TType ttype = (TType)tableTypes.elementAt(i - 1);
        if(ttype == null)
            throw new IllegalArgumentException("invalid column number");
        else
            return ttype.isSigned();
    }

    private void loadTable()
        throws SQLException
    {
        String s = "";
        String s2 = "";
        String s4 = "";
        for(int j = 0; j < rowCount; j++)
        {
            String s1 = "insert into " + name;
            String s3 = " (";
            String s5 = " values(";
            int i = 1;
            for(Enumeration enumeration = tableTypes.elements(); enumeration.hasMoreElements();)
            {
                TType ttype = (TType)enumeration.nextElement();
                if(!ttype.isAutoincrement())
                    switch(ttype.getJavaType())
                    {
                    default:
                        break;

                    case -7:
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "1" + ttype.getSuffix();
                        break;

                    case -6:
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "127" + ttype.getSuffix();
                        break;

                    case 5: // '\005'
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "-9999" + ttype.getSuffix();
                        break;

                    case 4: // '\004'
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "-99999999" + ttype.getSuffix();
                        break;

                    case -5:
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "-9999999999999999" + ttype.getSuffix();
                        break;

                    case 6: // '\006'
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "1.2345678912345" + ttype.getSuffix();
                        break;

                    case 7: // '\007'
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "1.234567" + ttype.getSuffix();
                        break;

                    case 8: // '\b'
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "1.2345678912345" + ttype.getSuffix();
                        break;

                    case 2: // '\002'
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "12.1234561234567890" + ttype.getSuffix();
                        break;

                    case 3: // '\003'
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "12.1234561234567890" + ttype.getSuffix();
                        break;

                    case 1: // '\001'
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" + ttype.getSuffix();
                        break;

                    case 12: // '\f'
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" + ttype.getSuffix();
                        break;

                    case -1:
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" + ttype.getSuffix();
                        break;

                    case 91: // '['
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + "{d '1996-01-01'}";
                        break;

                    case 92: // '\\'
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + "{t '04:59:59'}";
                        break;

                    case 93: // ']'
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + "{ts '1996-02-28 04:59:59.000000'}";
                        break;

                    case -2:
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F" + ttype.getSuffix();
                        break;

                    case -3:
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F" + ttype.getSuffix();
                        break;

                    case -4:
                        if(i++ > 1)
                        {
                            s3 = s3 + ", ";
                            s5 = s5 + ", ";
                        }
                        s3 = s3 + ttype.getColName();
                        s5 = s5 + ttype.getPrefix() + "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F" + ttype.getSuffix();
                        break;
                    }
            }

            s1 = s1 + s3 + ") " + s5 + ")";
            executeSQL(s1);
        }

    }

    public void trySQL(String s)
    {
        try
        {
            executeSQL(s);
        }
        catch(SQLException _ex) { }
        catch(Exception _ex) { }
    }

    public static final String aBIT = "1";
    public static final String aTINYINT = "127";
    public static final String aSMALLINT = "-9999";
    public static final String aINTEGER = "-99999999";
    public static final String aBIGINT = "-9999999999999999";
    public static final String aFLOAT = "1.2345678912345";
    public static final String aFLOATMin = "1.234567891234";
    public static final String aFLOATMax = "1.234567891235";
    public static final String aREAL = "1.234567";
    public static final String aREALMin = "1.23456";
    public static final String aREALMax = "1.23457";
    public static final String aDOUBLE = "1.2345678912345";
    public static final String aNUMERIC = "12.1234561234567890";
    public static final String aDECIMAL = "12.1234561234567890";
    public static final String aCHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String aVARCHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String aLONGVARCHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String aBINARY = "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F";
    public static final String aVARBINARY = "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F";
    public static final String aLONGVARBINARY = "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F";
    public static final String aDATE = "1996-01-01";
    public static final String aTIME = "04:59:59";
    public static final String aTIMESTAMP = "1996-02-28 04:59:59.000000";
    public static final int numericScale = 16;
    private Vector tableTypes;
    private String name;
    private int rowCount;
    private Connection ttConn;
}
