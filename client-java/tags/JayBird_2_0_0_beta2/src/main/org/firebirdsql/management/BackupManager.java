/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
 * 
 * Copyright (C) All Rights Reserved.
 * 
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *  
 *   - Redistributions of source code must retain the above copyright 
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above 
 *     copyright notice, this list of conditions and the following 
 *     disclaimer in the documentation and/or other materials provided 
 *     with the distribution.
 *   - Neither the name of the firebird development team nor the names
 *     of its contributors may be used to endorse or promote products 
 *     derived from this software without specific prior written 
 *     permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF 
 * SUCH DAMAGE.
 */
package org.firebirdsql.management;

import java.sql.SQLException;

import org.firebirdsql.gds.ISCConstants;

/**
 * Implements the backup and restore functionality of Firebird Services API.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine</a>
 */
public interface BackupManager extends ServiceManager {

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
     * Do not reserve 20% on each page for the future versions, useful for
     * read-only databases.
     */
    public static final int RESTORE_USE_ALL_SPACE = ISCConstants.isc_spb_res_use_all_space;

    
    /**
     * Sets the location of the backup file. This method is used to set the
     * path to the backup consisting of a single file. It is not possible to
     * add multiple files or specify the max. size of the file using this 
     * method. It is also not possible to call {@link #addBackupPath(String, int)}
     * method after calling this one.
     * 
     * @param backupPath the location of the backup file.
     * 
     * @see #addBackupPath(String, int) for multi-file backups.
     */
    void setBackupPath(String backupPath);
    
    /**
     * Add the file to the backup of the specified size. Firebird allows 
     * splitting the backup into multiple files, limiting the size of the backup
     * file. This can be useful for example for creating a backup on CD or DVD.
     * 
     * @param path path to the backup file.
     * @param size max size of the file in bytes.
     */
    void addBackupPath(String path, int size);
    
    /**
     * Add backup file to the list. This method is used only during restoring 
     * the database to specify multi-file backups. The call is equivalent to 
     * passing the size -1 to {@link #addBackupPath(String, int)} call. 
     * <p>
     * If application invokes backup operation, an error is generated in that 
     * call.
     * 
     * @param path path to the backup file.
     */
    void addBackupPath(String path);
    
    /**
     * Clear the information about backup paths. This method undoes all 
     * parameters set in the {@link #addBackupPath(String, int)} or 
     * {@link #addBackupPath(String)} methods. 
     */
    void clearBackupPaths();
    
    /**
     * Set the path to the database. This method is used both for backup and
     * restore operation.
     * 
     * @param path path to the database file.
     * <p>
     * In case of backup, value specifies the path of the existing database on
     * the server that will be backed up.
     * <p>
     * In case of restore, value specifies the path of the single-file database
     * where the backup will be restored to.
     */
    void setDatabase(String path);
    
    /**
     * Add the file to the multi-file database of the specified size for restore
     * operation. 
     * 
     * @param path path to the backup file.
     * @param size max size of the database file in pages.
     */
    void addRestorePath(String path, int size);
    
    /**
     * Clear the information about restore paths. This method undoes all 
     * parameters set in the {@link #addRestorePath(String, int)} or 
     * {@link #setDatabase(String)} methods. 
     */
    void clearRestorePaths();

    /**
     * Perform the backup operation.
     *
     * @throws SQLException if a database error occurs during the backup
     */
    public void backupDatabase() throws SQLException;

    /**
     * Perform the backup operation, metadata only.
     *
     * @throws SQLException if a database error occurs during the backup
     */
    public void backupMetadata() throws SQLException;

    /**
     * Perform the backup operation.
     *
     * @param options a bitmask combination of the <code>BACKUP_*</code> 
     *        static final fields for the backup operation
     * @throws SQLException if a database error occurs during the backup
     */
    public void backupDatabase(int options) throws SQLException;

    /**
     * Set whether the operations of this <code>BackupManager</code> will
     * result in verbose logging to the configured logger.
     *
     * @param verbose If <code>true</code>, operations will be logged
     *        verbosely, otherwise they will not be logged verbosely
     */
    public void setVerbose(boolean verbose);
      
    /**
     * Set the default number of pages to be buffered (cached) by default in a 
     * restored database.
     *
     * @param bufferCount The page-buffer size to be used, a positive value
     */
    public void setRestorePageBufferCount(int bufferCount);

    /**
     * Set the page size that will be used for a restored database. The value 
     * for <code>pageSize</code> must be one of: 1024, 2048, 4196, or 8192. The
     * default value is 1024.
     *
     * @param pageSize The page size to be used in a restored database, one
     *        of 1024, 2048, 4196, 8192
     */
    public void setRestorePageSize(int pageSize);

    /**
     * Set the restore operation to create a new database, as opposed to
     * overwriting an existing database.
     *
     * @param create If <code>true</code>, the restore operation will attempt
     *        to create a new database if it does not exit or overwrite an 
     *        existing one when it exists, <code>false</code> when restore 
     *        should fail if database already exist (if it doesn't, a database
     *        will be successfully created).
     */
    public void setRestoreReplace(boolean replace);

    /**
     * Set the read-only attribute on a restored database.
     *
     * @param readOnly If <code>true</code>, a restored database will be
     *        read-only, otherwise it will be read-write.
     */
    public void setRestoreReadOnly(boolean readOnly);

    /**
     * Perform the restore operation.
     * @param  verbose output to the logger.
     * @throws SQLException if a database error occurs during the restore
     */
    public void restoreDatabase() throws SQLException;

    /**
     * Perform the restore operation.
     * @param options A bitmask combination of <code>RESTORE_*</code> static
     *        final fields
     * @throws SQLException if a database error occurs during the restore
     */
    public void restoreDatabase(int options) throws SQLException;
}
