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

package org.firebirdsql.jca;


import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.resource.ResourceException;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jdbc.AbstractConnection;
import org.firebirdsql.jdbc.FBConnectionHelper;
import org.firebirdsql.jdbc.FBDriverPropertyManager;
import org.firebirdsql.jdbc.FBSQLException;

/**
 * A simple non-pooling XADataSource implementation.
 *
 * @author <a href="mailto:lorban@bitronix.be">Ludovic Orban</a>
 * @version 1.0
 * @deprecated Use {@link org.firebirdsql.ds.FBXADataSource}
 */
public class FBXADataSource implements XADataSource {
    
    private Map mcfToDataSourceMap = new HashMap();
    //private ConnectionManager cm = new FBStandAloneConnectionManager();
    private String user;
    private String password;
    private String database;

    public FBXADataSource() {
    }


    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabase() {
        return database;
    }
    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }


    private String getUrl() {
        return "jdbc:firebirdsql:" + database;
    }

    
    private static AbstractConnection createConnection(String url, String user, String password) throws ResourceException, SQLException, GDSException {
        FBManagedConnectionFactory mcf = createMcf(url, user, password);

        FBConnectionRequestInfo subjectCri = mcf.getDefaultConnectionRequestInfo();
        subjectCri.setUserName(user);
        subjectCri.setPassword(password);

        FBManagedConnection mc = (FBManagedConnection)((FBManagedConnectionFactory)mcf).createManagedConnection(null, subjectCri);
        mc.setManagedEnvironment(false);
        mc.setConnectionSharing(false);
        return (AbstractConnection) mc.getConnection(null, subjectCri);
    }


    private static FBManagedConnectionFactory createMcf(String url, String user, String password) throws SQLException, ResourceException, GDSException, FBResourceException {
        GDSType type = GDSFactory.getTypeForProtocol(url);

        Properties originalInfo = new Properties();
        originalInfo.setProperty("user", user);
        originalInfo.setProperty("password", password);

        Map normalizedInfo = FBDriverPropertyManager.normalize(url, originalInfo);
        int qMarkIndex = url.indexOf('?');
        if (qMarkIndex != -1)
            url = url.substring(0, qMarkIndex);
        
        FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(type);

        FBConnectionRequestInfo subjectCri = mcf.getDefaultConnectionRequestInfo();
        subjectCri.setUserName(user);
        subjectCri.setPassword(password);
        
        String databaseURL = GDSFactory.getDatabasePath(type, url);

        mcf.setDatabase(databaseURL);
        for (Iterator iter = normalizedInfo.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            
            mcf.setNonStandardProperty((String)entry.getKey(), (String)entry.getValue());
        }

        FBConnectionHelper.processTpbMapping(mcf.getGDS(), mcf, originalInfo);
        
        mcf = mcf.canonicalize();
        return mcf;
    }

    public XAConnection getXAConnection() throws SQLException {
        return getXAConnection(user, password);
    }


    public XAConnection getXAConnection(String user, String password) throws SQLException {
        if (database == null)
            throw new SQLException("database cannot be null");
        if (user == null)
            throw new SQLException("user cannot be null");
        if (password == null)
            throw new SQLException("password cannot be null");
        
        try {
            AbstractConnection c = createConnection(getUrl(), user, password);
            return new FBXAConnection(c);
        } catch (ResourceException ex) {
            throw new FBSQLException(ex);
        } catch (GDSException ex) {
            throw new FBSQLException(ex);
        }
    }


    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }
    public void setLogWriter(PrintWriter writer) throws SQLException {
    }

    public int getLoginTimeout() throws SQLException {
        return 0;
    }
    public void setLoginTimeout(int timeout) throws SQLException {
    }

}





