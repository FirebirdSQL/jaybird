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
 * Backup manager.
 */
public class BackupManager extends ServiceManager {

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
        BACKUP_IGNORE_CHECKSUMS,
        BACKUP_IGNORE_LIMBO,
        BACKUP_METADATA_ONLY,
        BACKUP_NO_GARBAGE_COLLECT,
        BACKUP_OLD_DESCRIPTIONS,
        BACKUP_NON_TRANSPORTABLE,
        BACKUP_CONVERT,
        BACKUP_EXPAND
    };
    
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
            RESTORE_DEACTIVATE_INDEX,
            RESTORE_NO_SHADOW,
            RESTORE_NO_VALIDITY,
            RESTORE_ONE_AT_A_TIME,
            RESTORE_REPLACE,
            RESTORE_CREATE,
            RESTORE_USE_ALL_SPACE
    };
    
    private String backupPath;

    /**
     * Create instance of this class.
     */
    public BackupManager(GDSType gdsType) {
        super(gdsType);
    }

    /**
     * @return Returns the backupPath.
     */
    public String getBackupPath() {
        return backupPath;
    }
    /**
     * @param backupPath The backupPath to set.
     */
    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }
    public void backupDatabase(boolean verbose) throws SQLException, IOException {
        backupDatabase(0, verbose);
    }

    public void backupMetadata(boolean verbose) throws SQLException, IOException {
        backupDatabase(BACKUP_METADATA_ONLY, verbose);
    }

    protected void backupDatabase(int options, boolean verbose) throws SQLException, IOException {
        
        GDS gds = getGds();
        
        try {
            isc_svc_handle handle = attachServiceManager(gds);
    
            try {
                ServiceRequestBuffer backupSRB = getBackupSRB(options, verbose, gds);
                gds.isc_service_start( handle, backupSRB );
                
                queueService(gds, handle);
            } finally {
                detachServiceManager(gds, handle);
            }
        } catch(GDSException ex) {
            throw new FBSQLException(ex);
        }
    }

    private ServiceRequestBuffer getBackupSRB(int options, boolean verbose, GDS gds) {
        ServiceRequestBuffer backupSPB = gds.newServiceRequestBuffer(ISCConstants.isc_action_svc_backup);
   
        backupSPB.addArgument(ISCConstants.isc_spb_dbname, getDatabase());
        backupSPB.addArgument(ISCConstants.isc_spb_bkp_file,  getBackupPath());
        backupSPB.addArgument(ISCConstants.isc_spb_bkp_length, 2048);
   
        if (verbose)
            backupSPB.addArgument(ISCConstants.isc_spb_verbose);
        
        backupSPB.addArgument(ISCConstants.isc_spb_options, options);

        return backupSPB;
    }
    
    public void restoreDatabase(boolean verbose) throws SQLException, IOException {
        restoreDatabase(-1, -1, false, 0, verbose);
    }
    
    protected void restoreDatabase(int buffers, int pageSize,
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
    
    private ServiceRequestBuffer getRestoreSRB(int buffers, int pageSize,
            boolean restoreReadOnly, int options, boolean verbose, GDS gds) {
        ServiceRequestBuffer restoreSPB = gds
                .newServiceRequestBuffer(ISCConstants.isc_action_svc_restore);

        restoreSPB.addArgument(ISCConstants.isc_spb_dbname, getDatabase());
        restoreSPB.addArgument(ISCConstants.isc_spb_bkp_file, getBackupPath());
        
        if (buffers != -1)
            restoreSPB.addArgument(ISCConstants.isc_spb_res_buffers, buffers);
        
        if (pageSize != -1)
            restoreSPB.addArgument(ISCConstants.isc_spb_res_page_size, pageSize);
        
        if (restoreReadOnly)
            restoreSPB.addArgument(ISCConstants.isc_spb_res_access_mode, 
                restoreReadOnly ? ISCConstants.isc_spb_res_am_readonly :
                    ISCConstants.isc_spb_res_am_readwrite);
        
        if (verbose) 
            restoreSPB.addArgument(ISCConstants.isc_spb_verbose);

        restoreSPB.addArgument(ISCConstants.isc_spb_options, options);
        
        return restoreSPB;
    }
}
