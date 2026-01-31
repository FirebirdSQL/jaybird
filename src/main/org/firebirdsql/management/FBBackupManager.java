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
import org.jspecify.annotations.NullMarked;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.*;

/**
 * Implements the backup and restore functionality of Firebird Services API.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
@NullMarked
public class FBBackupManager extends FBBackupManagerBase implements BackupManager {

    private boolean noLimitBackup = false;
    private final List<PathSizeStruct> backupPaths = new ArrayList<>();

    /**
     * Whether backing up will produce verbose output
     */
    protected boolean verboseBackup() {
        return verbose;
    }

    /**
     * Create a new instance of <code>FBBackupManager</code> based on the default GDSType.
     */
    public FBBackupManager() {
    }

    /**
     * Create a new instance of <code>FBBackupManager</code> based on a given GDSType.
     *
     * @param gdsType
     *        type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    @SuppressWarnings("unused")
    public FBBackupManager(String gdsType) {
        super(gdsType);
    }

    /**
     * Create a new instance of <code>FBBackupManager</code> based on a given GDSType.
     *
     * @param gdsType
     *        type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBBackupManager(GDSType gdsType) {
        super(gdsType);
    }

    public void setBackupPath(String backupPath) {
        addBackupPath(backupPath, -1);
        noLimitBackup = true;
    }

    public void addBackupPath(String path, int size) {
        if (noLimitBackup) {
            throw new IllegalArgumentException(
                    "You cannot use setBackupPath(String) and addBackupPath(String, int) methods simultaneously.");
        }
        backupPaths.add(new PathSizeStruct(path, size));
    }

    public void clearBackupPaths() {
        backupPaths.clear();
        noLimitBackup = false;
    }

    public void backupDatabase(int options) throws SQLException {
        try (FbService service = attachServiceManager()) {
            executeServicesOperation(service, getBackupSRB(service, options));
        }
    }

    public void restoreDatabase(int options) throws SQLException {
        try (FbService service = attachServiceManager()) {
            executeServicesOperation(service, getRestoreSRB(service, options));
        }
    }

    /**
     * Adds the currentDatabase as a source for the backup operation
     *
     * @param backupSPB
     *        The buffer to be used during the backup operation
     */
    protected void addBackupsToBackupRequestBuffer(FbService service, ServiceRequestBuffer backupSPB)
            throws SQLException {
        for (Iterator<PathSizeStruct> iter = backupPaths.iterator(); iter.hasNext();) {
            PathSizeStruct pathSize = iter.next();

            backupSPB.addArgument(isc_spb_bkp_file, pathSize.path());

            if (iter.hasNext()) {
                if (pathSize.size() == -1) {
                    throw new SQLException("No size specified for a backup file " + pathSize.path());
                }
                backupSPB.addArgument(isc_spb_bkp_length, pathSize.size());
            }
        }
    }

    /**
     * Adds the list of backups to be used for the restore operation
     *
     * @param restoreSPB
     *        The buffer to be used during the restore operation
     */
    protected void addBackupsToRestoreRequestBuffer(FbService service, ServiceRequestBuffer restoreSPB) {
        for (PathSizeStruct pathSize : backupPaths) {
            restoreSPB.addArgument(isc_spb_bkp_file, pathSize.path());
        }
    }
}
