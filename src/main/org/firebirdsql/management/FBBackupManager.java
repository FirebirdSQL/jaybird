/*
 * $Id$
 * 
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

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSType;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;

/**
 * Implements the backup and restore functionality of Firebird Services API.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy
 *         </a>
 */
public class FBBackupManager extends FBServiceManager implements BackupManager {

    private String backupPath;

    private boolean verbose;

    private int restoreBufferCount;

    private int restorePageSize;

    private boolean restoreReadOnly;

    private boolean restoreCreate;


    private static final int RESTORE_REPLACE = ISCConstants.isc_spb_res_replace;

    private static final int RESTORE_CREATE = ISCConstants.isc_spb_res_create;


    /**
     * Create instance of this class.
     */
    public FBBackupManager(GDSType gdsType) {
        super(gdsType);

        verbose = false;
        restoreBufferCount = -1;
        restorePageSize = -1;
        restoreReadOnly = false;
        restoreCreate = true;
    }

    /**
     * @see org.firebirdsql.management.BackupManager#getBackupPath()
     */
    public String getBackupPath() {
        return backupPath;
    }

    /**
     * @see org.firebirdsql.management.BackupManager#setBackupPath(java.lang.String)
     */
    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    /**
     * @see org.firebirdsql.management.BackupManager#backupDatabase()
     */
    public void backupDatabase() throws SQLException {
        backupDatabase(0);
    }

    /**
     * @see org.firebirdsql.management.BackupManager#backupMetadata(boolean)
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
    private ServiceRequestBuffer getBackupSRB(int options) {

        ServiceRequestBuffer backupSPB = createRequestBuffer(
                ISCConstants.isc_action_svc_backup, 
                options);

        backupSPB.addArgument(ISCConstants.isc_spb_bkp_file, getBackupPath());
        backupSPB.addArgument(ISCConstants.isc_spb_bkp_length, 2048);

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
     * for <code>pageSize</code> must be one of: 1024, 2048, 4196, or 8192. The
     * default value is 1024.
     *
     * @param pageSize The page size to be used in a restored database,
     *        one of 1024, 2048, 4196 or 8192
     */
    public void setRestorePageSize(int pageSize) {
        if (pageSize != 1024 && pageSize != 2048 
                && pageSize != 4196 && pageSize != 8192){
            throw new IllegalArgumentException(
                    "Page size must be one of 1024, 2048, 4196 or 8192");
        }
        this.restorePageSize = pageSize;
    }

    /**
     * Set the restore operation to create a new database, as opposed to
     * overwriting an existing database. This is true by default.
     *
     * @param create If <code>true</code>, the restore operation will attempt
     *        to create a new database, otherwise the restore operation will
     *        overwrite an existing database
     */
    public void setRestoreCreate(boolean create) {
        this.restoreCreate = create;
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
                .newServiceRequestBuffer(ISCConstants.isc_action_svc_restore);

        // db-name and backup path are reversed for restore operations
        restoreSPB.addArgument(ISCConstants.isc_spb_dbname, getBackupPath());
        restoreSPB.addArgument(ISCConstants.isc_spb_bkp_file, getDatabase());

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
            options |= restoreCreate ? RESTORE_CREATE : RESTORE_REPLACE;
        }
        
        restoreSPB.addArgument(ISCConstants.isc_spb_options, options);

        return restoreSPB;
    }

}
