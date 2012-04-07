/*
 * $Id$
 * 
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
package org.firebirdsql.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.jdbc.MetaDataValidator.MetaDataInfo;

/**
 * Base test class for testing and validating the various DatabaseMetaData
 * methods returning a ResultSet.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * 
 * @param <T>
 *            Enum containing the metadata columns to be validated.
 */
public abstract class FBMetaDataTestBase<T extends Enum & MetaDataInfo> extends FBTestBase {

    private Class<T> metaDataInfoClass;

    /**
     * Constructor for FBMetaDataTestBase.
     * 
     * @param name
     *            Name of the test (provided by JUnit)
     * @param metaDataInfoClass
     *            Class of the enum containing the metadata columns
     */
    protected FBMetaDataTestBase(String name, Class<T> metaDataInfoClass) {
        super(name);
        this.metaDataInfoClass = metaDataInfoClass;
    }

    protected Connection con;
    protected DatabaseMetaData dbmd;

    protected final void setUp() throws Exception {
        try {
            super.setUp();
            con = getConnectionViaDriverManager();
            for (String dropStatement : getDropStatements()) {
                executeDropTable(con, dropStatement);
            }
    
            for (String createStatement : getCreateStatements()) {
                executeCreateTable(con, createStatement);
            }
    
            dbmd = con.getMetaData();
    
            additionalSetup();
        } catch (Exception ex) {
            tearDown();
            throw ex;
        }
    }

    protected final void tearDown() throws Exception {
        try {
            for (String dropStatement : getDropStatements()) {
                executeDropTable(con, dropStatement);
            }
            additionalTeardown();
        } finally {
            closeQuietly(con);
            super.tearDown();
        }
    }

    /**
     * Method providing additional setup. This method is executed in setUp()
     * after creation of tables.
     */
    protected void additionalSetup() throws Exception {
        // default nothing
    }

    /**
     * Method providing additional teardown. This method is executed in
     * tearDown() after dropping of tables.
     */
    protected void additionalTeardown() throws Exception {
        // default nothing
    }

    /**
     * Provides the list of DROP statements to be executed in the setUp() and
     * tearDown().
     * <p>
     * The provided list must be ordered based on dependencies between objects
     * (if any)
     * </p>
     * 
     * @return List of drop statements
     */
    protected abstract List<String> getDropStatements();

    /**
     * Provides the list of CREATE (or other DDL) to be executed in the setUp().
     * <p>
     * The provided list must be ordered based on dependencies between objects
     * (if any)
     * </p>
     * 
     * @return List of create statements
     */
    protected abstract List<String> getCreateStatements();

    /**
     * Validates the current row in the ResultSet for the supplied rules.
     * 
     * @param metaData
     *            ResultSet
     * @param valueRules
     *            Validation rules for column values
     * @throws SQLException
     */
    protected void validateRowValues(ResultSet metaData, Map<T, Object> valueRules)
            throws SQLException {
        for (Map.Entry<T, Object> rule : valueRules.entrySet()) {
            rule.getKey().getValidator().assertColumnValue(metaData, rule.getValue());
        }
    }
    
    /**
     * Validates the columns in the metadata resultset for position and type.
     * 
     * @param metaDataResultSet A metadata resultset for the metadata represented by T
     */
    protected void validateResultSetColumns(ResultSet metaDataResultSet) throws Exception {
        for (T column : getRequiredMetaData()) {
            MetaDataValidator<?> validator = column.getValidator();
            validator.assertColumnPosition(metaDataResultSet);
            validator.assertColumnType(metaDataResultSet);
        }
    }

    /**
     * The default values for the validation rules for the columns in a row of
     * the metadata.
     * <p>
     * If a metadata column has no reasonable default it should not be defined
     * in the returned map.
     * </p>
     * 
     * @return Map with the default validation rules for the columns in a row of
     *         the metadata
     * @throws SQLException
     */
    protected abstract Map<T, Object> getDefaultValueValidationRules() throws Exception;

    /**
     * Checks if the validationRules contain all required metadata columns (as
     * provided by {@link #getRequiredMetaData()}.
     * <p>
     * If any validation rules are missing this will be printed to the error
     * console, including a stacktrace. No exception is thrown.
     * </p>
     * 
     * @param validationRules
     *            Map of the validation rules
     */
    protected final void checkValidationRulesComplete(Map<T, Object> validationRules) throws Exception {
        Set<T> undefinedKeys = getRequiredMetaData();
        undefinedKeys.removeAll(validationRules.keySet());
        if (undefinedKeys.size() != 0) {
            System.err.println("Some validation rules not defined for testmethod, entries missing: "
                            + undefinedKeys);
            new Exception().printStackTrace();
        }
    }

    /**
     * The required/expected metadata columns/validation rules for the metadata
     * ResultSet.
     * <p>
     * Default implementation returns all values of the Enum T.
     * </p>
     * <p>
     * NOTE: If overridden, make sure the returned set is a modifiable copy, as
     * the caller is allowed to modify the set.
     * </p>
     * 
     * @return Set with the required columns in the metadata ResultSet
     */
    protected Set<T> getRequiredMetaData() throws Exception {
        return EnumSet.allOf(metaDataInfoClass);
    }
}
