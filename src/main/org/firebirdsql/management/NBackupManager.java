/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.management;

import java.sql.SQLException;

/**
 * Implements the incremental backup and restore functionality of NBackup
 * via the Firebird Services API.
 *
 * @author <a href="mailto:tsteinmaurer@users.sourceforge.net">Thomas Steinmaurer</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
     * Set the path to the database. This method is used both for backup and
     * restore operation.
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
     *
     * @throws SQLException
     *         if a database error occurs during the restore
     */
    void restoreDatabase() throws SQLException;

    /**
     * Sets the backup level (0 = full, 1..n = incremental)
     *
     * @param level
     *         backup level (e.g. 0 = full backup, 1 = level 1 incremental backup based on level 0 backup
     */
    void setBackupLevel(int level);

    /**
     * Sets the backup GUID (Firebird 4 and higher only).
     * <p>
     * The backup GUID is the GUID of a previous backup of the (source) database. This is used by Firebird to backup
     * the pages modified since that backup.
     * </p>
     * <p>
     * This setting is mutually exclusive with {@link #setBackupLevel(int)}, but this is only checked server-side.
     * </p>
     *
     * @param guid A GUID string of a previous backup, enclosed in braces.
     * @since 4.0.4
     */
    void setBackupGuid(String guid);

    /**
     * Sets the option no database triggers when connecting at backup or in-place restore.
     *
     * @param noDBTriggers {@code true} disable db triggers during backup or in-place restore.
     */
    void setNoDBTriggers(boolean noDBTriggers);

    /**
     * Enables in-place restore.
     *
     * @param inPlaceRestore {@code true} to enable in-place restore
     * @since 4.0.4
     */
    void setInPlaceRestore(boolean inPlaceRestore);
    
    /**
     * Enables clean history on backup.
     * <p>
     * The backup will fail if {@link #setKeepDays(int)} or {@link #setKeepRows(int)} has not been called.
     * </p>
     *
     * @param cleanHistory
     *         {@code true} to enable clean history
     * @since 4.0.7
     */
    void setCleanHistory(boolean cleanHistory);

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
     * @see #setCleanHistory(boolean)
     * @see #setKeepRows(int)
     * @since 4.0.7
     */
    void setKeepDays(int days);

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
     * @see #setCleanHistory(boolean)
     * @see #setKeepDays(int)
     * @since 4.0.7
     */
    void setKeepRows(int rows);

}
