/*
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

/**
 * Implements the incremental backup and restore functionality of NBackup
 * via the Firebird Services API.
 * 
 * @author <a href="mailto:tsteinmaurer@users.sourceforge.net">Thomas Steinmaurer</a>
 */
public interface NBackupManager extends ServiceManager {
	
	/**
     * Sets the location of the backup file.
     * 
     * @param backupFile the location of the backup file.
     * 
     */
    void setBackupFile(String backupFile);
    
    /**
     * Add the file to the backup of the specified size. Firebird allows 
     * splitting the backup into multiple files, limiting the size of the backup
     * file. This can be useful for example for creating a backup on CD or DVD.
     * 
     * @param backupFile the location of the backup file.
     */
    void addBackupFile(String backupFile);

    /**
     * Clear the information about backup files. This method undoes all 
     * parameters set in the {@link #addBackupFile(String} method. 
     */
    void clearBackupFiles();

    /**
     * Set the path to the database. This method is used both for backup and
     * restore operation.
     * 
     * @param path path to the database file.
     * <p>
     * In case of backup, value specifies the path of the existing database on
     * the server that will be backed up.
     * <p>
     * In case of restore, value specifies the path of the database where the
     * backup will be restored to.
     */
    void setDatabase(String path);
    
    /**
     * Perform the backup operation.
     *
     * @throws SQLException if a database error occurs during the backup
     */
    public void backupDatabase() throws SQLException;

    /**
     * Perform the restore operation.
     * @throws SQLException if a database error occurs during the restore
     */
    public void restoreDatabase() throws SQLException;
    
    /**
     * Sets the backup level (0 = full, 1..n = incremental)
     * 
     * @param level backup level (e.g. 0 = full backup, 1 = level 1 incremental
     * backup based on level 0 backup
     */
    public void setBackupLevel(int level);
    
    /**
     * Sets the backup option no database triggers when connecting
     * at backup.
     * 
     * @param noDBTriggers
     */
    public void setNoDBTriggers(boolean noDBTriggers);
}
