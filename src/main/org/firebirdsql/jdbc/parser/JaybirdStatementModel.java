 /*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc.parser;

import java.util.ArrayList;

/**
 * Simple model of the parsed statement. At the moment the original statement 
 * cannot be reconstructed from this model, but it should be possible when the
 * parser is extended with the new functionality.
 */
public class JaybirdStatementModel {
    
    public static final int INSERT_TYPE = 1;
    public static final int UPDATE_TYPE = 2;
    public static final int DELETE_TYPE = 3;
    public static final int UPDATE_OR_INSERT_TYPE = 4;
    public static final int EXECUTE_TYPE = 5;

    private int statementType;
    
    private String tableName;
    private String selectClause;
    private ArrayList columns = new ArrayList();
    private ArrayList values = new ArrayList();
    private ArrayList returningColumns = new ArrayList();
    
    private boolean defaultValues;
    
    public int getStatementType() {
        return statementType;
    }
    
    public void setStatementType(int statementType) {
        this.statementType = statementType;
    }

    public boolean isDefaultValues() {
        return defaultValues;
    }
    
    public void setDefaultValues(boolean defaultValues) {
        this.defaultValues = defaultValues;
    }

    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getSelectClause() {
        return selectClause;
    }
    
    public void setSelectClause(String selectClause) {
        this.selectClause = selectClause;
    }
    
    public ArrayList getColumns() {
        return columns;
    }
    
    public void addColumn(String columnName) {
        columns.add(columnName);
    }
    
    public ArrayList getValues() {
        return values;
    }
    
    public void addValue(String value) {
        values.add(value);
    }
    
    public ArrayList getReturningColumns() {
        return returningColumns;
    }
    
    public void addReturningColumn(String columnName) {
        returningColumns.add(columnName); 
    }
}
