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

package org.firebirdsql.jgds;

import java.util.*;
import java.net.*;

import javax.security.auth.Subject;

import org.firebirdsql.gds.GDSException;

/**
 * Describe class <code>isc_db_handle_impl</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public final class isc_db_handle_impl implements org.firebirdsql.gds.isc_db_handle {
    private int rdb_id;
    private Subject subject;
    private Collection rdb_transactions = new ArrayList();
    private List rdb_warnings = new ArrayList();
    
    private boolean invalid;

    Socket socket;
    XdrOutputStream out;
    XdrInputStream in;
    private int resp_object;
    private long resp_blob_id;
    private byte[] resp_data;

    private int dialect = 0;
    private int protocol = 0;
    private String version = null;
    private String FBVersion = null;
    private int ODSMajorVersion = 0;
    private int ODSMinorVersion = 0;
	 
    private void checkValidity() {
        if (invalid)
            throw new IllegalStateException(
                "This database handle is invalid and cannot be used anymore.");
    }
    
    void invalidate() throws java.io.IOException {
        in.close();
        out.close();
        socket.close();
        
        in = null;
        out = null;
        socket = null;

        invalid = true;
    }
    
    /** @todo Implement statement handle tracking correctly */
    // Vector rdb_sql_requests = new Vector();
    

    public isc_db_handle_impl() {
    }

    void setRdb_id(int rdb_id) {
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

    public void setVersion(String value){
        version = value;
    }

    public String getVersion(){
        return version;
    }

    public String getDatabaseProductName(){
        if (version.indexOf("Firebird") != -1)
            return "Firebird";
        else
            return "Interbase";
    }

    public String getDatabaseProductVersion(){
        return version;
    }

    public int getDatabaseProductMajorVersion(){
        if (version.indexOf("Firebird") != -1){
            if (version.indexOf("Firebird 1.0") != -1)
                return 1;
            else if (version.indexOf("Firebird 1.5") != -1)
                return 1;
            else 
                return -1;
        }
        else
            return -1;
    }

    public int getDatabaseProductMinorVersion(){
        if (version.indexOf("Firebird") != -1){
            if (version.indexOf("Firebird 1.0") != -1)
                return 0;
            else if (version.indexOf("Firebird 1.5") != -1)
                return 5;
            else 
                return -1;
        }
        else
            return -1;
    }

    public void setFBVersion(String value){
        FBVersion = value;
    }

    public String getFBVersion(){
        return FBVersion;
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
}