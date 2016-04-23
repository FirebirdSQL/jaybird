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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.management.FBManager;
import org.junit.rules.ExternalResource;

import java.util.ArrayList;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.*;

/**
 * JUnit rule that creates and deletes a database.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class UsesDatabase extends ExternalResource {

    private final boolean initialCreate;
    private FBManager fbManager = null;
    private final List<String> databasesToDrop = new ArrayList<>();

    private UsesDatabase(boolean initialCreate) {
        // No outside instantiation
        this.initialCreate = initialCreate;
    }

    /**
     * Basic setup of the test database.
     */
    @Override
    protected void before() throws Exception {
        fbManager = createFBManager();
        if (initialCreate) createDefaultDatabase();
    }

    public void createDefaultDatabase() throws Exception {
        addDatabase(getDatabasePath());
        defaultDatabaseSetUp(fbManager);
    }

    /**
     * Basic teardown of the test database
     */
    @Override
    protected void after() {
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
    public static UsesDatabase usesDatabase() {
        return new UsesDatabase(true);
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
        return new UsesDatabase(false);
    }
}
