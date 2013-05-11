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
 * Implements the backup and restore functionality of Firebird Services API.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy
 *         </a>
 */
public class FBBackupManager extends FBServiceManager implements BackupManager {

    /**
     * Structure that holds path to the database and corresponding size of the 
     * file (in case of backup - that is size of the file in megabytes, in case
     * of restore - size of the database file in pages).
     */
    private static class PathSizeStruct {
        private int size;
        private String path;
        
        private PathSizeStruct(String path, int size) {
            this.path = path;
            this.size = size;
        }
        
        public String getPath() {
            return path;
        }

        public int getSize() {
            return size;
        }
        
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof PathSizeStruct)) return false;
            
            PathSizeStruct that = (PathSizeStruct)obj;
            
            return this.path.equals(that.path);
        }

        public int hashCode() {
            return path.hashCode();
        }

        public String toString() {
            return path + " " + size;
        }
    }

    private boolean noLimitBackup = false;
    private ArrayList backupPaths = new ArrayList();
    
    private boolean noLimitRestore = false;
    private ArrayList restorePaths = new ArrayList();
    
    private boolean verbose;

    private int restoreBufferCount;

    private int restorePageSize;

    private boolean restoreReadOnly;

    private boolean restoreReplace;


    private static final int RESTORE_REPLACE = ISCConstants.isc_spb_res_replace;

    private static final int RESTORE_CREATE = ISCConstants.isc_spb_res_create;
    
    /**
     * Create a new instance of <code>FBBackupManager</code> based on
     * the default GDSType.
     */
    public FBBackupManager()
    {
    	super();
    	init();
    }

    /**
     * Create a new instance of <code>FBBackupManager</code> based on
     * a given GDSType.
     * 
     * @param gdsType type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBBackupManager(String gdsType)
    {
    	super(gdsType);
    	init();
    }

    /**
     * Create a new instance of <code>FBBackupManager</code> based on
     * a given GDSType.
     * 
     * @param gdsType type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBBackupManager(GDSType gdsType) {
    	super(gdsType);
    	init();
    }
    
    /**
     * Initialize necessary parameters.
     */
    private void init() {
        verbose = false;
        restoreBufferCount = -1;
        restorePageSize = -1;
        restoreReadOnly = false;
        restoreReplace = false;
    }

    /**
     * @see org.firebirdsql.management.BackupManager#setBackupPath(java.lang.String)
     */
    public void setBackupPath(String backupPath) {
        addBackupPath(backupPath, -1);
        noLimitBackup = true;
    }

    public void addBackupPath(String path) {
        addBackupPath(path, -1);
    }
    
    public void addBackupPath(String path, int size) {
        
        if (noLimitBackup)
            throw new IllegalArgumentException(
                "You cannot use setBackupPath(String) and " +
                "addBackupPath(String, int) methods simultaneously.");
        
        backupPaths.add(new PathSizeStruct(path, size));
    }
    
    public void clearBackupPaths() {
        backupPaths.clear();
        noLimitBackup = false;
    }
    
    /* (non-Javadoc)
     * @see org.firebirdsql.management.ServiceManager#setDatabase(java.lang.String)
     */
    public void setDatabase(String database) {
        super.setDatabase(database);
        addRestorePath(database, -1);
        noLimitRestore = true;
    }
    public void addRestorePath(String path, int size) {

        if (noLimitRestore)
            throw new IllegalArgumentException(
                "You cannot use setDatabase(String) and " +
                "addRestorePath(String, int) methods simultaneously.");
        
        restorePaths.add(new PathSizeStruct(path, size));
    }
    
    public void clearRestorePaths() {
        restorePaths.clear();
        noLimitRestore = false;
    }
    
    /**
     * @see org.firebirdsql.management.BackupManager#backupDatabase()
     */
    public void backupDatabase() throws SQLException {
        backupDatabase(0);
    }

    /**
     * @see org.firebirdsql.management.BackupManager#backupMetadata()
     */
    public void backupMetadata() throws SQLException {
        backupDatabase(BACKUP_METADATA_ONLY);
    }

    /**
     * @see org.firebirdsql.management.BackupManager#backupDatabase(int)
     */
    public void backupDatabase(int options ) throws SQLException {
        executeServicesOperation(getBackupSRB(options));
    }

    /**
     * Creates and returns the "backup" service request buffer for the Service
     * Manager.
     * 
     * @param options The isc_spb_bkp_* parameters options to be used
     * @return the "backup" service request buffer for the Service Manager.
     */
    private ServiceRequestBuffer getBackupSRB(int options) throws SQLException {

        ServiceRequestBuffer backupSPB = getGds().createServiceRequestBuffer(
                ISCConstants.isc_action_svc_backup);

        backupSPB.addArgument(ISCConstants.isc_spb_dbname, getDatabase());
        
        for (Iterator iter = backupPaths.iterator(); iter.hasNext();) {
            PathSizeStruct pathSize = (PathSizeStruct) iter.next();
            
            backupSPB.addArgument(ISCConstants.isc_spb_bkp_file, pathSize.getPath());
            
            if (iter.hasNext() && pathSize.getSize() == -1)
                throw new FBSQLException("No size specified for a backup file " + 
                    pathSize.getPath());
            
            if (iter.hasNext())
                backupSPB.addArgument(ISCConstants.isc_spb_bkp_length, pathSize.size);
        }
        
        if (verbose)
            backupSPB.addArgument(ISCConstants.isc_spb_verbose);

        backupSPB.addArgument(ISCConstants.isc_spb_options, options);

        return backupSPB;
    }

    /**
     * @see org.firebirdsql.management.BackupManager#restoreDatabase()
     */
    public void restoreDatabase() throws SQLException {
        restoreDatabase(0);
    }

    /**
     * @see org.firebirdsql.management.BackupManager#restoreDatabase(int)
     */
    public void restoreDatabase(int options) throws SQLException {
        executeServicesOperation(getRestoreSRB(options));
    }

    /**
     * Set whether the operations of this <code>BackupManager</code> will
     * result in verbose logging to the configured logger.
     *
     * @param verbose If <code>true</code>, operations will be logged
     *        verbosely, otherwise they will not be logged verbosely
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
      
    /**
     * Set the default number of pages to be buffered (cached) by default in a 
     * restored database.
     *
     * @param bufferCount The page-buffer size to be used, a positive value
     */
    public void setRestorePageBufferCount(int bufferCount) {
        if (bufferCount < 0){
            throw new IllegalArgumentException("Buffer count must be positive");
        }
        this.restoreBufferCount = bufferCount;
    }

    /**
     * Set the page size that will be used for a restored database. The value 
     * for <code>pageSize</code> must be one of: 1024, 2048, 4096, 8192 or 16384. The
     * default value depends on the Firebird version.
     *
     * @param pageSize The page size to be used in a restored database,
     *        one of 1024, 2048, 4196, 8192 or 16384
     */
    public void setRestorePageSize(int pageSize) {
        if (pageSize != 1024 && pageSize != 2048 
                && pageSize != 4096 && pageSize != 8192 && pageSize != 16384){
            throw new IllegalArgumentException(
                    "Page size must be one of 1024, 2048, 4096, 8192 or 16384");
        }
        this.restorePageSize = pageSize;
    }

    /**
     * Set the restore operation to create a new database, as opposed to
     * overwriting an existing database. This is true by default.
     *
     * @param replace If <code>true</code>, the restore operation will attempt
     *        to create a new database, otherwise the restore operation will
     *        overwrite an existing database
     */
    public void setRestoreReplace(boolean replace) {
        this.restoreReplace = replace;
    }


    /**
     * Set the read-only attribute on a restored database.
     *
     * @param readOnly If <code>true</code>, a restored database will be
     *        read-only, otherwise it will be read-write.
     */
    public void setRestoreReadOnly(boolean readOnly) {
        this.restoreReadOnly = readOnly;
    }


    /**
     * Creates and returns the "backup" service request buffer for the Service
     * Manager.
     * 
     * @param options THe options to be used for the backup operation
     * @return the "backup" service request buffer for the Service Manager.
     */
    private ServiceRequestBuffer getRestoreSRB(int options) {

        GDS gds = getGds();
        ServiceRequestBuffer restoreSPB = gds
                .createServiceRequestBuffer(ISCConstants.isc_action_svc_restore);

        // backup files without sizes
        for (Iterator iter = backupPaths.iterator(); iter.hasNext();) {
            PathSizeStruct pathSize = (PathSizeStruct) iter.next();
            
            restoreSPB.addArgument(ISCConstants.isc_spb_bkp_file, pathSize.getPath());            
        }
        
        // restore files with sizes except the last one
        for (Iterator iter = restorePaths.iterator(); iter.hasNext();) {
            PathSizeStruct pathSize = (PathSizeStruct) iter.next();
            
            restoreSPB.addArgument(ISCConstants.isc_spb_dbname, pathSize.getPath());
            
            if (iter.hasNext() && pathSize.getSize() != -1)
                restoreSPB.addArgument(ISCConstants.isc_spb_res_length, pathSize.getSize());
        }
        
        if (restoreBufferCount != -1)
            restoreSPB.addArgument(ISCConstants.isc_spb_res_buffers, 
                    restoreBufferCount);

        if (restorePageSize != -1)
            restoreSPB.addArgument(ISCConstants.isc_spb_res_page_size, 
                    restorePageSize);

        restoreSPB.addArgument(ISCConstants.isc_spb_res_access_mode,
                    ((byte)(restoreReadOnly 
                        ? ISCConstants.isc_spb_res_am_readonly
                        : ISCConstants.isc_spb_res_am_readwrite)));

        if (verbose)
            restoreSPB.addArgument(ISCConstants.isc_spb_verbose);

        if ((options & RESTORE_CREATE) != RESTORE_CREATE 
                && (options & RESTORE_REPLACE) != RESTORE_REPLACE){
            options |= restoreReplace ? RESTORE_REPLACE : RESTORE_CREATE;
        }
        
        restoreSPB.addArgument(ISCConstants.isc_spb_options, options);

        return restoreSPB;
    }

}
