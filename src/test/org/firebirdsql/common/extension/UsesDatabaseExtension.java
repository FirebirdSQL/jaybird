/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.common.extension;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.management.FBManager;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.createFBManager;
import static org.firebirdsql.common.FBTestProperties.defaultDatabaseSetUp;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDatabasePath;

/**
 * JUnit 5 extension that creates and deletes a database.
 * <p>
 * This is similar to the JUnit 4 rule {@link org.firebirdsql.common.rules.UsesDatabase}.
 * </p>
 * <p>
 * When used with {@code @ExtendWith}, a default database is created. For more control,
 * use {@code @RegisterExtension}, the static factory methods can be used for configuration.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public class UsesDatabaseExtension implements BeforeEachCallback, AfterEachCallback {

    private final boolean initialCreate;
    private FBManager fbManager = null;
    private final List<String> initStatements = new ArrayList<>();
    private final List<String> databasesToDrop = new ArrayList<>();

    public UsesDatabaseExtension() {
        this(true);
    }

    private UsesDatabaseExtension(boolean initialCreate) {
        this.initialCreate = initialCreate;
    }

    // NOTE: Can be called with context == null (from UsesDatabase)
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        fbManager = createFBManager();
        if (initialCreate) createDefaultDatabase();
    }

    // NOTE: Can be called with context == null (from UsesDatabase)
    @Override
    public void afterEach(ExtensionContext context) {
        try {
            for (String databasePath : databasesToDrop) {
                try {
                    fbManager.dropDatabase(databasePath, FBTestProperties.DB_USER, FBTestProperties.DB_PASSWORD);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            try {
                fbManager.stop();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            fbManager = null;
        }
    }

    public void createDefaultDatabase() throws Exception {
        addDatabase(getDatabasePath());
        defaultDatabaseSetUp(fbManager);
        executeInitStatements();
    }

    /**
     * Add a database path to be dropped when this rule completes.
     * <p>
     * Primary use case is cleaning up extra databases created in a test.
     * </p>
     *
     * @param databasePath Database path
     */
    public void addDatabase(String databasePath) {
        databasesToDrop.add(databasePath);
    }

    // TODO Consider implementing a way to have a non-standard initialization (eg as in TestResultSetDialect1)

    /**
     * Creates a rule to initialize (and drop) a test database with the default configuration.
     *
     * @return a UsesDatabase rule
     */
    public static UsesDatabaseExtension usesDatabase() {
        return new UsesDatabaseExtension(true);
    }

    /**
     * Create a rule to intialize (and drop) a test database with specific initialization statements.
     * <p>
     * Statements are executed in a single transaction. If you need intermediate commits, add statement
     * {@code COMMIT WORK} (case insensitive).
     * </p>
     * <p>
     * Statements will be executed only for the default database, not for databases registered with
     * {@link #addDatabase(String)}.
     * </p>
     *
     * @param initializationStatements Statements to initialize database.
     * @return a UsesDatabase rule
     * @since 4.0
     */
    public static UsesDatabaseExtension usesDatabase(String... initializationStatements) {
        return usesDatabase(Arrays.asList(initializationStatements));
    }

    /**
     * Create a rule to intialize (and drop) a test database with specific initialization statements.
     * <p>
     * Statements are executed in a single transaction. If you need intermediate commits, add statement
     * {@code COMMIT WORK} (case insensitive).
     * </p>
     * <p>
     * Statements will be executed only for the default database, not for databases registered with
     * {@link #addDatabase(String)}.
     * </p>
     *
     * @param initializationStatements Statements to initialize database.
     * @return a UsesDatabase rule
     * @since 4.0
     */
    public static UsesDatabaseExtension usesDatabase(List<String> initializationStatements) {
        UsesDatabaseExtension extension = new UsesDatabaseExtension(true);
        extension.initStatements.addAll(initializationStatements);
        return extension;
    }

    /**
     * Creates a rule that doesn't create an initial database.
     * <p>
     * Call {@link #createDefaultDatabase()} to create the default database.
     * </p>
     *
     * @return a UsesDatabase rule
     */
    public static UsesDatabaseExtension noDatabase() {
        return new UsesDatabaseExtension(false);
    }

    private void executeInitStatements() throws SQLException {
        if (initStatements.isEmpty()) return;

        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            for (String initStatement : initStatements) {
                if ("COMMIT WORK".equalsIgnoreCase(initStatement)) {
                    connection.commit();
                    continue;
                }
                stmt.execute(initStatement);
            }
            connection.commit();
        }
    }
}
