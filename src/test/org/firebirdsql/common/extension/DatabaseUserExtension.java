// SPDX-FileCopyrightText: Copyright 2015-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.extension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;

/**
 * JUnit extension to create a user and drop it again after completion of the test.
 * <p>
 * NOTE: This extension only works for Firebird versions that support user management through SQL
 * (i.e. Firebird 2.5 and higher).
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
public class DatabaseUserExtension implements AfterEachCallback {

    private static final System.Logger log = System.getLogger(DatabaseUserExtension.class.getName());

    private final List<User> createdUsers = new ArrayList<>();

    private DatabaseUserExtension() {
        // No direct instantiation
    }

    public static DatabaseUserExtension withDatabaseUser() {
        return new DatabaseUserExtension();
    }

    /**
     * Create a database user with the specified username and plugin.
     * <p>
     * For non-null plugins, this method will only work on Firebird 3 or higher.
     * </p>
     *
     * @param username
     *         username
     * @param plugin
     *         the user manager plugin to use, or {@code null} for the default (or Firebird 2.5)
     * @throws SQLException
     *         for errors creating the user
     */
    public void createUser(String username, String password, String plugin) throws SQLException {
        try (var connection = getConnectionViaDriverManager();
             var statement = connection.createStatement()) {
            var createUserSql = new StringBuilder("CREATE USER ").append(statement.enquoteIdentifier(username, false))
                    .append(" PASSWORD ").append(statement.enquoteLiteral(password));
            if (plugin != null) {
                createUserSql.append(" USING PLUGIN ").append(statement.enquoteIdentifier(plugin, false));
            }
            statement.execute(createUserSql.toString());
        }

        createdUsers.add(new User(username, plugin));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (createdUsers.isEmpty()) {
            return;
        }
        try (var connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(false);
            try (var statement = connection.createStatement()) {
                for (User user : createdUsers) {
                    dropUser(statement, user);
                }
            } finally {
                connection.commit();
            }
        } catch (SQLException e) {
            log.log(System.Logger.Level.ERROR, "Can't drop users", e);
        }
    }

    private void dropUser(Statement statement, User user) {
        try {
            var dropUserSql = new StringBuilder("DROP USER ").append(statement.enquoteIdentifier(user.name, false));
            if (user.plugin != null) {
                dropUserSql.append(" USING PLUGIN ").append(statement.enquoteIdentifier(user.plugin, false));
            }
            statement.execute(dropUserSql.toString());
        } catch (SQLException e) {
            log.log(System.Logger.Level.ERROR, "Unable to drop user " + user, e);
        }
    }

    private record User(String name, String plugin) {

        @Override
        public String toString() {
            return plugin == null ? name : name + " (" + plugin + " )";
        }
    }
}
