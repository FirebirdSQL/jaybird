/*
 * Firebird Open Source J2EE Connector - JDBC Driver
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jdbc.FBSQLException;

/**
 * Implements the incremental backup and restore functionality of NBackup
 * via the Firebird Services API.
 * 
 * @author <a href="mailto:tsteinmaurer@users.sourceforge.net">Thomas Steinmaurer</a>
 */
public class FBNBackupManager extends FBServiceManager implements NBackupManager {

    private ArrayList backupFiles = new ArrayList();
    
    private int backupLevel = 0;
    private boolean noDBTriggers = false;
    
    /**
     * Create a new instance of <code>FBNBackupManager</code> based on
     * the default GDSType.
     */
    public FBNBackupManager()
    {
    	super();
    }

    /**
     * Create a new instance of <code>FBNBackupManager</code> based on
     * a given GDSType.
     * 
     * @param gdsType type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBNBackupManager(String gdsType)
    {
    	super(gdsType);
    }

    /**
     * Create a new instance of <code>FBNBackupManager</code> based on
     * a given GDSType.
     * 
     * @param gdsType type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBNBackupManager(GDSType gdsType) {
    	super(gdsType);
    }
    
    /**
     * @see org.firebirdsql.management.NBackupManager#setBackupFile(java.lang.String)
     */
    public void setBackupFile(String backupFile) {
        addBackupFile(backupFile);
    }

    /**
     * @see org.firebirdsql.management.NBackupManager#addBackupFile(java.lang.String)
     */
    public void addBackupFile(String backupFile) {
        this.backupFiles.add(backupFile);
    }
    
    /**
     * @see org.firebirdsql.management.NBackupManager#clearBackupFiles()
     */
    public void clearBackupFiles() {
        this.backupFiles.clear();
    }
    
    /**
     * @see org.firebirdsql.management.ServiceManager#setDatabase(java.lang.String)
     */
    public void setDatabase(String database) {
        super.setDatabase(database);
    }
    
    /**
     * @see org.firebirdsql.management.NBackupManager#backupDatabase()
     */
    public void backupDatabase() throws SQLException {
        executeServicesOperation(getBackupSRB());
    }

    /**
     * Creates and returns the "backup" service request buffer for the Service
     * Manager.
     * 
     * @return the "backup" service request buffer for the Service Manager.
     */
    private ServiceRequestBuffer getBackupSRB() throws SQLException {

        ServiceRequestBuffer backupSPB = getGds().createServiceRequestBuffer(
                ISCConstants.isc_action_svc_nbak);

        backupSPB.addArgument(ISCConstants.isc_spb_dbname, getDatabase());
        Iterator it = this.backupFiles.iterator();
        if (it.hasNext()) {
            String backupFile = (String) it.next();
            
            backupSPB.addArgument(ISCConstants.isc_spb_nbk_file, backupFile);
            backupSPB.addArgument(ISCConstants.isc_spb_nbk_level, this.backupLevel);
            if (this.noDBTriggers)
            	backupSPB.addArgument(ISCConstants.isc_spb_options, ISCConstants.isc_spb_nbk_no_triggers);
        } else {
        	throw new FBSQLException("No backup file specified");
        }
        
        return backupSPB;
    }

    /**
     * @see org.firebirdsql.management.NBackupManager#restoreDatabase()
     */
    public void restoreDatabase() throws SQLException {
        executeServicesOperation(getRestoreSRB());
    }

    /**
     * Creates and returns the "restore" service request buffer for the Service
     * Manager.
     * 
     * @return the "restore" service request buffer for the Service Manager.
     */
    private ServiceRequestBuffer getRestoreSRB() throws SQLException {

        GDS gds = getGds();
        ServiceRequestBuffer restoreSPB = gds
                .createServiceRequestBuffer(ISCConstants.isc_action_svc_nrest);

        restoreSPB.addArgument(ISCConstants.isc_spb_dbname, getDatabase());
        
        Iterator it = this.backupFiles.iterator();
        if (it.hasNext()) {
        	while (it.hasNext()) {
                String backupFile = (String) it.next();
                restoreSPB.addArgument(ISCConstants.isc_spb_nbk_file, backupFile);
        	}
        } else {
        	throw new FBSQLException("No backup file specified");
        }

        return restoreSPB;
    }
    
    /**
     * @see org.firebirdsql.management.NBackupManager#setBackupLevel(int)
     */
    public void setBackupLevel(int backupLevel) {
    	this.backupLevel = backupLevel;
    }
    
    /**
     * @see org.firebirdsql.management.NBackupManager#setNoDBTriggers(boolean)
     */
    public void setNoDBTriggers(boolean noDBTriggers) {
    	this.noDBTriggers = noDBTriggers;
    }

}
