// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   SupportedSQLType.java

package jdbcTest.harness;


public class SupportedSQLType
{

    public SupportedSQLType(String s, short word0, int i, String s1, String s2, String s3, short word1,
            boolean flag, short word2, boolean flag1, boolean flag2, boolean flag3, String s4, short word3,
            short word4, int j)
    {
        name = s != null ? s : "unknown";
        javaType = word0;
        precision = i;
        prefix = s1 != null ? s1 : "";
        suffix = s2 != null ? s2 : "";
        parms = s3 != null ? s3 : "";
        nullable = word1;
        casesensitive = flag;
        searchable = word2;
        signed = flag1;
        currency = flag2;
        autoincrement = flag3;
        typeName = s4 != null ? s4 : "";
        minScale = word3;
        maxScale = word4;
        radix = j;
    }

    public short NULLABLE()
    {
        return nullable;
    }

    public Object clone()
    {
        return new SupportedSQLType(new String(name), javaType, precision, new String(prefix), new String(suffix), new String(parms), nullable, casesensitive, searchable, signed, currency, autoincrement, typeName, minScale, maxScale, radix);
    }

    public boolean getAutoIncrement()
    {
        return autoincrement;
    }

    public boolean getCaseSensitive()
    {
        return casesensitive;
    }

    public String getCreateParams()
    {
        return parms;
    }

    public int getDataType()
    {
        return javaType;
    }

    public boolean getFixedPrecScale()
    {
        return currency;
    }

    public String getLiteralPrefix()
    {
        if(prefix == null)
            return "";
        else
            return prefix;
    }

    public String getLiteralSuffix()
    {
        if(suffix == null)
            return "";
        else
            return suffix;
    }

    public String getLocalTypeName()
    {
        return typeName;
    }

    public short getMaximumScale()
    {
        return maxScale;
    }

    public short getMinimumScale()
    {
        return minScale;
    }

    public int getNumPrecRadix()
    {
        return radix;
    }

    public int getPrecision()
    {
        return precision;
    }

    public short getSearchable()
    {
        return searchable;
    }

    public String getTypeName()
    {
        return name;
    }

    public boolean getUnsignedAttribute()
    {
        return signed;
    }

    String name;
    short javaType;
    int precision;
    String prefix;
    String suffix;
    String parms;
    short nullable;
    boolean casesensitive;
    short searchable;
    boolean signed;
    boolean currency;
    boolean autoincrement;
    String typeName;
    short minScale;
    short maxScale;
    int radix;
}
