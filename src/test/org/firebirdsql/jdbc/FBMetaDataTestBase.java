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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.jdbc.MetaDataValidator.MetaDataInfo;
import org.junit.After;
import org.junit.Before;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;

/**
 * Base test class for testing and validating the various DatabaseMetaData
 * methods returning a ResultSet.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * 
 * @param <T>
 *            Enum containing the metadata columns to be validated.
 */
public abstract class FBMetaDataTestBase<T extends Enum<T> & MetaDataInfo> extends FBJUnit4TestBase {

    private Class<T> metaDataInfoClass;

    /**
     * Constructor for FBMetaDataTestBase.
     * 
     * @param metaDataInfoClass
     *            Class of the enum containing the metadata columns
     */
    protected FBMetaDataTestBase(Class<T> metaDataInfoClass) {
        this.metaDataInfoClass = metaDataInfoClass;
    }

    protected Connection con;
    protected DatabaseMetaData dbmd;

    @Before
    public final void setUp() throws Exception {
        try {
            con = getConnectionViaDriverManager();
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

    @After
    public final void tearDown() throws Exception {
        try {
            additionalTeardown();
        } finally {
            closeQuietly(con);
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
