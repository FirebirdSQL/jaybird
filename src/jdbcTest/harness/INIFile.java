// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   INIFile.java

package jdbcTest.harness;

import java.io.*;
import java.util.*;

public class INIFile
{

    public INIFile(String s)
        throws FileNotFoundException
    {
        this(s, System.getProperty("user.dir"));
    }

    public INIFile(String s, String s1)
        throws FileNotFoundException
    {
        INIProperties = new Hashtable();
        DataInputStream datainputstream = null;
        String s2 = null;
        String s3 = null;
        String s4 = "@#default#@";
        Object obj = null;
        Object obj1 = null;
        int i = 0;
        Vector vector = null;
        Vector vector1 = null;
        boolean flag = false;
        boolean flag1 = false;
        Properties properties = new Properties();
        INIProperties.put(s4, properties);
        if(s.length() == 0)
        {
            System.out.println("INIFile requires filename argument");
            System.exit(0);
        }
        if(s1.length() == 0)
            s1 = new String(".");
        if(s.indexOf(".") < 0)
            s = new String(s + ".ini");
        s2 = new String(s1 + System.getProperty("file.separator") + s);
        datainputstream = new DataInputStream(new FileInputStream(s2));
        try
        {
            s3 = datainputstream.readLine();
        }
        catch(IOException _ex)
        {
            flag = true;
        }
        while(!flag && s3 != null)
        {
            s3 = s3.trim();
            if(s3.startsWith("["))
            {
                String s5 = s3.substring(1, s3.lastIndexOf("]"));
                String s6 = s5.trim();
                properties = new Properties();
                vector = new Vector(1);
                vector1 = new Vector(1);
                INIProperties.put(s6, properties);
                INIProperties.put(new String(s6 + ":KEYS"), vector);
                INIProperties.put(new String(s6 + ":VALS"), vector1);
                i = 0;
            } else
            if(!s3.startsWith(";") && !s3.startsWith("//"))
                if(s3.startsWith("@@"))
                {
                    String s7 = new String("@@" + ++i);
                    String s10 = new String(s3.substring(2).trim());
                    if(s10.endsWith("\\"))
                        s10 = doLineContinuation(datainputstream, s10);
                    properties.put(s7, s10);
                    vector.addElement(s7);
                    vector1.addElement(s10);
                } else
                {
                    int j = s3.indexOf("=");
                    if(j > 0)
                    {
                        String s8 = new String(s3.substring(0, j));
                        s8 = s8.trim();
                        String s11 = new String(s3.substring(j + 1).trim());
                        properties.put(s8, s11);
                        vector.addElement(s8);
                        vector1.addElement(s11);
                    } else
                    if(s3.length() > 0)
                    {
                        String s9 = new String(s3);
                        String s12 = "";
                        properties.put(s9, s12);
                        vector.addElement(s9);
                        vector1.addElement(s12);
                    }
                }
            try
            {
                s3 = datainputstream.readLine();
            }
            catch(IOException _ex)
            {
                flag = true;
            }
        }
    }

    private String doLineContinuation(DataInputStream datainputstream, String s)
    {
        if(s.endsWith("\\\\"))
            return s;
        String s1 = new String();
        for(boolean flag = false; !flag;)
            try
            {
                String s2 = datainputstream.readLine();
                s = s.substring(0, s.length() - 1) + " " + s2.trim();
                if(s.endsWith("\\"))
                {
                    if(s.endsWith("\\\\"))
                        flag = true;
                    else
                        flag = false;
                } else
                {
                    flag = true;
                }
            }
            catch(IOException _ex)
            {
                flag = true;
            }

        return s;
    }

    public Properties getSection(String s)
    {
        try
        {
            return (Properties)INIProperties.get(s);
        }
        catch(Exception _ex)
        {
            return new Properties();
        }
    }

    public Vector getSectionKeys(String s)
    {
        String s1 = s + ":KEYS";
        try
        {
            return (Vector)INIProperties.get(s1);
        }
        catch(Exception _ex)
        {
            return new Vector(1);
        }
    }

    public Vector getSectionValues(String s)
    {
        String s1 = s + ":VALS";
        try
        {
            return (Vector)INIProperties.get(s1);
        }
        catch(Exception _ex)
        {
            return new Vector(1);
        }
    }

    public String getString(String s, String s1)
    {
        try
        {
            Properties properties = (Properties)INIProperties.get(s);
            return properties.getProperty(s1);
        }
        catch(Exception _ex)
        {
            return "";
        }
    }

    public String getString(String s, String s1, String s2)
    {
        String s3 = s2;
        if(s3 == null)
            s3 = "";
        if(s == null || s1 == null)
            return s3;
        try
        {
            Properties properties = (Properties)INIProperties.get(s);
            return properties.getProperty(s1, s2);
        }
        catch(Exception _ex)
        {
            return s3;
        }
    }

    Hashtable INIProperties;
}
