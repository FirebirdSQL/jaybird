/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.common.rules;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension.UsesDatabaseForEach;
import org.junit.rules.ExternalResource;

import java.util.List;

/**
 * JUnit 4 rule that creates and deletes a database.
 * <p>
 * For JUnit 5, see the extension {@link org.firebirdsql.common.extension.UsesDatabaseExtension}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class UsesDatabase extends ExternalResource {

    private final UsesDatabaseForEach usesDatabaseExtension;

    private UsesDatabase(UsesDatabaseForEach usesDatabaseExtension) {
        this.usesDatabaseExtension = usesDatabaseExtension;
    }

    /**
     * Basic setup of the test database.
     */
    @Override
    protected void before() throws Exception {
        usesDatabaseExtension.beforeEach(null);
    }

    public void createDefaultDatabase() throws Exception {
        usesDatabaseExtension.createDefaultDatabase();
    }

    /**
     * Basic teardown of the test database
     */
    @Override
    protected void after() {
        usesDatabaseExtension.afterEach(null);
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
        usesDatabaseExtension.addDatabase(databasePath);
    }

    /**
     * Creates a rule to initialize (and drop) a test database with the default configuration.
     *
     * @return a UsesDatabase rule
     */
    public static UsesDatabase usesDatabase() {
        return new UsesDatabase(UsesDatabaseExtension.usesDatabase());
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
    public static UsesDatabase usesDatabase(String... initializationStatements) {
        return new UsesDatabase(UsesDatabaseExtension.usesDatabase(initializationStatements));
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
    public static UsesDatabase usesDatabase(List<String> initializationStatements) {
        return new UsesDatabase(UsesDatabaseExtension.usesDatabase(initializationStatements));
    }

    /**
     * Creates a rule that doesn't create an initial database.
     * <p>
     * Call {@link #createDefaultDatabase()} to create the default database.
     * </p>
     *
     * @return a UsesDatabase rule
     */
    public static UsesDatabase noDatabase() {
        return new UsesDatabase(UsesDatabaseExtension.noDatabase());
    }
}
