// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   JDBCTestWin.java
package jdbcTest;


import java.io.*;
import java.util.Date;
import jdbcTest.harness.*;

public class JDBCTest
    implements TestDriverSubscriber
{

    public PrintStream Console = System.out;
    private String user = "sysdba";
    private String password = "masterkey";
    private String url = "jdbc:firebirdsql:localhost:/usr/local/firebird/dev/client-java/db/fbmctest.gdb";

    public JDBCTest()
    {
        Console.println("JDBCTest Harness v .04");
    }


    public void loadINIFile()
    {
        try
        {
            ini = new INIFile("JDBCTestWin.ini");
            java.util.Properties properties = ini.getSection("TestModules");
        }
        catch(FileNotFoundException _ex)
        {
            Console.println("JDBCTestWin.ini file not found in " + System.getProperty("user.dir"));
            ini = null;
        }
    }

    public static void main(String args[])
    {
        new JDBCTest().selectedStartTest();
    }

    public void receiveNotification(Object obj, String s)
    {
        //start.enable();
        //Stop.disable();
        Log.printOnAll(s);
    }



    public void selectedStartTest()
    {
        loadINIFile();
        setLogFiles();
        if(ini == null)
        {
            Console.println("JDBCTestWin.ini file not found in " + System.getProperty("user.dir"));
            return;
        }
        Log.printOnAll("**** Test Run Started " + new Date() + " ****");
        java.util.Properties properties = ini.getSection("TestModules");
        if(properties == null)
        {
            Console.println("TestModules section not found in ini file");
            return;
        } else
        {
            TestDriver testdriver = new TestDriver(url, user, password, properties, ini);
            testDriver = testdriver;
            testDriver.subscribe(this);
            testdriver.start();
            //start.disable();
            //Stop.enable();
            return;
        }
    }



    public void setLogFiles()
    {
        String s = new String("");
        if(ini != null)
            s = ini.getString("Log Files", "passfail", "");
        else
            s = "";
        if(s == "")
            s = new String("System.out");
        else
            try
            {
                Log.setPassFailFile(s);
            }
            catch(Exception _ex)
            {
                Console.println("Unable to open pass fail log file " + s);
                s = new String("System.out");
            }
        Console.println("Logging pass fail to " + s);
        if(ini != null)
            s = ini.getString("Log Files", "output", "");
        else
            s = "";
        if(s == "")
            s = new String("System.out");
        else
            try
            {
                Log.setOutputFile(s);
            }
            catch(Exception _ex)
            {
                Console.println("Unable to open outpout log file " + s);
                s = new String("System.out");
            }
        Console.println("Logging output to " + s);
    }


    TestDriver testDriver;
    INIFile ini;
}
