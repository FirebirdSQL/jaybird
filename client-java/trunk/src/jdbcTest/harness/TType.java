// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   TestTable.java

package jdbcTest.harness;


// Referenced classes of package jdbcTest.harness:
//            TestTable

class TType
{

    public TType(String s, int i, int j, String s1, String s2, String s3, short word0,
            boolean flag, short word1, boolean flag1, boolean flag2, boolean flag3, String s4, short word2,
            short word3)
    {
        name = s;
        javaType = i;
        precision = j;
        prefix = s1;
        suffix = s2;
        parms = s3;
        nullable = word0;
        casesensitive = flag;
        searchable = word1;
        unsigned = flag1;
        currency = flag2;
        autoincrement = flag3;
        typeName = s4;
        minScale = word2;
        maxScale = word3;
        colName = "";
    }

    public String getColName()
    {
        return colName;
    }

    public int getJavaType()
    {
        return javaType;
    }

    public short getMaxScale()
    {
        return maxScale;
    }

    public short getMinScale()
    {
        return minScale;
    }

    public String getName()
    {
        return name;
    }

    public String getParms()
    {
        return parms;
    }

    public int getPrecision()
    {
        return precision;
    }

    public String getPrefix()
    {
        if(prefix == null)
            return "";
        else
            return prefix;
    }

    public String getSuffix()
    {
        if(suffix == null)
            return "";
        else
            return suffix;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public boolean isAutoincrement()
    {
        return autoincrement;
    }

    public boolean isCaseSensitive()
    {
        return casesensitive;
    }

    public boolean isCurrency()
    {
        return currency;
    }

    public short isNullable()
    {
        return nullable;
    }

    public short isSearchable()
    {
        return searchable;
    }

    public boolean isSigned()
    {
        return !unsigned;
    }

    public void setColName(String s)
    {
        colName = s;
    }

    String name;
    int javaType;
    int precision;
    String prefix;
    String suffix;
    String parms;
    short nullable;
    boolean casesensitive;
    short searchable;
    boolean unsigned;
    boolean currency;
    boolean autoincrement;
    String typeName;
    short minScale;
    short maxScale;
    String colName;
}
