package org.firebirdsql.management;

import org.firebirdsql.gds.*;
import org.firebirdsql.jgds.*;

import java.sql.SQLException;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


public interface FBManagerMBean {

    public final static String OBJECT_NAME = "DefaultDomain:service=firebirdsqlmanager";

    public void start()
        throws Exception;

    public void stop();

    public String getName();
    public int getState();
    public String getStateString();

    public void setURL(String host);

    public String getURL();

    public void setPort(int port);

    public int getPort();

    public void createDatabase (String filename, String user, String password) throws Exception;

    public void dropDatabase(String fileName, String user, String password) throws Exception;


}
