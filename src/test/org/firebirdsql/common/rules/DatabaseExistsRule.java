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
import org.firebirdsql.management.FBStatisticsManager;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.sql.SQLException;

/**
 * JUnit rule that checks if a database exists.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class DatabaseExistsRule implements TestRule {

    private final String databaseName;
    private final CheckType checkType;

    public static DatabaseExistsRule requireExistence(String databaseName) {
        return new DatabaseExistsRule(databaseName, CheckType.REQUIRE_EXISTENCE);
    }

    private DatabaseExistsRule(String databaseName, CheckType checkType) {
        this.databaseName = databaseName;
        this.checkType = checkType;
    }

    @Override
    public Statement apply(final Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                checkDatabaseExists();
                statement.evaluate();
            }
        };
    }

    private void checkDatabaseExists() {
        FBStatisticsManager statisticsManager = new FBStatisticsManager(FBTestProperties.getGdsType());
        statisticsManager.setHost(FBTestProperties.DB_SERVER_URL);
        statisticsManager.setPort(FBTestProperties.DB_SERVER_PORT);
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
            void onExists(String databaseName) {
                // do nothing
            }

            @Override
            void onConnectFailure(String databaseName,SQLException e) {
                throw new AssumptionViolatedException("Expected database " + databaseName + " to exist, error was: " +
                        e.getMessage());
            }
        }
        ;

        abstract void onExists(String databaseName);

        abstract void onConnectFailure(String databaseName, SQLException e);
    }

}
