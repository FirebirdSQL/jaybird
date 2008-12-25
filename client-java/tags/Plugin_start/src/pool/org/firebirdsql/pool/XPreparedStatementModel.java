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
package org.firebirdsql.pool;

import java.util.Arrays;

import org.firebirdsql.jdbc.FirebirdStatement;

/**
 * Object containing all information that is needed to prepare the statement.
 *  
 * It also handles correct comparison using the SQL statement, result set type, 
 * concurrency, holdability or list of generated keys for INSERT statements and
 * can be used as a key in the hash table (cache).
 */
public class XPreparedStatementModel {

    private String sql;
    private int rsType;
    private int rsConcurrency;
    private int rsHoldability;
    private boolean generatedKeys;
    private int[] keyIndexes;
    private String[] keyColumns;
    
    public XPreparedStatementModel(String sql, int rsType, int rsConcurrency, int rsHoldability) {
        this.sql = sql;
        this.rsType = rsType;
        this.rsConcurrency = rsConcurrency;
        this.rsHoldability = rsHoldability;
        this.generatedKeys = false;
    }
    
    public XPreparedStatementModel(String sql, int generatedKeys) {
        this.sql = sql;
        this.generatedKeys = generatedKeys == FirebirdStatement.RETURN_GENERATED_KEYS;
    }
    
    public XPreparedStatementModel(String sql, int[] keyIndexes) {
        this.sql = sql;
        this.generatedKeys = true;
        this.keyIndexes = new int[keyIndexes.length];
        System.arraycopy(keyIndexes, 0, this.keyIndexes, 0, keyIndexes.length);
    }

    public XPreparedStatementModel(String sql, String[] keyColumns) {
        this.sql = sql;
        this.generatedKeys = true;
        this.keyColumns = new String[keyColumns.length];
        System.arraycopy(keyColumns, 0, this.keyColumns, 0, keyColumns.length);
    }
    
    public String getSql() {
        return sql;
    }

    public int getResultSetType() {
        return rsType;
    }

    public int getResultSetConcurrency() {
        return rsConcurrency;
    }

    public int getResultSetHoldability() {
        return rsHoldability;
    }

    public boolean isGeneratedKeys() {
        return generatedKeys;
    }

    public int[] getKeyIndexes() {
        return keyIndexes;
    }

    public String[] getKeyColumns() {
        return keyColumns;
    }

    private int hashCodeNoGeneratedKeys() {
        return sql.hashCode() ^ (rsType ^ rsConcurrency ^ rsHoldability);
    }
    
    private int hashCodeGeneratedKeys1() {
        return sql.hashCode();
    }
    
    private int hashCodeGeneratedKeys2() {
        int result = sql.hashCode();
        
        for (int i = 0; i < keyIndexes.length; i++) {
            result ^= keyIndexes[i];
        }
        
        return result;
    }
    
    private int hashCodeGeneratedKeys3() {
        int result = sql.hashCode();
        
        for (int i = 0; i < keyColumns.length; i++) {
            result ^= keyColumns[i].hashCode();
        }
        
        return result;
    }
    
    public int hashCode() {
        if (!generatedKeys)
            return hashCodeNoGeneratedKeys();
        else
        if (keyIndexes == null && keyColumns != null)
            return hashCodeGeneratedKeys3();
        else
        if (keyIndexes != null && keyColumns == null)
            return hashCodeGeneratedKeys2();
        else
        if (keyIndexes == null && keyColumns == null)
            return hashCodeGeneratedKeys1();
        else
            throw new IllegalStateException();
    }
    
    private boolean equalsNoGeneratedKeys(XPreparedStatementModel key) {
        boolean result = true;
        
        result &= sql.equals(key.sql);
        result &= rsType == key.rsType;
        result &= rsConcurrency == key.rsConcurrency;
        result &= rsHoldability == key.rsHoldability;
        
        return result;
    }
    
    private boolean equalsGeneratedKeys1(XPreparedStatementModel key) {
        return sql.equals(key.sql);
    }
    
    private boolean equalsGeneratedKeys2(XPreparedStatementModel key) {
        return sql.equals(key.sql) & Arrays.equals(keyIndexes, key.keyIndexes);
    }
    
    private boolean equalsGeneratedKeys3(XPreparedStatementModel key) {
        return sql.equals(key.sql) & Arrays.equals(keyColumns, key.keyColumns);
    }
    
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof XPreparedStatementModel)) return false;
        
        XPreparedStatementModel key = (XPreparedStatementModel)obj;
        
        if (!generatedKeys)
            return equalsNoGeneratedKeys(key);
        else
        if (keyIndexes == null && keyColumns != null)
            return equalsGeneratedKeys3(key);
        else
        if (keyIndexes != null && keyColumns == null)
            return equalsGeneratedKeys2(key);
        else
        if (keyIndexes == null && keyColumns == null)
            return equalsGeneratedKeys1(key);
        else
            throw new IllegalStateException();
    }
    
}
