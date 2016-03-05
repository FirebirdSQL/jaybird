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
     * @param user
     *         to the Firebird Security Database.
     * @throws SQLException
     * @throws IOException
     */
    void add(User user) throws SQLException, IOException;

    /**
     * Delete a user to the Firebird Security Database.
     *
     * @param user
     *         to the Firebird Security Database.
     * @throws SQLException
     * @throws IOException
     */
    void delete(User user) throws SQLException, IOException;

    /**
     * Update a user to the Firebird Security Database.
     *
     * @param user
     *         to the Firebird Security Database.
     * @throws SQLException
     * @throws IOException
     */
    void update(User user) throws SQLException, IOException;

    /**
     * Return all valid users in the Firebird Security Database.
     *
     * @return all valid users in the Firebird Security Database.
     * @throws SQLException
     * @throws IOException
     */
    Map<String, User> getUsers() throws SQLException, IOException;

    /**
     * Sets the security database and therefore overrides the
     * per default used security database (e.g. security2.fdb)
     * <p/>
     * Supported since Firebird 2.1
     *
     * @param securityDatabase
     *         name/path of securityDatabase
     */
    void setSecurityDatabase(String securityDatabase);

    /**
     * Sets AUTO ADMIN MAPPING for role RDB$ADMIN in security database
     */
    void setAdminRoleMapping() throws SQLException, IOException;

    /**
     * Drops AUTO ADMIN MAPPING from role RDB$ADMIN in security database
     */
    void dropAdminRoleMapping() throws SQLException, IOException;
}
