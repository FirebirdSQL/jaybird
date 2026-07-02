// SPDX-FileCopyrightText: Copyright 2009 Thomas Steinmaurer
// SPDX-FileCopyrightText: Copyright 2011-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.management;

import org.jspecify.annotations.Nullable;

import java.sql.SQLException;

/**
 * Implements the incremental backup and restore functionality of NBackup
 * via the Firebird Services API.
 *
 * @author Thomas Steinmaurer
 * @author Mark Rotteveel
 */
public interface NBackupManager extends ServiceManager {

    /**
     * Sets the location of the backup file.
     * <p>
     * Warning: this method behaves identical to {@link #addBackupFile(String)}.
     * </p>
     *
     * @param backupFile
     *         the location of the backup file.
     */
    void setBackupFile(String backupFile);

    /**
     * Add additional backup files.
     * <p>
     * Specifying multiple backup files is only valid for restore, for backup only the first file is used.
     * </p>
     * <p>
     * Use {@link #clearBackupFiles()} to clear earlier backup files.
     * </p>
     *
     * @param backupFile
     *         the location of the backup file.
     */
    void addBackupFile(String backupFile);

    /**
     * Clear the information about backup files. This method undoes all
     * parameters set in the {@link #addBackupFile(String)} method.
     */
    void clearBackupFiles();

    /**
     * Set the path to the database. This method is used both for backup and restore operation.
     * <p>
     * NOTE: Contrary to {@link ServiceManager#setDatabase(String)}, {@code path} is {@code @NonNull}.
     * </p>
     *
     * @param path
     *         path to the database file.
     *         <p>
     *         In case of backup, value specifies the path of the existing database on the server that will be
     *         backed up.
     *         </p>
     *         <p>
     *         In case of restore, value specifies the path of the database where the backup will be restored to.
     *         </p>
     */
    void setDatabase(String path);

    /**
     * Perform the backup operation.
     *
     * @throws SQLException
     *         if a database error occurs during the backup
     */
    void backupDatabase() throws SQLException;

    /**
     * Perform the restore operation.
     * <p>
     * Set {@link #setPreserveSequence(boolean)} to preserve the original database GUID and replication sequence.
     * </p>
     *
     * @throws SQLException
     *         if a database error occurs during the restore
     */
    void restoreDatabase() throws SQLException;

    /**
     * Perform the nbackup fixup operation.
     * <p>
     * A fixup will switch a locked database to 'normal' state without merging the delta, so this is a potentially
     * destructive action. The normal use-case of this option is to unlock a copy of a database file where the source
     * database file was locked with {@code nbackup -L} or {@code ALTER DATABASE BEGIN BACKUP}.
     * </p>
     * <p>
     * Set {@link #setPreserveSequence(boolean)} to preserve the original database GUID and replication sequence.
     * </p>
     *
     * @throws SQLException
     *         if a database error occurs during the fixup
     * @since 5
     */
    void fixupDatabase() throws SQLException;

    /**
     * Sets the backup level (0 = full, 1..n = incremental, -1 = not set).
     * <p>
     * This setting is mutually exclusive with {@link #setBackupGuid(String)} (unless set to {@code -1}). Values are not
     * validated client-side, only server-side.
     * </p>
     *
     * @param level
     *         backup level (e.g. 0 = full backup, 1 = level 1 incremental backup based on level 0 backup, etc.); use
     *         {@code -1} to clear
     * @see #getBackupLevel()
     * @see #setBackupGuid(String)
     */
    void setBackupLevel(int level);

    /**
     * @return backup level (e.g. 0 = full backup, 1 = level 1 incremental backup based on level 0 backup, etc.),
     * {@code -1} means the backup level is not set (it will either use {@code 0} or the backup GUID)
     * @see #setBackupLevel(int)
     * @since 7
     */
    int getBackupLevel();

    /**
     * Sets the backup GUID (Firebird 4 and higher only).
     * <p>
     * The backup GUID is the GUID of a previous backup of the (source) database. This is used by Firebird to back up
     * the pages modified since that backup.
     * </p>
     * <p>
     * This setting is mutually exclusive with {@link #setBackupLevel(int)} (unless set to {@code null}). Values are not
     * validated client-side, only server-side.
     * </p>
     *
     * @param guid
     *         GUID string of a previous backup, enclosed in braces
     * @see #getBackupGuid()
     * @see #setBackupLevel(int)
     * @since 4.0.4
     */
    void setBackupGuid(@Nullable String guid);

    /**
     * @return GUID string of a previous backup, enclosed in braces
     * @see #setBackupGuid(String)
     * @since 7
     */
    @Nullable String getBackupGuid();

    /**
     * Sets the option no database triggers when connecting at backup or in-place restore.
     *
     * @param noDBTriggers
     *         {@code true} disable db triggers during backup or in-place restore
     * @see #isNoDBTriggers()
     */
    void setNoDBTriggers(boolean noDBTriggers);

    /**
     * @return {@code true} db triggers during backup or in-place restore are disabled
     * @see #setNoDBTriggers(boolean)
     * @since 7
     */
    boolean isNoDBTriggers();

    /**
     * Enables in-place restore.
     *
     * @param inPlaceRestore
     *         {@code true} to enable in-place restore
     * @see #isInPlaceRestore()
     * @since 4.0.4
     */
    void setInPlaceRestore(boolean inPlaceRestore);

    /**
     * @return {@code true} in-place restore enabled
     * @see #setInPlaceRestore(boolean)
     * @since 7
     */
    boolean isInPlaceRestore();

    /**
     * Enables preserve sequence (for fixup or restore).
     * <p>
     * This preserves the existing GUID and replication sequence of the original database (they are reset otherwise).
     * </p>
     *
     * @param preserveSequence
     *         {@code true} to enable preserve sequence
     * @see #isPreserveSequence()
     * @since 5
     */
    void setPreserveSequence(boolean preserveSequence);

    /**
     * @return {@code true} preserve sequence enabled
     * @see #setPreserveSequence(boolean)
     * @since 7
     */
    boolean isPreserveSequence();

    /**
     * Enables clean history on backup.
     * <p>
     * The backup will fail if {@link #setKeepDays(int)} or {@link #setKeepRows(int)} have not been set.
     * </p>
     *
     * @param cleanHistory
     *         {@code true} to enable clean history
     * @see #isCleanHistory()
     * @see #setKeepDays(int)
     * @see #setKeepRows(int)
     * @since 4.0.7
     */
    void setCleanHistory(boolean cleanHistory);

    /**
     * @return {@code true} clean history enabled
     * @see #setCleanHistory(boolean)
     * @since 7
     */
    boolean isCleanHistory();

    /**
     * Sets the number of days of backup history to keep.
     * <p>
     * Server-side, this option is mutually exclusive with {@link #setKeepRows(int)}, this is not enforced by the Java
     * code.
     * </p>
     * <p>
     * This option only has effect when {@code setCleanHistory(true)} has been called.
     * </p>
     *
     * @param days
     *         number of days to keep history when cleaning, or {@code -1} to clear current value
     * @see #getKeepDays()
     * @see #setCleanHistory(boolean)
     * @see #setKeepRows(int)
     * @since 4.0.7
     */
    void setKeepDays(int days);

    /**
     * @return number of days to keep history when cleaning, or {@code -1} if not set
     * @see #setKeepDays(int)
     * @since 7
     */
    int getKeepDays();

    /**
     * Sets the number of rows of backup history to keep (this includes the row created by the backup).
     * <p>
     * Server-side, this option is mutually exclusive with {@link #setKeepDays(int)}, this is not enforced by the Java
     * code.
     * </p>
     * <p>
     * This option only has effect when {@code setCleanHistory(true)} has been called.
     * </p>
     *
     * @param rows
     *         number of rows to keep history when cleaning, or {@code -1} to clear current value
     * @see #getKeepRows()
     * @see #setCleanHistory(boolean)
     * @see #setKeepDays(int)
     * @since 4.0.7
     */
    void setKeepRows(int rows);

    /**
     * @return number of rows to keep history when cleaning, or {@code -1} if not set
     * @see #setKeepRows(int)
     * @since 7
     */
    int getKeepRows();

}
