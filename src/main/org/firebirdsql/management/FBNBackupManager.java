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
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.*;

/**
 * Implements the incremental backup and restore functionality of NBackup via the Firebird Services API.
 *
 * @author <a href="mailto:tsteinmaurer@users.sourceforge.net">Thomas Steinmaurer</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBNBackupManager extends FBServiceManager implements NBackupManager {

    private final List<String> backupFiles = new ArrayList<>();

    private int backupLevel;
    private boolean noDBTriggers;

    /**
     * Create a new instance of <code>FBNBackupManager</code> based on the default GDSType.
     */
    public FBNBackupManager() {
    }

    /**
     * Create a new instance of <code>FBNBackupManager</code> based on a given GDSType.
     *
     * @param gdsType
     *         type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBNBackupManager(String gdsType) {
        super(gdsType);
    }

    /**
     * Create a new instance of <code>FBNBackupManager</code> based on a given GDSType.
     *
     * @param gdsType
     *         type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBNBackupManager(GDSType gdsType) {
        super(gdsType);
    }

    public void setBackupFile(String backupFile) {
        addBackupFile(backupFile);
    }

    public void addBackupFile(String backupFile) {
        backupFiles.add(backupFile);
    }

    public void clearBackupFiles() {
        backupFiles.clear();
    }

    public void setDatabase(String database) {
        super.setDatabase(database);
    }

    public void backupDatabase() throws SQLException {
        try (FbService service = attachServiceManager()) {
            executeServicesOperation(service, getBackupSRB(service));
        }
    }

    /**
     * Creates and returns the "backup" service request buffer for the Service Manager.
     *
     * @param service
     *         Service handle
     * @return the "backup" service request buffer for the Service Manager.
     */
    private ServiceRequestBuffer getBackupSRB(FbService service) throws SQLException {
        ServiceRequestBuffer backupSPB = service.createServiceRequestBuffer();
        backupSPB.addArgument(isc_action_svc_nbak);
        backupSPB.addArgument(isc_spb_dbname, getDatabase());
        if (backupFiles.size() == 0) {
            throw new SQLException("No backup file specified");
        }
        String backupFile = backupFiles.get(0);

        backupSPB.addArgument(isc_spb_nbk_file, backupFile);
        backupSPB.addArgument(isc_spb_nbk_level, backupLevel);
        if (noDBTriggers) {
            backupSPB.addArgument(isc_spb_options, isc_spb_nbk_no_triggers);
        }

        return backupSPB;
    }

    /**
     * @see org.firebirdsql.management.NBackupManager#restoreDatabase()
     */
    public void restoreDatabase() throws SQLException {
        try (FbService service = attachServiceManager()) {
            executeServicesOperation(service, getRestoreSRB(service));
        }
    }

    /**
     * Creates and returns the "restore" service request buffer for the Service Manager.
     *
     * @return the "restore" service request buffer for the Service Manager.
     */
    private ServiceRequestBuffer getRestoreSRB(FbService service) throws SQLException {
        ServiceRequestBuffer restoreSPB = service.createServiceRequestBuffer();
        restoreSPB.addArgument(isc_action_svc_nrest);
        restoreSPB.addArgument(isc_spb_dbname, getDatabase());

        if (backupFiles.size() == 0) {
            throw new SQLException("No backup file specified");
        }
        for (String backupFile : backupFiles) {
            restoreSPB.addArgument(isc_spb_nbk_file, backupFile);
        }

        return restoreSPB;
    }

    public void setBackupLevel(int backupLevel) {
        this.backupLevel = backupLevel;
    }

    public void setNoDBTriggers(boolean noDBTriggers) {
        this.noDBTriggers = noDBTriggers;
    }

}
