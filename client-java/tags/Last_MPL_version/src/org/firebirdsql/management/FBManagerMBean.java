/**
 * This code is licensed under the lgpl.  for details consult gnu.org.
 */
package org.firebirdsql.management;

import org.firebirdsql.gds.*;
import org.firebirdsql.jgds.*;

import java.sql.SQLException;



/**
 * Describe interface <code>FBManagerMBean</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface FBManagerMBean {

    public void start()
        throws Exception;

    public void stop()
        throws Exception;

    public String getName();

    public String getState();

    public void setServer(String host);

    public String getServer();

    public void setPort(int port);

    public int getPort();

    public String getFileName();
    
    /**
     * Set the value of fileName
     * @param fileName  Value to assign to fileName
     *
     * @jmx:managed-attribute
     */
    public void setFileName(final String fileName);
    
    
    /**
     * mbean get-set pair for field userName
     * Get the value of userName
     * @return value of userName
     *
     * @jmx:managed-attribute
     */
    public String getUserName();
    
    
    /**
     * Set the value of userName
     * @param userName  Value to assign to userName
     *
     * @jmx:managed-attribute
     */
    public void setUserName(final String userName);
    
    
    
    /**
     * mbean get-set pair for field password
     * Get the value of password
     * @return value of password
     *
     * @jmx:managed-attribute
     */
    public String getPassword();
    
    
    /**
     * Set the value of password
     * @param password  Value to assign to password
     *
     * @jmx:managed-attribute
     */
    public void setPassword(final String password);
    
    
    
    /**
     * mbean get-set pair for field createOnStart
     * Get the value of createOnStart
     * @return value of createOnStart
     *
     * @jmx:managed-attribute
     */
    public boolean isCreateOnStart();
    
    /**
     * Set the value of createOnStart
     * @param createOnStart  Value to assign to createOnStart
     *
     * @jmx:managed-attribute
     */
    public void setCreateOnStart(final boolean createOnStart);
    
    
    
    
    /**
     * mbean get-set pair for field dropOnStop
     * Get the value of dropOnStop
     * @return value of dropOnStop
     *
     * @jmx:managed-attribute
     */
    public boolean isDropOnStop();
    
    
    /**
     * Set the value of dropOnStop
     * @param dropOnStop  Value to assign to dropOnStop
     *
     * @jmx:managed-attribute
     */
    public void setDropOnStop(final boolean dropOnStop);   
    
    public void createDatabase (String filename, String user, String password) throws Exception;

    public void dropDatabase(String fileName, String user, String password) throws Exception;


}
