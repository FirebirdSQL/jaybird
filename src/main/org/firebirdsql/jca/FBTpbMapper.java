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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jdbc.FBConnectionHelper;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This class is provides mapping capabilities between standard JDBC
 * transaction isolation level and Firebird Transaction Parameters Block (TPB).
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBTpbMapper implements Serializable {
    
    public static final String DEFAULT_MAPPING_RESOURCE = "isc_tpb_mapping";
    public static final FBTpbMapper DEFAULT_MAPPER = new FBTpbMapper();
    
    private static final String TRANSACTION_SERIALIZABLE = FBTpb.TRANSACTION_SERIALIZABLE;
    private static final String TRANSACTION_REPEATABLE_READ = FBTpb.TRANSACTION_REPEATABLE_READ;
    private static final String TRANSACTION_READ_COMMITTED = FBTpb.TRANSACTION_READ_COMMITTED;
    private static final String TRANSACTION_READ_UNCOMMITTED = FBTpb.TRANSACTION_READ_UNCOMMITTED;
//    private static final String TRANSACTION_NONE = FBTpb.TRANSACTION_NONE;
    
    private HashMap mapping = new HashMap();
    
    /**
     * Create instance of this class with the default mapping of JDBC
     * transaction isolation levels to Firebird TPB.
     */
    public FBTpbMapper() {
        
        HashSet serializableTpb = new HashSet();
        serializableTpb.add(new Integer(ISCConstants.isc_tpb_write));
        serializableTpb.add(new Integer(ISCConstants.isc_tpb_wait));
        serializableTpb.add(new Integer(ISCConstants.isc_tpb_consistency));
        
        HashSet repeatableReadTpb = new HashSet();
        repeatableReadTpb.add(new Integer(ISCConstants.isc_tpb_write));
        repeatableReadTpb.add(new Integer(ISCConstants.isc_tpb_wait));
        repeatableReadTpb.add(new Integer(ISCConstants.isc_tpb_concurrency));
        
        HashSet readCommittedTpb = new HashSet();
        readCommittedTpb.add(new Integer(ISCConstants.isc_tpb_write));
        readCommittedTpb.add(new Integer(ISCConstants.isc_tpb_wait));
        readCommittedTpb.add(new Integer(ISCConstants.isc_tpb_read_committed));
        readCommittedTpb.add(new Integer(ISCConstants.isc_tpb_rec_version));
        
        mapping.put(new Integer(Connection.TRANSACTION_SERIALIZABLE), serializableTpb);
        mapping.put(new Integer(Connection.TRANSACTION_REPEATABLE_READ), repeatableReadTpb);
        mapping.put(new Integer(Connection.TRANSACTION_READ_COMMITTED), readCommittedTpb);
    }
    
    /**
     * Create instance of this class for the specified string mapping.
     * 
     * @param stringMapping mapping of JDBC transaction isolation to Firebird
     * mapping. Keys and values of this map must be strings. Keys can have 
     * following values:
     * <ul>
     * <li><code>"TRANSACTION_SERIALIZABLE"</code>
     * <li><code>"TRANSACTION_REPEATABLE_READ"</code>
     * <li><code>"TRANSACTION_READ_COMMITTED"</code>
     * <li><code>"TRANSACTION_READ_UNCOMMITTED"</code>
     * </ul>
     * Values are specified as comma-separated list of following keywords:
     * <ul>
     * <li><code>"isc_tpb_consistency"</code>
     * <li><code>"isc_tpb_concurrency"</code>
     * <li><code>"isc_tpb_read_committed"</code>
     * <li><code>"isc_tpb_rec_version"</code>
     * <li><code>"isc_tpb_no_rec_version"</code>
     * <li><code>"isc_tpb_wait"</code>
     * <li><code>"isc_tpb_nowait"</code>
     * <li><code>"isc_tpb_read"</code>
     * <li><code>"isc_tpb_write"</code>
     * <li><code>"isc_tpb_lock_read"</code>
     * <li><code>"isc_tpb_lock_write"</code>
     * <li><code>"isc_tpb_shared"</code>
     * <li><code>"isc_tpb_protected"</code>
     * </ul>
     * It is also allowed to strip "isc_tpb_" prefix from above shown constans.
     * Meaning of these constants and possible combinations you can find in a 
     * documentation.
     * 
     * @throws FBResourceException if mapping contains incorrect values.
     */
    public FBTpbMapper(Map stringMapping) throws FBResourceException {
        this();
        processMapping(stringMapping);
    }
    
    /**
     * Process specified string mapping. This method updates default mapping
     * with values specified in a <code>stringMapping</code>.
     * 
     * @param stringMapping mapping to process.
     * 
     * @throws FBResourceException if mapping contains incorrect values.
     */
    private void processMapping(Map stringMapping) throws FBResourceException {
        
        Iterator iter = stringMapping.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            
            String jdbcTxIsolation = (String)entry.getKey();
            
            if (TRANSACTION_SERIALIZABLE.equalsIgnoreCase(jdbcTxIsolation))
                mapping.put(
                    new Integer(Connection.TRANSACTION_SERIALIZABLE), 
                    processMapping((String)entry.getValue()));
            else
            if (TRANSACTION_REPEATABLE_READ.equalsIgnoreCase(jdbcTxIsolation))
                mapping.put(
                    new Integer(Connection.TRANSACTION_REPEATABLE_READ),
                    processMapping((String)entry.getValue()));
            else
            if (TRANSACTION_READ_COMMITTED.equalsIgnoreCase(jdbcTxIsolation))
                mapping.put(
                    new Integer(Connection.TRANSACTION_READ_COMMITTED),
                    processMapping((String)entry.getValue()));
            else
            if (TRANSACTION_READ_UNCOMMITTED.equalsIgnoreCase(jdbcTxIsolation))
                mapping.put(
                    new Integer(Connection.TRANSACTION_READ_UNCOMMITTED),
                    processMapping((String)entry.getValue()));
            else
                throw new FBResourceException(
                    "Transaction isolation " + jdbcTxIsolation + 
                    " is not supported.");
        }
    }
    
    /**
     * Create instance of this class and load mapping from the specified
     * resource.
     * 
     * @param mappingResource name of the resource to load.
     * @param cl class loader that should be used to load specified resource.
     * 
     * @throws FBResourceException if resource cannot be loaded or contains
     * incorrect values.
     */
    public FBTpbMapper(String mappingResource, ClassLoader cl) throws FBResourceException {
        this();
        try {
            ResourceBundle res = ResourceBundle.getBundle(
                mappingResource, Locale.getDefault(), cl);
                
            HashMap mapping = new HashMap();
            
            Enumeration en = res.getKeys();
            while(en.hasMoreElements()) {
                String key = (String)en.nextElement();
                String value = res.getString(key);
                mapping.put(key, value);
            }
            
            processMapping(mapping);
            
        } catch(MissingResourceException mrex) {
            throw new FBResourceException(
                "Cannot load TPB mapping." + mrex.getMessage());
        }
    }
    
    /**
     * Process comma-separated list of keywords and convert them into TPB 
     * values.
     * 
     * @param mapping comma-separated list of keywords.
     * 
     * @return set containing values corresponding to the specified keywords.
     * 
     * @throws FBResourceException if mapping contains keyword that is not
     * a TPB parameter.
     */
    private Set processMapping(String mapping) throws FBResourceException {
        Set result = new HashSet();
        
        StringTokenizer st = new StringTokenizer(mapping, ",");
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            Integer value = FBConnectionHelper.getTpbParam(token);
            if (value == null)
                throw new FBResourceException(
                    "Keyword " + token + " unknown. Please check your mapping.");
                    
            result.add(value);
        }
        
        return result;
    }
    
    /**
     * Get mapping for the specified transaction isolation level.
     * 
     * @param transactionIsolation transaction isolation level.
     * 
     * @return set with TPB parameters.
     * 
     * @throws FBResourceException if specified transaction isolation level
     * is unknown.
     */
    public Set getMapping(int transactionIsolation) throws FBResourceException {
        
        switch(transactionIsolation) {
            
            case Connection.TRANSACTION_SERIALIZABLE: 
            case Connection.TRANSACTION_REPEATABLE_READ:
            case Connection.TRANSACTION_READ_COMMITTED:
                return new HashSet((Set)mapping.get(
                    new Integer(transactionIsolation)));
                
            // promote transaction 
            case Connection.TRANSACTION_READ_UNCOMMITTED:
                return new HashSet((Set)mapping.get(
                    new Integer(Connection.TRANSACTION_READ_COMMITTED)));
                
            case Connection.TRANSACTION_NONE:
            default:
                throw new FBResourceException(
                    "Transaction isolation level " + transactionIsolation + 
                    " is not supported.");
        }
    }
    
    /**
     * Get default mapping. Default mapping represents a TPB mapping for the
     * default transaction isolation level (read committed).
     * 
     * @return mapping for the default transaction isolation level.
     */
    public Set getDefaultMapping() {
        return (Set)mapping.get(new Integer(Connection.TRANSACTION_READ_COMMITTED));
    }
}