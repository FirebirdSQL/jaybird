package org.firebirdsql.management;

import org.firebirdsql.gds.*;
import org.firebirdsql.jgds.*;

import java.sql.SQLException;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

public class FBManager implements FBManagerMBean, MBeanRegistration {

   private final Logger log = LoggerFactory.getLogger(getClass(),true);

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

    private String fileName;

    private String userName;

    private String password;

    private boolean createOnStart = false;

    private boolean dropOnStop = false;


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
        c.append(GDSFactory.newClumplet(gds.isc_dpb_sql_dialect, new byte[] {3, 0, 0, 0}));

        state = STARTED;
        if (isCreateOnStart()) 
        {
            createDatabase(getFileName(), getUserName(), getPassword());
        } // end of if ()
        
    }

    public void stop() throws Exception {
        if (isDropOnStop()) 
        {
            dropDatabase(getFileName(), getUserName(), getPassword());
        } // end of if ()
        
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

    public void setServer(final String host) {
        this.host = host;
    }

    public String getServer() {
        return host;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    
    
    /**
     * mbean get-set pair for field fileName
     * Get the value of fileName
     * @return value of fileName
     *
     * @jmx:managed-attribute
     */
    public String getFileName()
    {
        return fileName;
    }
    
    
    /**
     * Set the value of fileName
     * @param fileName  Value to assign to fileName
     *
     * @jmx:managed-attribute
     */
    public void setFileName(final String fileName)
    {
        this.fileName = fileName;
    }
    
    
    
    
    /**
     * mbean get-set pair for field userName
     * Get the value of userName
     * @return value of userName
     *
     * @jmx:managed-attribute
     */
    public String getUserName()
    {
        return userName;
    }
    
    
    /**
     * Set the value of userName
     * @param userName  Value to assign to userName
     *
     * @jmx:managed-attribute
     */
    public void setUserName(final String userName)
    {
        this.userName = userName;
    }
    
    
    
    /**
     * mbean get-set pair for field password
     * Get the value of password
     * @return value of password
     *
     * @jmx:managed-attribute
     */
    public String getPassword()
    {
        return password;
    }
    
    
    /**
     * Set the value of password
     * @param password  Value to assign to password
     *
     * @jmx:managed-attribute
     */
    public void setPassword(final String password)
    {
        this.password = password;
    }
    
    
    
    
    /**
     * mbean get-set pair for field createOnStart
     * Get the value of createOnStart
     * @return value of createOnStart
     *
     * @jmx:managed-attribute
     */
    public boolean isCreateOnStart()
    {
        return createOnStart;
    }
    
    
    /**
     * Set the value of createOnStart
     * @param createOnStart  Value to assign to createOnStart
     *
     * @jmx:managed-attribute
     */
    public void setCreateOnStart(final boolean createOnStart)
    {
        this.createOnStart = createOnStart;
    }
    
    
    
    
    /**
     * mbean get-set pair for field dropOnStop
     * Get the value of dropOnStop
     * @return value of dropOnStop
     *
     * @jmx:managed-attribute
     */
    public boolean isDropOnStop()
    {
        return dropOnStop;
    }
    
    
    /**
     * Set the value of dropOnStop
     * @param dropOnStop  Value to assign to dropOnStop
     *
     * @jmx:managed-attribute
     */
    public void setDropOnStop(final boolean dropOnStop)
    {
        this.dropOnStop = dropOnStop;
    }
    
    


    //Meaningful management methods

    public void createDatabase (String fileName, String user, String password) throws Exception {
        isc_db_handle db = gds.get_new_isc_db_handle();
        try {
           Clumplet dpb = GDSFactory.cloneClumplet(c);
           dpb.append(GDSFactory.newClumplet(GDS.isc_dpb_user_name, user));
           dpb.append(GDSFactory.newClumplet(GDS.isc_dpb_password, password));
            gds.isc_create_database(getConnectString(fileName), db, dpb);
            gds.isc_detach_database(db);
        }
        catch (Exception e) {
           if (log!=null) log.error("Exception creating database", e);
           throw e;
        }
    }

    public void dropDatabase(String fileName, String user, String password) throws Exception {
        try {
            isc_db_handle db = gds.get_new_isc_db_handle();
           Clumplet dpb = GDSFactory.cloneClumplet(c);
           dpb.append(GDSFactory.newClumplet(GDS.isc_dpb_user_name, user));
           dpb.append(GDSFactory.newClumplet(GDS.isc_dpb_password, password));
            gds.isc_attach_database(getConnectString(fileName), db, dpb);
            gds.isc_drop_database(db);

        }
        catch (Exception e) {
           if (log!=null) log.error("Exception dropping database", e);

            throw e;
        }
    }

    //private methods
    private String getConnectString(String filename) {
        String fileString = getServer() + "/" + getPort() + ":" + filename;
        return fileString;
    }

}


