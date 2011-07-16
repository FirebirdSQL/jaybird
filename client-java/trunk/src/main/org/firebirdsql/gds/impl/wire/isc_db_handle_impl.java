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
/*
 *
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */

package org.firebirdsql.gds.impl.wire;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.security.auth.Subject;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.AbstractIscDbHandle;
import org.firebirdsql.gds.impl.GDSServerVersion;

/**
 * Describe class <code>isc_db_handle_impl</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class isc_db_handle_impl extends AbstractIscDbHandle {
    
    private static final int DEFAULT_RESP_DATA = 65536;
    
    private int rdb_id;
    private Subject subject;
    private Collection rdb_transactions = new ArrayList();
    private List rdb_warnings = new ArrayList();
    
    public boolean invalid;

    Socket socket;
    public XdrOutputStream out;
    public WireXdrInputStream in;
    private int resp_object;
    private long resp_blob_id;
    private byte[] resp_data = new byte[DEFAULT_RESP_DATA];
    private int resp_data_len;

    private int dialect = 0;
    private int protocol = 0;
    private String version = null;
    private GDSServerVersion serverVersion;
    private int ODSMajorVersion = 0;
    private int ODSMinorVersion = 0;

    EventCoordinator eventCoordinator = null;
	 
    public isc_db_handle_impl() {
    }

    public boolean isValid()
    {
        return !invalid;
    }

    protected void invalidate() throws IOException {
        
        if (invalid)
            return;
        
        in.close();
        out.close();
        socket.close();
        
        in = null;
        out = null;
        socket = null;

        invalid = true;
    }
    
    /* TODO Implement statement handle tracking correctly */
    // Vector rdb_sql_requests = new Vector();
    

    public void setRdb_id(int rdb_id) {
        checkValidity();
        this.rdb_id = rdb_id;
    }

    public int getRdb_id() {
        checkValidity();
        return rdb_id;
    }

    void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Subject getSubject() {
        return subject;
    }

    public boolean hasTransactions()
    {
        checkValidity();
        return !rdb_transactions.isEmpty();
    }
    
    int getOpenTransactionCount() {
        checkValidity();
        return rdb_transactions.size();
    }

    void addTransaction(isc_tr_handle_impl tr)
    {
        checkValidity();
        rdb_transactions.add(tr);
    }

    void removeTransaction(isc_tr_handle_impl tr)
    {
        checkValidity();
        rdb_transactions.remove(tr);
    }
    
    public Collection getTransactions()
    {
        return new ArrayList(rdb_transactions);
    }
    
    public List getWarnings() {
        checkValidity();
        synchronized(rdb_warnings) {
            return new ArrayList(rdb_warnings);
        }
    }
    
    public void addWarning(GDSException warning) {
        checkValidity();
        synchronized(rdb_warnings) {
            rdb_warnings.add(warning);
        }
    }
    
    public void clearWarnings() {
        checkValidity();
        synchronized(rdb_warnings) {
            rdb_warnings.clear();
        }
    }
    //
    //
    //
    public void setDialect(int value){
        dialect = value;
    }

    public int getDialect(){
        return dialect;
    }

    public void setProtocol(int value){
        protocol = value;
    }

    public int getProtocol(){
        return protocol;
    }

    public void setVersion(String value) throws GDSException {
        version = value;
        serverVersion = GDSServerVersion.parseRawVersion(value);
    }

    public String getVersion(){
        return version;
    }

    public String getDatabaseProductName(){
        return serverVersion.getServerName();
    }

    public String getDatabaseProductVersion(){
        return serverVersion.getFullVersion();
    }

    public int getDatabaseProductMajorVersion(){
        return serverVersion.getMajorVersion();
    }

    public int getDatabaseProductMinorVersion(){
        return serverVersion.getMinorVersion();
    }

    public void setODSMajorVersion(int value){
        ODSMajorVersion = value;
    }

    public int getODSMajorVersion(){
        return ODSMajorVersion;
    }

    public void setODSMinorVersion(int value){
        ODSMinorVersion = value;
    }

    public int getODSMinorVersion(){
        return ODSMinorVersion;
    }

    public void setResp_object(int value){
        resp_object = value;
    }

    public int getResp_object(){
        return resp_object;
    }

    public void setResp_blob_id(long value){
        resp_blob_id = value;
    }

    public long getResp_blob_id(){
        return resp_blob_id;
    }

    public void setResp_data(byte[] value){
        resp_data = value;
    }

    public byte[] getResp_data(){
        return resp_data;
    }

    public byte[] getResp_data_truncated() {
        byte[] dest = new byte[getResp_data_len()];
        System.arraycopy(resp_data, 0, dest, 0, dest.length);
        return dest;
    }
    
    public void setResp_data_len(int len) {
        this.resp_data_len = len;
    }
    
    public int getResp_data_len() {
        return this.resp_data_len;
    }
    
    private void checkValidity() {
        if (invalid)
            throw new IllegalStateException(
                "This database handle is invalid and cannot be used anymore.");
    }
}
