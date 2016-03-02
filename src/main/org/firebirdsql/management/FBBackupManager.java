/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.management;

import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.*;

/**
 * Implements the backup and restore functionality of Firebird Services API.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBBackupManager extends FBBackupManagerBase implements BackupManager {

    private boolean noLimitBackup = false;
    private List<PathSizeStruct> backupPaths = new ArrayList<>();

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

            backupSPB.addArgument(isc_spb_bkp_file, pathSize.getPath(), service.getEncoding());

            if (iter.hasNext() && pathSize.getSize() == -1) {
                throw new SQLException("No size specified for a backup file " + pathSize.getPath());
            }

            if (iter.hasNext()) {
                backupSPB.addArgument(isc_spb_bkp_length, pathSize.getSize());
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
            restoreSPB.addArgument(isc_spb_bkp_file, pathSize.getPath(), service.getEncoding());
        }
    }
}
