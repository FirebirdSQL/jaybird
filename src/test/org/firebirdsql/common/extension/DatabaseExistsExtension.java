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
import org.firebirdsql.management.FBStatisticsManager;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;

import java.sql.SQLException;

/**
 * JUnit extension that checks if a database exists.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public class DatabaseExistsExtension implements BeforeAllCallback {

    private final String databaseName;
    private final CheckType checkType;

    public static DatabaseExistsExtension requireExistence(String databaseName) {
        return new DatabaseExistsExtension(databaseName, CheckType.REQUIRE_EXISTENCE);
    }

    private DatabaseExistsExtension(String databaseName, CheckType checkType) {
        this.databaseName = databaseName;
        this.checkType = checkType;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        checkDatabaseExists();
    }

    private void checkDatabaseExists() {
        FBStatisticsManager statisticsManager = new FBStatisticsManager(FBTestProperties.getGdsType());
        statisticsManager.setServerName(FBTestProperties.DB_SERVER_URL);
        statisticsManager.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        statisticsManager.setUser(FBTestProperties.DB_USER);
        statisticsManager.setPassword(FBTestProperties.DB_PASSWORD);
        statisticsManager.setDatabase(databaseName);
        try {
            statisticsManager.getHeaderPage();
            checkType.onExists(databaseName);
        } catch (SQLException e) {
            checkType.onConnectFailure(databaseName, e);
        }
    }

    private enum CheckType {
        REQUIRE_EXISTENCE {
            @Override
            void onConnectFailure(String databaseName, SQLException e) {
                throw new TestAbortedException(
                        "Expected database " + databaseName + " to exist, error was: " + e.getMessage());
            }
        }
        ;

        void onExists(String databaseName) {
            // default do nothing
        }

        void onConnectFailure(String databaseName, SQLException e) {
            // default do nothing
        }
    }

}
