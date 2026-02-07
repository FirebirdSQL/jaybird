/*
 SPDX-FileCopyrightText: Copyright 2004-2008 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2004-2005 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
 SPDX-FileCopyrightText: Copyright 2016 Ivan Arabadzhiev
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.management;

import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbService;
import org.firebirdsql.jaybird.fb.constants.SpbItems;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;

/**
 * Implements the common functionality between regular and streaming backup/restore
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
public abstract class FBBackupManagerBase extends FBServiceManager implements BackupManager {

    /**
     * Structure that holds path to the database and corresponding size of the file (in case of backup - that is
     * size of the file in megabytes, in case of restore - size of the database file in pages).
     */
    protected record PathSizeStruct(String path, int size) {

        protected PathSizeStruct {
            requireNonNull(path, "path");
            if (size < -1 || size == 0) {
                throw new IllegalArgumentException("size can only be -1 or a positive integer, value was " + size);
            }
        }

        /**
         * @deprecated use {@link #path()}; this method may be removed in Jaybird 8 or later
         */
        @Deprecated(since = "7", forRemoval = true)
        public String getPath() {
            return path;
        }

        /**
         * @deprecated use {@link #size()}; this method may be removed in Jaybird 8 or later
         */
        public int getSize() {
            return size;
        }

        public boolean equals(@Nullable Object obj) {
            if (obj == this) return true;
            return obj instanceof PathSizeStruct that && this.path.equals(that.path);
        }

        public int hashCode() {
            return path.hashCode();
        }

        public String toString() {
            return path + " " + size;
        }
    }

    protected boolean noLimitRestore;
    protected final List<PathSizeStruct> restorePaths = new ArrayList<>();

    protected boolean verbose;

    private int restoreBufferCount = -1;
    private int restorePageSize = -1;
    private boolean restoreReadOnly;
    private boolean restoreReplace;

    private static final int RESTORE_REPLACE = isc_spb_res_replace;
    private static final int RESTORE_CREATE = isc_spb_res_create;

    /**
     * Create a new instance of <code>FBBackupManagerBase</code> based on the default GDSType.
     */
    protected FBBackupManagerBase() {
    }

    /**
     * Create a new instance of <code>FBBackupManagerBase</code> based on a given GDSType.
     *
     * @param gdsType
     *         type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    protected FBBackupManagerBase(String gdsType) {
        super(gdsType);
    }

    /**
     * Create a new instance of <code>FBBackupManagerBase</code> based on a given GDSType.
     *
     * @param gdsType
     *         type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    protected FBBackupManagerBase(GDSType gdsType) {
        super(gdsType);
    }

    @Override
    public void addBackupPath(String path) {
        addBackupPath(path, -1);
    }

    @Override
    public void setDatabase(String database) {
        super.setDatabase(requireNonNull(database, "database"));
        addRestorePath(database, -1);
        noLimitRestore = true;
    }

    @Override
    public void addRestorePath(String path, int size) {
        if (noLimitRestore) {
            throw new IllegalArgumentException(
                    "You cannot use setDatabase(String) and addRestorePath(String, int) methods simultaneously.");
        }
        restorePaths.add(new PathSizeStruct(path, size));
    }

    @Override
    public void clearRestorePaths() {
        restorePaths.clear();
        noLimitRestore = false;
    }

    @Override
    public void backupDatabase() throws SQLException {
        backupDatabase(0);
    }

    @Override
    public void backupMetadata() throws SQLException {
        backupDatabase(BACKUP_METADATA_ONLY);
    }

    /**
     * Creates and returns the "backup" service request buffer for the Service Manager.
     *
     * @param service
     *         Service handle
     * @param options
     *         The isc_spb_bkp_* parameters options to be used
     * @return the "backup" service request buffer for the Service Manager.
     */
    protected ServiceRequestBuffer getBackupSRB(FbService service, int options) throws SQLException {
        ServiceRequestBuffer backupSPB = service.createServiceRequestBuffer();
        backupSPB.addArgument(isc_action_svc_backup);
        backupSPB.addArgument(SpbItems.isc_spb_dbname, requireDatabase());
        addBackupsToBackupRequestBuffer(service, backupSPB);

        if (verboseBackup()) {
            backupSPB.addArgument(SpbItems.isc_spb_verbose);
        }

        if (getParallelWorkers() > 0 && supportInfoFor(service).supportsParallelWorkers())  {
            backupSPB.addArgument(isc_spb_bkp_parallel_workers, getParallelWorkers());
        }

        backupSPB.addArgument(SpbItems.isc_spb_options, options);

        return backupSPB;
    }

    @Override
    public void restoreDatabase() throws SQLException {
        restoreDatabase(0);
    }

    /**
     * Set whether the operations of this {@code BackupManager} will result in verbose logging to the configured logger.
     *
     * @param verbose
     *         If <code>true</code>, operations will be logged verbosely, otherwise they will not be logged verbosely
     */
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Set the default number of pages to be buffered (cached) by default in a restored database.
     *
     * @param bufferCount
     *         The page-buffer size to be used, a positive value
     */
    @Override
    public void setRestorePageBufferCount(int bufferCount) {
        if (bufferCount < 0) {
            throw new IllegalArgumentException("Buffer count must be positive");
        }
        this.restoreBufferCount = bufferCount;
    }

    /**
     * Set the page size that will be used for a restored database. The value for {@code pageSize} must be
     * one of {@link PageSizeConstants}. The default value depends on the Firebird version.
     * <p>
     * Be aware that not all page sizes are supported by all Firebird versions.
     * </p>
     *
     * @param pageSize
     *         The page size to be used in a restored database, see {@link PageSizeConstants}
     * @see PageSizeConstants
     */
    @Override
    public void setRestorePageSize(int pageSize) {
        this.restorePageSize = PageSizeConstants.requireValidPageSize(pageSize);
    }

    /**
     * Set the restore operation to create a new database, as opposed to overwriting an existing database. This is true
     * by default.
     *
     * @param replace
     *         If <code>true</code>, the restore operation will attempt to create a new database, otherwise the restore
     *         operation will overwrite an existing database
     */
    @Override
    public void setRestoreReplace(boolean replace) {
        this.restoreReplace = replace;
    }

    /**
     * Set the read-only attribute on a restored database.
     *
     * @param readOnly
     *         If <code>true</code>, a restored database will be read-only, otherwise it will be read-write.
     */
    @Override
    public void setRestoreReadOnly(boolean readOnly) {
        this.restoreReadOnly = readOnly;
    }

    /**
     * Creates and returns the "restore" service request buffer for the Service Manager.
     *
     * @param service
     *         Service handle
     * @param options
     *         The options to be used for the restore operation
     * @return the "restore" service request buffer for the Service Manager.
     */
    protected ServiceRequestBuffer getRestoreSRB(FbService service, int options) throws SQLException {
        ServiceRequestBuffer restoreSPB = service.createServiceRequestBuffer();
        restoreSPB.addArgument(isc_action_svc_restore);

        // restore files with sizes except the last one
        for (Iterator<PathSizeStruct> iter = restorePaths.iterator(); iter.hasNext(); ) {
            PathSizeStruct pathSize = iter.next();

            restoreSPB.addArgument(SpbItems.isc_spb_dbname, pathSize.path());

            if (iter.hasNext()) {
                if (pathSize.size() == -1) {
                    throw new SQLException("No size specified for a restore file " + pathSize.path());
                }
                restoreSPB.addArgument(isc_spb_res_length, pathSize.size());
            }
        }

        addBackupsToRestoreRequestBuffer(service, restoreSPB);

        if (restoreBufferCount != -1) {
            restoreSPB.addArgument(isc_spb_res_buffers, restoreBufferCount);
        }

        if (restorePageSize != -1) {
            restoreSPB.addArgument(isc_spb_res_page_size, restorePageSize);
        }

        restoreSPB.addArgument(isc_spb_res_access_mode,
                (byte) (restoreReadOnly
                        ? isc_spb_res_am_readonly
                        : isc_spb_res_am_readwrite));

        if (verbose) {
            restoreSPB.addArgument(SpbItems.isc_spb_verbose);
        }

        if (getParallelWorkers() > 0 && supportInfoFor(service).supportsParallelWorkers())  {
            restoreSPB.addArgument(isc_spb_res_parallel_workers, getParallelWorkers());
        }

        if ((options & RESTORE_CREATE) != RESTORE_CREATE
                && (options & RESTORE_REPLACE) != RESTORE_REPLACE) {
            options |= restoreReplace
                    ? RESTORE_REPLACE
                    : RESTORE_CREATE;
        }

        restoreSPB.addArgument(SpbItems.isc_spb_options, options);

        return restoreSPB;
    }

    /**
     * Adds the backup source for the backup operation, depending on the manager used
     *
     * @param backupSPB
     *         The buffer to be used during the backup operation
     */
    protected abstract void addBackupsToBackupRequestBuffer(FbService service, ServiceRequestBuffer backupSPB)
            throws SQLException;

    /**
     * Adds the backup files to be used during restore
     */
    protected abstract void addBackupsToRestoreRequestBuffer(FbService service, ServiceRequestBuffer restoreSPB);

    /**
     * Whether the backup will produce verbose output
     */
    protected abstract boolean verboseBackup();

}
