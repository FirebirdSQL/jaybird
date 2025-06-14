// SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.extension;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.management.FBManager;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.firebirdsql.common.FBTestProperties.createFBManager;
import static org.firebirdsql.common.FBTestProperties.defaultDatabaseSetUp;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDatabasePath;

/**
 * JUnit 5 extension that creates and deletes a database.
 * <p>
 * When used with {@code @ExtendWith}, a default database is created. For more control,
 * use {@code @RegisterExtension}, the static factory methods can be used for configuration.
 * </p>
 * <p>
 * When the database(s) need to be shared by all tests in a class, use the {@code XXXForAll} static factory methods and
 * register the extension in a static field.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
public abstract class UsesDatabaseExtension {

    private final boolean initialCreate;
    private FBManager fbManager = null;
    private final List<String> initStatements;
    private final List<String> databasesToDrop = new ArrayList<>();

    private UsesDatabaseExtension(boolean initialCreate) {
        this(initialCreate, emptyList());
    }

    private UsesDatabaseExtension(boolean initialCreate, List<String> initStatements) {
        this.initialCreate = initialCreate;
        this.initStatements = initStatements;
    }

    void sharedBefore() throws Exception {
        fbManager = createFBManager();
        if (initialCreate) createDefaultDatabase();
    }

    void sharedAfter() {
        if (fbManager == null) return;
        try {
            if (fbManager.getState().equals("Stopped")) {
                FBTestProperties.configureFBManager(fbManager);
            }
            for (String databasePath : databasesToDrop) {
                try {
                    fbManager.dropDatabase(databasePath, FBTestProperties.DB_USER, FBTestProperties.DB_PASSWORD);
                } catch (Exception e) {
                    System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Exception dropping DB", e);
                }
            }
        } catch (Exception e){
            System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Exception dropping DBs", e);
        } finally {
            try {
                if (!(fbManager == null || fbManager.getState().equals("Stopped"))) {
                    fbManager.stop();
                }
            } catch (Exception e) {
                System.getLogger(getClass().getName())
                        .log(System.Logger.Level.ERROR, "Exception stopping FBManager", e);
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

    /**
     * Removes {@code databasePath} from the paths to be dropped when this rule completes.
     * <p>
     * Primary use case is to remove database path that are known to not exist, so no errors are logged when this rule
     * completes and cannot drop the specified database.
     * </p>
     *
     * @param databasePath
     *         database path
     * @since 6
     */
    public void removeDatabase(String databasePath) {
        databasesToDrop.remove(databasePath);
    }

    // TODO Consider implementing a way to have a non-standard initialization (e.g. as in TestResultSetDialect1)

    /**
     * Creates a rule to initialize (and drop) a test database with the default configuration.
     *
     * @return a UsesDatabase extension
     */
    public static UsesDatabaseForEach usesDatabase() {
        return new UsesDatabaseForEach(true);
    }

    /**
     * Variant of {@link #usesDatabase()} for use for all tests in a class, must be assigned to a static field.
     * <p>
     * Registered database are only cleaned up after all tests have been executed.
     * </p>
     * 
     * @return a UsesDatabase extension
     * @since 5
     */
    public static UsesDatabaseForAll usesDatabaseForAll() {
        return new UsesDatabaseForAll(true);
    }

    /**
     * Create a rule to initialize (and drop) a test database with specific initialization statements.
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
     * @return a UsesDatabase extension
     * @since 4.0
     */
    public static UsesDatabaseForEach usesDatabase(String... initializationStatements) {
        return usesDatabase(Arrays.asList(initializationStatements));
    }

    /**
     * Variant of {@link #usesDatabase(String[])} for use for all tests in a class, must be assigned to a static field.
     * <p>
     * Registered database are only cleaned up after all tests have been executed.
     * </p>
     *
     * @param initializationStatements Statements to initialize database.
     * @return a UsesDatabase extension
     * @see #usesDatabase(String...)
     * @since 5
     */
    public static UsesDatabaseForAll usesDatabaseForAll(String... initializationStatements) {
        return usesDatabaseForAll(Arrays.asList(initializationStatements));
    }

    /**
     * Create a rule to initialize (and drop) a test database with specific initialization statements.
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
     * @return a UsesDatabase extension
     * @since 4.0
     */
    public static UsesDatabaseForEach usesDatabase(List<String> initializationStatements) {
        return new UsesDatabaseForEach(true, initializationStatements);
    }

    /**
     * Variant of {@link #usesDatabase(List)} for use for all tests in a class, must be assigned to a static field.
     * <p>
     * Registered database are only cleaned up after all tests have been executed.
     * </p>
     *
     * @param initializationStatements Statements to initialize database.
     * @return a UsesDatabase extension
     * @see #usesDatabaseForAll(List)
     * @since 5
     */
    public static UsesDatabaseForAll usesDatabaseForAll(List<String> initializationStatements) {
        return new UsesDatabaseForAll(true, initializationStatements);
    }

    /**
     * Creates a rule that doesn't create an initial database.
     * <p>
     * Call {@link #createDefaultDatabase()} to create the default database.
     * </p>
     *
     * @return a UsesDatabase extension
     */
    public static UsesDatabaseForEach noDatabase() {
        return new UsesDatabaseForEach(false);
    }

    /**
     * Variant of {@link #noDatabase()} for use for all tests in a class, must be assigned to a static field.
     * <p>
     * Registered database are only cleaned up after all tests have been executed.
     * </p>
     *
     * @return a UsesDatabase extension
     * @since 5
     */
    public static UsesDatabaseForAll noDatabaseForAll() {
        return new UsesDatabaseForAll(false);
    }

    private void executeInitStatements() throws SQLException {
        if (initStatements.isEmpty()) return;

        try (var connection = getConnectionViaDriverManager();
             var stmt = connection.createStatement()) {
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

    public static class UsesDatabaseForEach extends UsesDatabaseExtension
            implements BeforeEachCallback, AfterEachCallback {

        private UsesDatabaseForEach(boolean initialCreate) {
            super(initialCreate);
        }

        private UsesDatabaseForEach(boolean initialCreate, List<String> initStatements) {
            super(initialCreate, initStatements);
        }

        // NOTE: Can be called with context == null (from UsesDatabase)
        @Override
        public void beforeEach(ExtensionContext context) throws Exception {
            sharedBefore();
        }

        // NOTE: Can be called with context == null (from UsesDatabase)
        @Override
        public void afterEach(ExtensionContext context) {
            sharedAfter();
        }

    }

    public static class UsesDatabaseForAll extends UsesDatabaseExtension
            implements BeforeAllCallback, AfterAllCallback {

        private UsesDatabaseForAll(boolean initialCreate) {
            super(initialCreate);
        }

        private UsesDatabaseForAll(boolean initialCreate, List<String> initStatements) {
            super(initialCreate, initStatements);
        }

        @Override
        public void beforeAll(ExtensionContext context) throws Exception {
            sharedBefore();
        }

        @Override
        public void afterAll(ExtensionContext context) {
            sharedAfter();
        }

    }

}
