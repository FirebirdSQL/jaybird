 /*
 * Firebird Open Source J2ee connector - jdbc driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */

package org.firebirdsql.management;

import java.util.HashMap;
import java.util.Map;

import org.firebirdsql.gds.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * The class <code>FBManager</code> is a simple jmx mbean that allows you
 * to create and drop databases.  in particular, they can be created and
 * dropped using the jboss service lifecycle operations start and stop.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 * @jmx.mbean
 */
public class FBManager implements FBManagerMBean
	{
    private final static Logger log = LoggerFactory.getLogger(FBManager.class,false);

    private GDS gds;

    private Clumplet c;


    private String host = "localhost";

    private int port = 3050;

    private String fileName;

    private String userName;

    private String password;

    private boolean forceCreate = false;

    private boolean createOnStart = false;

    private boolean dropOnStop = false;

    private String state = STOPPED;

    private static final String STOPPED = "Stopped";
    private static final String STARTED = "Started";



    private GDSType type;


    public FBManager()
        {
        this(GDSType.PURE_JAVA);
        }

    public FBManager(GDSType type)
        {
        this.type = type;
        }






	//Service methods
    /**
     * Describe <code>start</code> method here.
     *
     * @exception Exception if an error occurs
     * @jmx.managed-operation
     */
    public void start() throws Exception {
        gds = GDSFactory.getGDSForType(type);
        c = gds.newClumplet(ISCConstants.isc_dpb_num_buffers, new byte[] {90});
        c.append(gds.newClumplet(ISCConstants.isc_dpb_dummy_packet_interval, new byte[] {120, 10, 0, 0}));
        c.append(gds.newClumplet(ISCConstants.isc_dpb_sql_dialect, new byte[] {3, 0, 0, 0}));

        state = STARTED;
        if (isCreateOnStart())
        {
            createDatabase(getFileName(), getUserName(), getPassword());
        } // end of if ()

    }

    /**
     * Describe <code>stop</code> method here.
     *
     * @exception Exception if an error occurs
     * @jmx.managed-operation
     */
    public void stop() throws Exception {
        if (isDropOnStop())
        {
            dropDatabase(getFileName(), getUserName(), getPassword());
        } // end of if ()

        c = null;
        gds.close();
	gds = null;
        state = STOPPED;
    }


    /**
     * Describe <code>getState</code> method here.
     *
     * @return a <code>String</code> value
     * @jmx.managed-attribute
     */
    public String getState()
    {
        return state;
    }


    /**
     * Describe <code>getName</code> method here.
     *
     * @return a <code>String</code> value
     * @jmx.managed-attribute
     */
    public String getName() {
        return "Firebird Database manager";
    }






    //Firebird specific methods
    //Which server are we connecting to?

    /**
     * Describe <code>setServer</code> method here.
     *
     * @param host a <code>String</code> value
     * @jmx.managed-attribute
     */
    public void setServer(final String host) {
        this.host = host;
    }

    /**
     * Describe <code>getServer</code> method here.
     *
     * @return a <code>String</code> value
     * @jmx.managed-attribute
     */
    public String getServer() {
        return host;
    }

    /**
     *
     * @return
     */
    public String getType() {
        return this.type.toString();
    }

    /**
     * Describe <code>setPort</code> method here.
     *
     * @param port an <code>int</code> value
     * @jmx.managed-attribute
     */
    public void setPort(final int port) {
        this.port = port;
    }

    /**
     * Describe <code>getPort</code> method here.
     *
     * @return an <code>int</code> value
     * @jmx.managed-attribute
     */
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

    public void setType(String type) {
        this.type = GDSType.getType(type);
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


    /**
     * Get the ForceCreate value.
     * @return the ForceCreate value.
     *
     * @jmx:managed-attribute
     */
    public boolean isForceCreate() {
	return forceCreate;
    }

    /**
     * Set the ForceCreate value.
     * @param forceCreate The new ForceCreate value.
     *
     * @jmx:managed-attribute
     */
    public void setForceCreate(boolean forceCreate) {
	this.forceCreate = forceCreate;
    }



    //Meaningful management methods

    /**
     * Describe <code>createDatabase</code> method here.
     *
     * @param fileName a <code>String</code> value
     * @param user a <code>String</code> value
     * @param password a <code>String</code> value
     * @exception Exception if an error occurs
     *
     * @jmx.managed-operation
     */
    public void createDatabase (String fileName, String user, String password)
        throws Exception
    {
        isc_db_handle db = null;
	if (!forceCreate) {
	    db = gds.get_new_isc_db_handle();
	    try {
		Clumplet dpb = gds.cloneClumplet(c);
		dpb.append(gds.newClumplet(ISCConstants.isc_dpb_user_name, user));
		dpb.append(gds.newClumplet(ISCConstants.isc_dpb_password, password));
		gds.isc_attach_database(getConnectString(fileName), db, dpb);
		gds.isc_detach_database(db);
		return; //database exists, don't wipe it out.
	    }
	    catch (Exception e)
	    {
		//db does not exist, create it.
	    }

	} // end of if ()

	db = gds.get_new_isc_db_handle();
        try {
            Clumplet dpb = gds.cloneClumplet(c);
            dpb.append(gds.newClumplet(ISCConstants.isc_dpb_user_name, user));
            dpb.append(gds.newClumplet(ISCConstants.isc_dpb_password, password));
            gds.isc_create_database(getConnectString(fileName), db, dpb);
            gds.isc_detach_database(db);
        }
        catch (Exception e) {
	    if (log!=null)
	    {
		log.error("Exception creating database", e);
	    }
	    throw e;
        }
    }

    /**
     * Describe <code>dropDatabase</code> method here.
     *
     * @param fileName a <code>String</code> value
     * @param user a <code>String</code> value
     * @param password a <code>String</code> value
     * @exception Exception if an error occurs
     * @jmx.managed-operation
     */
    public void dropDatabase(String fileName, String user, String password)
        throws Exception
    {
        try {
            isc_db_handle db = gds.get_new_isc_db_handle();
            Clumplet dpb = gds.cloneClumplet(c);
            dpb.append(gds.newClumplet(ISCConstants.isc_dpb_user_name, user));
            dpb.append(gds.newClumplet(ISCConstants.isc_dpb_password, password));
            gds.isc_attach_database(getConnectString(fileName), db, dpb);
            gds.isc_drop_database(db);

        }
        catch (Exception e) {
            if (log!=null)
            {
                log.error("Exception dropping database", e);
            }
            throw e;
        }
    }

    //private methods
    private String getConnectString(String filename) {
        String fileString = getServer() + "/" + getPort() + ":" + filename;
        return fileString;
    }

}


