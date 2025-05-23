// SPDX-FileCopyrightText: Copyright 2009 Thomas Steinmaurer
// SPDX-FileCopyrightText: Copyright 2012-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.management;

import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.jaybird.fb.constants.SpbItems.isc_spb_dbname;
import static org.firebirdsql.jaybird.fb.constants.SpbItems.isc_spb_options;

/**
 * Implements the incremental backup and restore functionality of NBackup via the Firebird Services API.
 *
 * @author Thomas Steinmaurer
 * @author Mark Rotteveel
 */
public class FBNBackupManager extends FBServiceManager implements NBackupManager {

    private final List<String> backupFiles = new ArrayList<>();

    private int backupLevel = -1;
    private String backupGuid;
    private boolean noDBTriggers;
    private boolean inPlaceRestore;
    private boolean preserveSequence;
    private boolean cleanHistory;
    private int keepDays = -1;
    private int keepRows = -1;

    /**
     * Create a new instance of {@code FBNBackupManager} based on the default GDSType.
     */
    @SuppressWarnings("unused")
    public FBNBackupManager() {
    }

    /**
     * Create a new instance of {@code FBNBackupManager} based on a given GDSType name.
     *
     * @param gdsType
     *         type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    @SuppressWarnings("unused")
    public FBNBackupManager(String gdsType) {
        super(gdsType);
    }

    /**
     * Create a new instance of {@code FBNBackupManager} based on a given GDSType.
     *
     * @param gdsType
     *         type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBNBackupManager(GDSType gdsType) {
        super(gdsType);
    }

    @Override
    public void setBackupFile(String backupFile) {
        addBackupFile(backupFile);
    }

    @Override
    public void addBackupFile(String backupFile) {
        backupFiles.add(backupFile);
    }

    @Override
    public void clearBackupFiles() {
        backupFiles.clear();
    }

    @Override
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
        if (backupFiles.isEmpty()) {
            throw new SQLException("No backup file specified");
        }
        String backupFile = backupFiles.get(0);

        backupSPB.addArgument(isc_spb_nbk_file, backupFile);
        // Previously, the default level was 0, retain that default if no backup GUID has been set
        int resolvedBackupLevel = backupLevel == -1 && backupGuid == null ? 0 : backupLevel;
        backupSPB.addArgument(isc_spb_nbk_level, resolvedBackupLevel);
        if (backupGuid != null) {
            backupSPB.addArgument(isc_spb_nbk_guid, backupGuid);
        }
        int options = getOptions();
        if (options != 0) {
            backupSPB.addArgument(isc_spb_options, options);
        }
        if (cleanHistory) {
            backupSPB.addArgument(isc_spb_nbk_clean_history);
            // NOTE: The keepXXX are mutually exclusive, but we leave it to the server to enforce that
            if (keepDays != -1) {
                backupSPB.addArgument(isc_spb_nbk_keep_days, keepDays);
            }
            if (keepRows != -1) {
                backupSPB.addArgument(isc_spb_nbk_keep_rows, keepRows);
            }
        }

        return backupSPB;
    }

    /**
     * @see org.firebirdsql.management.NBackupManager#restoreDatabase()
     */
    @Override
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

        if (backupFiles.isEmpty()) {
            throw new SQLException("No backup file specified");
        }
        for (String backupFile : backupFiles) {
            restoreSPB.addArgument(isc_spb_nbk_file, backupFile);
        }
        int options = getOptions();
        if (options != 0) {
            restoreSPB.addArgument(isc_spb_options, options);
        }

        return restoreSPB;
    }

    @Override
    public void fixupDatabase() throws SQLException {
        try (FbService service = attachServiceManager()) {
            executeServicesOperation(service, getFixupSRB(service));
        }
    }

    private ServiceRequestBuffer getFixupSRB(FbService service) {
        ServiceRequestBuffer restoreSPB = service.createServiceRequestBuffer();
        restoreSPB.addArgument(isc_action_svc_nfix);
        restoreSPB.addArgument(isc_spb_dbname, getDatabase());
        int options = getOptions();
        if (options != 0) {
            restoreSPB.addArgument(isc_spb_options, options);
        }
        return restoreSPB;
    }

    private int getOptions() {
        int options = 0;
        if (noDBTriggers) {
            options |= isc_spb_nbk_no_triggers;
        }
        if (inPlaceRestore) {
            options |= isc_spb_nbk_inplace;
        }
        if (preserveSequence) {
            options |= isc_spb_nbk_sequence;
        }
        return options;
    }

    @Override
    public void setBackupLevel(int backupLevel) {
        this.backupLevel = backupLevel;
    }

    @Override
    public void setBackupGuid(String guid) {
        backupGuid = guid;
    }

    @Override
    public void setNoDBTriggers(boolean noDBTriggers) {
        this.noDBTriggers = noDBTriggers;
    }

    @Override
    public void setInPlaceRestore(boolean inPlaceRestore) {
        this.inPlaceRestore = inPlaceRestore;
    }

    @Override
    public void setPreserveSequence(boolean preserveSequence) {
        this.preserveSequence = preserveSequence;
    }

    @Override
    public void setCleanHistory(boolean cleanHistory) {
        this.cleanHistory = cleanHistory;
    }

    @Override
    public void setKeepDays(int days) {
        keepDays = days;
    }

    @Override
    public void setKeepRows(int rows) {
        keepRows = rows;
    }

}
