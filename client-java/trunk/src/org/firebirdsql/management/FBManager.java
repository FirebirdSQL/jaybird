package org.firebirdsql.management;

import org.firebirdsql.gds.*;
import org.firebirdsql.jgds.*;

import java.sql.SQLException;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class FBManager implements FBManagerMBean, MBeanRegistration {

    // Constants -----------------------------------------------------
    public static final String[] states = {"Stopped","Stopping","Starting","Started"};
    public static final int STOPPED  = 0;
    public static final int STOPPING = 1;
    public static final int STARTING = 2;
    public static final int STARTED  = 3;


    private MBeanServer server;

    private int state = 0;

    private String name;

    private GDS gds;

    private Clumplet c;


    private String host = "localhost";

    private int port = 3050;


    public FBManager() {}

    // Public --------------------------------------------------------
    public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
    {
        this.server = server;
        return name==null ? new ObjectName(OBJECT_NAME) : name;
    }

    //MBeanRegistration methods
    public ObjectName preRegister(MBeanServer server, ObjectName name)
        throws java.lang.Exception
    {
        System.out.println("in FBManager preRegister");
        name = getObjectName(server, name);
        this.server = server;
        return name;
    }

    public void postRegister(java.lang.Boolean registrationDone)
    {
        if (!registrationDone.booleanValue()) {
//            destroy();
        }
    }

    public void preDeregister()
      throws java.lang.Exception
    {
    }

    public void postDeregister()
    {
//        destroy();
    }

    //Service methods
    public void start() throws Exception {
        state = STARTING;
        gds = GDSFactory.newGDS();
        c = GDSFactory.newClumplet(gds.isc_dpb_num_buffers, new byte[] {90});
        c.append(GDSFactory.newClumplet(gds.isc_dpb_dummy_packet_interval, new byte[] {120, 10, 0, 0}));

        state = STARTED;
    }

    public void stop() {
        state = STOPPING;
        c = null;
        gds = null;
        state = STOPPED;
    }



    public String getName() {
        return "Firebird Database manager";
    }

    public int getState() {
        return state;
    }

    public String getStateString() {
        return states[state];
    }

    //Firebird specific methods
    //Which server are we connecting to?

    public void setURL(String host) {
        this.host = host;
    }

    public String getURL() {
        return host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    //Meaningful management methods

    public void createDatabase (String fileName) throws Exception {
        isc_db_handle db = gds.get_new_isc_db_handle();
//        isc_tr_handle tr = gds.get_new_isc_tr_handle();
        try {
//            gds.isc_dsql_execute_immediate(db, tr, "CREATE DATABASE '" + fileName + "' USER 'sysdba' PASSWORD 'masterkey'", GDS.SQL_DIALECT_CURRENT, null);
//            gds.isc_dsql_execute_immediate(db, tr, "CREATE DATABASE '" + fileName + "'", 1, null);
//            gds.isc_create_database2(getConnectString(fileName), db);//Only creates dialect 1!!!
            gds.isc_create_database(getConnectString(fileName), db, c);//Only creates dialect 1!!!
            gds.isc_detach_database(db);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void dropDatabase(String fileName) throws Exception {
        try {
            isc_db_handle db = gds.get_new_isc_db_handle();
            gds.isc_attach_database(getConnectString(fileName), db, c);
            gds.isc_drop_database(db);

        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //private methods
    private String getConnectString(String filename) {
        String fileString = getURL() + "/" + getPort() + ":" + filename;// + getPort() + ":"
        System.out.println("file string: " + fileString);
        return fileString;
    }

}


