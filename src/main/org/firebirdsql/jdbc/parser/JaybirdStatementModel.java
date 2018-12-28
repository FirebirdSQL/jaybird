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
package org.firebirdsql.jdbc.parser;

/**
 * Simple model of the parsed statement. At the moment the original statement
 * cannot be reconstructed from this model, but it should be possible when the
 * parser is extended with the new functionality.
 */
public class JaybirdStatementModel {

    public static final int UNDETECTED_TYPE = 0;
    public static final int INSERT_TYPE = 1;
    public static final int UPDATE_TYPE = 2;
    public static final int DELETE_TYPE = 3;
    public static final int UPDATE_OR_INSERT_TYPE = 4;
    @SuppressWarnings("unused")
    @Deprecated
    public static final int EXECUTE_TYPE = 5;
    public static final int MERGE_TYPE = 6;
    public static final int MAX_STATEMENT_TYPE_VALUE = MERGE_TYPE;

    private int statementType = UNDETECTED_TYPE;

    private String tableName;
    private boolean hasReturning;

    public int getStatementType() {
        return statementType;
    }

    public void setStatementType(int statementType) {
        this.statementType = statementType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean hasReturning() {
        return hasReturning;
    }

    public void setHasReturning() {
        hasReturning = true;
    }
}
