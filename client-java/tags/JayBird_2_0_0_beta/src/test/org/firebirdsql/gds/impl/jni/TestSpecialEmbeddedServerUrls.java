package org.firebirdsql.gds.impl.jni;

import junit.framework.TestCase;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.jdbc.FBDriver;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by IntelliJ IDEA.
 * User: Ryan Baldwin
 * Date: 15-Oct-2003
 * Time: 17:33:53
 * To change this template use Options | File Templates.
 */
public class TestSpecialEmbeddedServerUrls extends TestCase
    {
     protected final Logger log = LoggerFactory.getLogger(getClass(),true);


    public TestSpecialEmbeddedServerUrls(String s)
        {
        super(s);
        }

    protected void setUp() throws Exception
        {
        try
            {
            Class.forName(FBDriver.class.getName());

            fbManager = new FBManager(GDSType.getType("EMBEDDED"));

            fbManager.setServer("localhost");
            fbManager.setPort(5066);
            fbManager.start();

            mRelativeDatabasePath = "db/" + "testES01874.fdb";
            mAbsoluteDatabasePath = new File("").getCanonicalPath() + "/"+mRelativeDatabasePath;

            fbManager.createDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
            }
        catch (Exception e)
            {
            if (log!=null) log.warn("exception in setup of " + getName() + ": ", e);
            } // end of try-catch
        }

    protected void tearDown() throws Exception
        {
        try
            {
            fbManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
            fbManager.stop();
            fbManager = null;
            }
        catch (Exception e)
            {
            if (log!=null) log.warn("exception in teardown of " + getName() + ": ", e);
            } // end of try-catch
        }

    public void testFBManagerWithoutSettingServerAndPort() throws Exception
        {
        FBManager testFBManager = new FBManager(GDSType.getType("EMBEDDED"));
        testFBManager.start();

        testFBManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        testFBManager.createDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");

        testFBManager.stop();
        }

    public void testFBManagerWithRelativeDatabaseFile() throws Exception
        {
        FBManager testFBManager = new FBManager(GDSType.getType("EMBEDDED"));
        testFBManager.start();

        testFBManager.dropDatabase(mRelativeDatabasePath, "SYSDBA", "masterkey");
        testFBManager.createDatabase(mRelativeDatabasePath, "SYSDBA", "masterkey");

        testFBManager.stop();
        }

    public void testDriverManagerGetConnectionWithoutServerAndPortInUrl() throws Exception
        {
        Connection connection = DriverManager.getConnection("jdbc:firebirdsql:embedded:"+mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        connection.close();
        }

    public void testDriverManagerGetConnectionWithoutServerAndPortInUrlWithRelativeDatabasePath() throws Exception
        {
        Connection connection = DriverManager.getConnection("jdbc:firebirdsql:embedded:"+mRelativeDatabasePath, "SYSDBA", "masterkey");
        connection.close();
        }



    private String mRelativeDatabasePath = null;
    private String mAbsoluteDatabasePath = null;

    private FBManager fbManager = null;
    }
