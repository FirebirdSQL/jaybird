// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   JDBCTestWin.java
package jdbcTest;


import java.awt.*;
import java.io.*;
import java.util.Date;
import jdbcTest.harness.*;

public class JDBCTestWin extends Frame
    implements TestDriverSubscriber
{

    public JDBCTestWin()
    {
        super("JDBCTest Harness v .04");
        conDialog = null;
        MenuBar menubar = new MenuBar();
        fileMenu = new Menu("File");
        fileMenu.add(new MenuItem("Open..."));
        fileMenu.addSeparator();
        fileMenu.add(new MenuItem("Exit"));
        menubar.add(fileMenu);
        menu1 = new Menu("Connection");
        menu1.add(DBConn = new MenuItem("Database Connection"));
        menubar.add(menu1);
        RunMenu = new Menu("Run");
        RunMenu.add(start = new MenuItem("Start Test"));
        RunMenu.add(Stop = new MenuItem("Stop Test"));
        Stop.disable();
        menubar.add(RunMenu);
        helpMenu = new Menu("Help");
        helpMenu.add(new MenuItem("About..."));
        menubar.add(helpMenu);
        setMenuBar(menubar);
        setLayout(new BorderLayout());
        addNotify();
        resize(insets().left + insets().right + 486, insets().top + insets().bottom + 253);
        conDisplay = new DebugTextConsole();
        conDisplay.setFont(new Font("Courier", 0, 12));
        add("Center", conDisplay);
        conDisplay.reshape(insets().left, insets().top + 7, 480, 217);
        show();
        Console.setOutput((ConsoleOutput)conDisplay);
        Console.println("Starting JDBCTest ...");
    }

    public boolean action(Event event, Object obj)
    {
        if(event.target instanceof MenuItem)
        {
            String s = (String)obj;
            if(event.target == Stop)
            {
                selectedStopTest();
                return true;
            }
            if(event.target == RegDriver)
            {
                selectedRegDriver();
                return true;
            }
            if(event.target == DBConn)
            {
                selectedDBConn();
                return true;
            }
            if(event.target == start)
            {
                selectedStartTest();
                return true;
            }
            if(s.equalsIgnoreCase("About..."))
            {
                selectedAbout();
                return true;
            }
            if(s.equalsIgnoreCase("Exit"))
            {
                selectedExit();
                return true;
            }
            if(s.equalsIgnoreCase("Open..."))
            {
                selectedOpen();
                return true;
            }
            if(s.equalsIgnoreCase("Start Test"))
            {
                selectedStartTest();
                return true;
            }
        }
        return super.action(event, obj);
    }

    public boolean handleEvent(Event event)
    {
        if(event.id == 201)
        {
            hide();
            dispose();
            System.exit(0);
            return true;
        } else
        {
            return super.handleEvent(event);
        }
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
        new JDBCTestWin();
    }

    public void receiveNotification(Object obj, String s)
    {
        start.enable();
        Stop.disable();
        Log.printOnAll(s);
    }

    public void selectedAbout()
    {
        AboutBox aboutbox = new AboutBox(this);
        aboutbox.show();
    }

    public void selectedDBConn()
    {
        try
        {
            if(conDialog == null)
                conDialog = new ConnectDialog(this);
            conDialog.show();
        }
        catch(Exception _ex) { }
    }

    public void selectedExit()
    {
        QuitBox quitbox = new QuitBox(this);
        quitbox.show();
    }

    public void selectedOpen()
    {
        FileDialog filedialog = new FileDialog(this, "Open...", 0);
        filedialog.setFile("*.log");
        filedialog.show();
        String s = filedialog.getFile();
        String s1 = filedialog.getDirectory();
        filedialog.dispose();
        String s2 = "";
        s2 = new String(s1 + System.getProperty("file.separator") + s);
        DataInputStream datainputstream;
        try
        {
            datainputstream = new DataInputStream(new FileInputStream(s2));
        }
        catch(FileNotFoundException _ex)
        {
            Console.println("File " + s + " not found!");
            return;
        }
        boolean flag = false;
        String s3 = "";
        Console.Clear();
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
            Console.println(s3);
            try
            {
                s3 = datainputstream.readLine();
            }
            catch(IOException _ex)
            {
                flag = true;
            }
        }
        try
        {
            datainputstream.close();
        }
        catch(IOException _ex) { }
    }

    public void selectedRegDriver()
    {
    }

    public void selectedStartTest()
    {
        conDisplay.setText("");
        loadINIFile();
        setLogFiles();
        if(conDialog == null)
        {
            Console.println("\n\nERROR:  Database connections must be defined!.\n");
            return;
        }
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
            TestDriver testdriver = new TestDriver(conDialog.getUrl(), conDialog.getUserID(), conDialog.getPassword(), properties, ini);
            testDriver = testdriver;
            testDriver.subscribe(this);
            testdriver.start();
            start.disable();
            Stop.enable();
            return;
        }
    }

    public void selectedStop()
    {
        start.enable();
        Stop.disable();
    }

    public void selectedStopTest()
    {
        Log.printOnAll("**** Test Run Aborted " + new Date() + "****");
        testDriver.stop();
        start.enable();
        Stop.disable();
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

    public synchronized void show()
    {
        move(50, 50);
        super.show();
    }

    Menu fileMenu;
    Menu menu1;
    MenuItem RegDriver;
    MenuItem DBConn;
    Menu RunMenu;
    MenuItem start;
    MenuItem Stop;
    Menu helpMenu;
    ConnectDialog conDialog;
    TestDriver testDriver;
    INIFile ini;
    TextArea conDisplay;
}
