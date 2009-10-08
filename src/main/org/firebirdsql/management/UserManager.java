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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * The base Firebird Service API functionality.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
 */
public interface UserManager extends ServiceManager {
	
    /**
     * Add a user to the Firebird Security Database.
     * 
     * @param user to the Firebird Security Database.
     * @throws SQLException
     * @throws IOException
     */
    public void add(User user) throws SQLException, IOException;

    /**
     * Delete a user to the Firebird Security Database.
     * 
     * @param user to the Firebird Security Database.
     * @throws SQLException
     * @throws IOException
     */
    public void delete(User user) throws SQLException, IOException;

    /**
     * Update a user to the Firebird Security Database.
     * 
     * @param user to the Firebird Security Database.
     * @throws SQLException
     * @throws IOException
     */
    public void update(User user) throws SQLException, IOException;

    /**
     * Return all valid users in the Firebird Security Database.
     * 
     * @return all valid users in the Firebird Security Database.
     * @throws SQLException
     * @throws IOException
     */
    public Map getUsers() throws SQLException, IOException;
    
	/**
	 * Sets the security database and therefore overrides the
	 * per default used security database (e.g. security2.fdb)
	 * 
	 * Supported since Firebird 2.1
	 * 
	 * @param name/path of securityDatabase
	 */
    public void setSecurityDatabase(String securityDatabase);
    
    /**
     * Sets AUTO ADMIN MAPPING for role RDB$ADMIN in security database
     */
    public void setAdminRoleMapping() throws SQLException, IOException;
    
    /**
     * Drops AUTO ADMIN MAPPING from role RDB$ADMIN in security database
     */
    public void dropAdminRoleMapping() throws SQLException, IOException;
}
