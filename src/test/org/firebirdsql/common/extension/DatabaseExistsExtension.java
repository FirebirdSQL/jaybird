// SPDX-FileCopyrightText: Copyright 2018-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.extension;

import org.firebirdsql.management.FBStatisticsManager;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;

import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.configureDefaultServiceProperties;
import static org.firebirdsql.common.FBTestProperties.getGdsType;

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
        var statisticsManager = new FBStatisticsManager(getGdsType());
        configureDefaultServiceProperties(statisticsManager);
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
