// SPDX-FileCopyrightText: Copyright 2004 Steven Jardine
// SPDX-FileCopyrightText: Copyright 2009 Thomas Steinmaurer
// SPDX-FileCopyrightText: Copyright 2011-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.management;

import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * The base Firebird Service API functionality.
 *
 * @author Steven Jardine
 * @deprecated Use the SQL user management statements instead, we currently do not plan to remove this API
 */
@Deprecated(since = "6")
@SuppressWarnings("DeprecatedIsStillUsed")
@NullMarked
public interface UserManager extends ServiceManager {

    /**
     * Add a user to the Firebird Security Database.
     *
     * @param user
     *         to the Firebird Security Database.
     */
    void add(User user) throws SQLException, IOException;

    /**
     * Delete a user to the Firebird Security Database.
     *
     * @param user
     *         to the Firebird Security Database.
     */
    void delete(User user) throws SQLException, IOException;

    /**
     * Update a user to the Firebird Security Database.
     *
     * @param user
     *         to the Firebird Security Database.
     */
    void update(User user) throws SQLException, IOException;

    /**
     * Return all valid users in the Firebird Security Database.
     *
     * @return all valid users in the Firebird Security Database.
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
