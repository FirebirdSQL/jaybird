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

import java.io.IOException;
import java.sql.SQLException;

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.GDSType;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.isc_svc_handle;
import org.firebirdsql.jdbc.FBSQLException;

/**
 * Implements the backup and restore functionality of Firebird Services API.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy
 *         </a>
 */
public class FBBackupManager extends FBServiceManager implements BackupManager {

    /**
     * Ignore checksums.
     */
    public static final int BACKUP_IGNORE_CHECKSUMS = ISCConstants.isc_spb_bkp_ignore_checksums;

    /**
     * Ignore in-limbo transactions.
     */
    public static final int BACKUP_IGNORE_LIMBO = ISCConstants.isc_spb_bkp_ignore_limbo;

    /**
     * Backup metadata only.
     */
    public static final int BACKUP_METADATA_ONLY = ISCConstants.isc_spb_bkp_metadata_only;

    /**
     * Do not collect garbage during backup.
     */
    public static final int BACKUP_NO_GARBAGE_COLLECT = ISCConstants.isc_spb_bkp_no_garbage_collect;

    /**
     * Save old style metadata descriptions.
     */
    public static final int BACKUP_OLD_DESCRIPTIONS = ISCConstants.isc_spb_bkp_old_descriptions;

    /**
     * Use non-transportable backup format.
     */
    public static final int BACKUP_NON_TRANSPORTABLE = ISCConstants.isc_spb_bkp_non_transportable;

    /**
     * Backup external files as tables.
     */
    public static final int BACKUP_CONVERT = ISCConstants.isc_spb_bkp_convert;

    /**
     * No data compression.
     */
    public static final int BACKUP_EXPAND = ISCConstants.isc_spb_bkp_expand;

    private static final int[] BACKUP_OPTIONS = new int[] {
            BACKUP_IGNORE_CHECKSUMS, BACKUP_IGNORE_LIMBO, BACKUP_METADATA_ONLY,
            BACKUP_NO_GARBAGE_COLLECT, BACKUP_OLD_DESCRIPTIONS,
            BACKUP_NON_TRANSPORTABLE, BACKUP_CONVERT, BACKUP_EXPAND };

    /**
     * Deactivate indices during restore.
     */
    public static final int RESTORE_DEACTIVATE_INDEX = ISCConstants.isc_spb_res_deactivate_idx;

    /**
     * Do not restore shadow database.
     */
    public static final int RESTORE_NO_SHADOW = ISCConstants.isc_spb_res_no_shadow;

    /**
     * Do not restore validity constraints.
     */
    public static final int RESTORE_NO_VALIDITY = ISCConstants.isc_spb_res_no_validity;

    /**
     * Commit after completing restore of each table.
     */
    public static final int RESTORE_ONE_AT_A_TIME = ISCConstants.isc_spb_res_one_at_a_time;

    /**
     * Replace existing database during restore.
     */
    public static final int RESTORE_REPLACE = ISCConstants.isc_spb_res_replace;

    /**
     * Create a database during restore, but do not replace it if it exists.
     */
    public static final int RESTORE_CREATE = ISCConstants.isc_spb_res_create;

    /**
     * Do not reserve 20% on each page for the future versions, useful for
     * read-only databases.
     */
    public static final int RESTORE_USE_ALL_SPACE = ISCConstants.isc_spb_res_use_all_space;

    private static final int[] RESTORE_OPTIONS = new int[] {
            RESTORE_DEACTIVATE_INDEX, RESTORE_NO_SHADOW, RESTORE_NO_VALIDITY,
            RESTORE_ONE_AT_A_TIME, RESTORE_REPLACE, RESTORE_CREATE,
            RESTORE_USE_ALL_SPACE };

    private String backupPath;

    /**
     * Create instance of this class.
     */
    public FBBackupManager(GDSType gdsType) {
        super(gdsType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.BackupManager#getBackupPath()
     */
    public String getBackupPath() {
        return backupPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.BackupManager#setBackupPath(java.lang.String)
     */
    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.BackupManager#backupDatabase(boolean)
     */
    public void backupDatabase(boolean verbose) throws SQLException,
            IOException {
        backupDatabase(0, verbose);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.BackupManager#backupMetadata(boolean)
     */
    public void backupMetadata(boolean verbose) throws SQLException,
            IOException {
        backupDatabase(BACKUP_METADATA_ONLY, verbose);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.BackupManager#backupDatabase(int,
     *      boolean)
     */
    public void backupDatabase(int options, boolean verbose)
            throws SQLException, IOException {

        GDS gds = getGds();

        try {
            isc_svc_handle handle = attachServiceManager(gds);

            try {
                ServiceRequestBuffer backupSRB = getBackupSRB(options, verbose,
                        gds);
                gds.isc_service_start(handle, backupSRB);

                queueService(gds, handle);
            } finally {
                detachServiceManager(gds, handle);
            }
        } catch (GDSException ex) {
            throw new FBSQLException(ex);
        }
    }

    /**
     * Creates and returns the "backup" service request buffer for the Service
     * Manager.
     * 
     * @param options
     * @param verbose
     * @param gds
     * @return the "backup" service request buffer for the Service Manager.
     */
    private ServiceRequestBuffer getBackupSRB(int options, boolean verbose,
            GDS gds) {
        ServiceRequestBuffer backupSPB = gds
                .newServiceRequestBuffer(ISCConstants.isc_action_svc_backup);

        backupSPB.addArgument(ISCConstants.isc_spb_dbname, getDatabase());
        backupSPB.addArgument(ISCConstants.isc_spb_bkp_file, getBackupPath());
        backupSPB.addArgument(ISCConstants.isc_spb_bkp_length, 2048);

        if (verbose)
            backupSPB.addArgument(ISCConstants.isc_spb_verbose);

        backupSPB.addArgument(ISCConstants.isc_spb_options, options);

        return backupSPB;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.BackupManager#restoreDatabase(boolean)
     */
    public void restoreDatabase(boolean verbose) throws SQLException,
            IOException {
        restoreDatabase(-1, -1, false, 0, verbose);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.management.BackupManager#restoreDatabase(int, int,
     *      boolean, int, boolean)
     */
    public void restoreDatabase(int buffers, int pageSize,
            boolean restoreReadOnly, int options, boolean verbose)
            throws SQLException, IOException {
        GDS gds = getGds();

        try {
            isc_svc_handle handle = attachServiceManager(gds);

            try {
                ServiceRequestBuffer restoreSRB = getRestoreSRB(buffers,
                        pageSize, restoreReadOnly, options, verbose, gds);
                gds.isc_service_start(handle, restoreSRB);

                queueService(gds, handle);
            } finally {
                detachServiceManager(gds, handle);
            }
        } catch (GDSException ex) {
            throw new FBSQLException(ex);
        }
    }

    /**
     * Creates and returns the "backup" service request buffer for the Service
     * Manager.
     * 
     * @param buffers
     * @param pageSize
     * @param restoreReadOnly
     * @param options
     * @param verbose
     * @param gds
     * @return the "backup" service request buffer for the Service Manager.
     */
    private ServiceRequestBuffer getRestoreSRB(int buffers, int pageSize,
            boolean restoreReadOnly, int options, boolean verbose, GDS gds) {
        ServiceRequestBuffer restoreSPB = gds
                .newServiceRequestBuffer(ISCConstants.isc_action_svc_restore);

        restoreSPB.addArgument(ISCConstants.isc_spb_bkp_file, getBackupPath());
        restoreSPB.addArgument(ISCConstants.isc_spb_dbname, getDatabase());

        if (buffers != -1)
            restoreSPB.addArgument(ISCConstants.isc_spb_res_buffers, buffers);

        if (pageSize != -1)
            restoreSPB
                    .addArgument(ISCConstants.isc_spb_res_page_size, pageSize);

        if (restoreReadOnly)
            restoreSPB.addArgument(ISCConstants.isc_spb_res_access_mode,
                    restoreReadOnly ? ISCConstants.isc_spb_res_am_readonly
                            : ISCConstants.isc_spb_res_am_readwrite);

        if (verbose)
            restoreSPB.addArgument(ISCConstants.isc_spb_verbose);

        restoreSPB.addArgument(ISCConstants.isc_spb_options, options);

        return restoreSPB;
    }

}
