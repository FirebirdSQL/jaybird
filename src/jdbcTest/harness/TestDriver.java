// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   TestDriver.java

package jdbcTest.harness;

import java.sql.DriverManager;
import java.util.*;

// Referenced classes of package jdbcTest.harness:
//            Console, INIFile, Log, TestDriverInterface,
//            TestDriverSubscriber, TestModule

public class TestDriver
    implements Runnable, TestDriverInterface
{

    public TestDriver(String s, String s1, String s2, Properties properties, INIFile inifile)
    {
        tdThread = null;
        tmModules = null;
        tmSubscribers = null;
        tmURL = s;
        tmSignonID = s1;
        tmPassWord = s2;
        tmModules = properties;
        tdINI = inifile;
    }

    public String getString(String s, String s1, String s2)
    {
        return tdINI.getString(s, s1, s2);
    }

    public ThreadGroup getThreadGroup()
    {
        return tdThreadGroup;
    }

    public synchronized void run()
    {
        Object obj = null;
        try
        {
            Properties properties = System.getProperties();
            Console.println("initial drivers list:" + System.getProperty("jdbc.drivers"));
            String s1 = tdINI.getString("Driver Options", "Drivers", "");
            properties.put("jdbc.drivers", s1.trim());
            Console.println("Driver = " + s1);
            s1 = System.getProperty("jdbc.drivers");
            s1 = tdINI.getString("Driver Options", "RegisterDriver", "");
            Console.println("RegisterDriver = " + s1);
            if(s1 != "")
                Class.forName(s1.trim());
            Console.println("Driver Registered " + s1);
            String s2 = tdINI.getString("Driver Options", "DriverManagerLog", "off");
            if(s2.trim().toUpperCase().startsWith("ON"))
            {
                DriverManager.setLogStream(Log.out);
                Console.println("Driver Manager logging is on");
            } else
            {
                Console.println("Driver Manager logging is off (enable by adding Driver Option 'DriverManagerLog = ON')");
            }
            Vector vector = tdINI.getSectionKeys("TestModules");
            for(Enumeration enumeration = vector.elements(); enumeration.hasMoreElements();)
            {
                String s = ((String)enumeration.nextElement()).trim();
                Log.println("\n=======================================\nLoading test module " + s);
                try
                {
                    TestModule testmodule = (TestModule)Class.forName("jdbcTest." + s).newInstance();
                    try
                    {
                        testmodule.setDriver(this, tdThreadGroup, tmURL, tmSignonID, tmPassWord);
                        testmodule.setDescription(s + " - " + tmModules.getProperty(s));
                        testmodule.start();
                        wait();
                        testmodule = null;
                        System.gc();
                    }
                    catch(Exception exception2)
                    {
                        exception2.printStackTrace();
                    }
                }
                catch(Exception _ex)
                {
                    Log.printOnAll("**** Unable to load module jdbcTest. ****");
                }
                catch(NoClassDefFoundError _ex)
                {
                    Log.printOnAll("**** Unable to load module jdbcTest." + s + " ****");
                }
            }

        }
        catch(Exception exception1)
        {
            exception1.printStackTrace();
        }
        finally
        {
            tellSubscribers();
        }
    }

    public void start()
    {
        if(tdThread == null)
        {
            tdThread = new Thread(this);
            tdThreadGroup = new ThreadGroup("JDBCTest");
            tdThread.start();
        }
    }

    public void stop()
    {
        if(tdThread != null)
        {
            tellSubscribers();
            tdThreadGroup.stop();
            tdThreadGroup = null;
            tdThread.stop();
            tdThread = null;
            tdINI = null;
        }
        tellSubscribers();
    }

    public void subscribe(TestDriverSubscriber testdriversubscriber)
    {
        if(tmSubscribers == null)
            tmSubscribers = new Vector(5);
        tmSubscribers.addElement(testdriversubscriber);
    }

    private void tellSubscribers()
    {
        TestDriverSubscriber testdriversubscriber;
        for(Enumeration enumeration = tmSubscribers.elements(); enumeration.hasMoreElements(); testdriversubscriber.receiveNotification(this, "**** Test Run Completed " + new Date() + " ****"))
            testdriversubscriber = (TestDriverSubscriber)enumeration.nextElement();

    }

    private Thread tdThread;
    private String tmURL;
    private String tmSignonID;
    private String tmPassWord;
    private Properties tmModules;
    private ThreadGroup tdThreadGroup;
    private INIFile tdINI;
    private Vector tmSubscribers;
}
