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
package org.firebirdsql.pool.sun;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;

import org.firebirdsql.ds.RootCommonDataSource;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.firebirdsql.pool.FBWrappingDataSource;

/**
 * Implementation of the {@link javax.sql.DataSource} interface for the Sun
 * Application Server 8.x (tested with PE 8.1). The main difference to the
 * {@link org.firebirdsql.pool.FBWrappingDataSource} class is that property
 * "userName" is renamed into "user" and the possibilities to set the custom
 * properties is removed. All this is caused by a quite strange algorithm that
 * Sun software engineers had chosen to set data source properties.
 * <p>
 * This class also acts as an object factory for the JNDI de-referencing.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class AppServerDataSource extends RootCommonDataSource implements DataSource, Referenceable,
        Serializable, ObjectFactory {

    private static final long serialVersionUID = 7129672234735607712L;
    
    private FBWrappingDataSource dataSource;

    private AppServerDataSource(FBWrappingDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public AppServerDataSource() {
        this.dataSource = new FBWrappingDataSource();
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public Connection getConnection(String username, String password)
            throws SQLException {
        return dataSource.getConnection(username, password);
    }

    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    public int getBlobBufferSize() {
        return dataSource.getBlobBufferSize();
    }

    public void setBlobBufferSize(int value) {
        dataSource.setBlobBufferSize(value);
    }

    public int getBlockingTimeout() {
        return dataSource.getBlockingTimeout();
    }

    public void setBlockingTimeout(int value) {
        dataSource.setBlockingTimeout(value);
    }

    public int getBuffersNumber() {
        return dataSource.getBuffersNumber();
    }

    public void setBuffersNumber(int value) {
        dataSource.setBuffersNumber(value);
    }

    public String getCharSet() {
        return dataSource.getCharSet();
    }

    public void setCharSet(String value) {
        dataSource.setCharSet(value);
    }

    public String getDatabaseName() {
        return dataSource.getDatabase();
    }

    public void setDatabaseName(String value) {
        dataSource.setDatabase(value);
    }

    public String getDefaultIsolation() {
        return dataSource.getDefaultIsolation();
    }

    public void setDefaultIsolation(String value) {
        dataSource.setDefaultIsolation(value);
    }

    public String getEncoding() {
        return dataSource.getEncoding();
    }

    public void setEncoding(String value) {
        dataSource.setEncoding(value);
    }

    public boolean isKeepStatements() {
        return dataSource.isKeepStatements();
    }

    public void setKeepStatements(boolean value) {
        dataSource.setKeepStatements(value);
    }

    public int getMaxIdleTime() {
        return dataSource.getMaxIdleTime();
    }

    public void setMaxIdleTime(int value) {
        dataSource.setMaxIdleTime(value);
    }

    public int getMaxPoolSize() {
        return dataSource.getMaxPoolSize();
    }

    public void setMaxPoolSize(int value) {
        dataSource.setMaxPoolSize(value);
    }

    public int getMaxStatements() {
        return dataSource.getMaxStatements();
    }

    public void setMaxStatements(int value) {
        dataSource.setMaxStatements(value);
    }

    public int getMinPoolSize() {
        return dataSource.getMinPoolSize();
    }

    public void setMinPoolSize(int value) {
        dataSource.setMinPoolSize(value);
    }

    public String getPassword() {
        return dataSource.getPassword();
    }

    public void setPassword(String value) {
        dataSource.setPassword(value);
    }

    public int getPingInterval() {
        return dataSource.getPingInterval();
    }

    public void setPingInterval(int value) {
        dataSource.setPingInterval(value);
    }

    public String getPingStatement() {
        return dataSource.getPingStatement();
    }

    public void setPingStatement(String value) {
        dataSource.setPingStatement(value);
    }

    public boolean isPooling() {
        return dataSource.isPooling();
    }

    public void setPooling(boolean value) {
        dataSource.setPooling(value);
    }

    public int getRetryInterval() {
        return dataSource.getRetryInterval();
    }

    public void setRetryInterval(int value) {
        dataSource.setRetryInterval(value);
    }

    public String getRoleName() {
        return dataSource.getRoleName();
    }

    public void setRoleName(String value) {
        dataSource.setRoleName(value);
    }

    public int getSocketBufferSize() {
        return dataSource.getSocketBufferSize();
    }

    public void setSocketBufferSize(int value) {
        dataSource.setSocketBufferSize(value);
    }

    public String getSqlDialect() {
        return dataSource.getSqlDialect();
    }

    public void setSqlDialect(String value) {
        dataSource.setSqlDialect(value);
    }

    public boolean isStatementPooling() {
        return dataSource.isStatementPooling();
    }

    public void setStatementPooling(boolean value) {
        dataSource.setStatementPooling(value);
    }

    public boolean isTimestampUsesLocalTimezone() {
        return dataSource.isTimestampUsesLocalTimezone();
    }

    public void setTimestampUsesLocalTimezone(boolean value) {
        dataSource.setTimestampUsesLocalTimezone(value);
    }

    public String getType() {
        return dataSource.getType();
    }

    public void setType(String value) {
        dataSource.setType(value);
    }

    public String getUser() {
        return dataSource.getUserName();
    }

    public void setUser(String value) {
        dataSource.setUserName(value);
    }

    public boolean isUseStandardUdf() {
        return dataSource.isUseStandardUdf();
    }

    public void setUseStandardUdf(boolean value) {
        dataSource.setUseStandardUdf(value);
    }

    public boolean isUseStreamBlobs() {
        return dataSource.isUseStreamBlobs();
    }

    public void setUseStreamBlobs(boolean value) {
        dataSource.setUseStreamBlobs(value);
    }

    public String getUseTranslation() {
        return dataSource.getUseTranslation();
    }

    public void setUseTranslation(String value) {
        dataSource.setUseTranslation(value);
    }

    public Reference getReference() throws NamingException {
        Reference ref = new Reference(getClass().getName(), getClass()
                .getName(), null);

        Reference defaultRef = dataSource.getDefaultReference();
        convertReference(ref, defaultRef);

        return ref;
    }

    private void convertReference(Reference ref, Reference defaultRef) {
        Enumeration en = defaultRef.getAll();
        while (en.hasMoreElements()) {
            RefAddr addr = (RefAddr) en.nextElement();
            ref.add(addr);
        }
    }

    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
            Hashtable environment) throws Exception {

        if (!(obj instanceof Reference)) return null;

        Reference passedRef = (Reference) obj;
        passedRef = (Reference) passedRef.clone();

        if (!getClass().getName().equals(passedRef.getClassName()))
            return null;

        Reference ref = new Reference(FBWrappingDataSource.class.getName(),
                FBWrappingDataSource.class.getName(), null);

        convertReference(ref, passedRef);

        FBWrappingDataSource objectFactory = new FBWrappingDataSource();
        FBWrappingDataSource dataSource = (FBWrappingDataSource) objectFactory
                .getObjectInstance(ref, name, nameCtx, environment);

        return new AppServerDataSource(dataSource);
    }

    
    // JDBC 4.0
    
    public boolean isWrapperFor(Class iface) throws SQLException {
    	return false;
    }
    
    public Object unwrap(Class iface) throws SQLException {
    	throw new FBDriverNotCapableException();
    }
}
