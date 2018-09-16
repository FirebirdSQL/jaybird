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

import org.firebirdsql.jdbc.MetaDataValidator.MetaDataInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Support class for testing and validating the various DatabaseMetaData methods returning a ResultSet.
 *
 * @param <T>
 *         Enum containing the metadata columns to be validated.
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
final class MetaDataTestSupport<T extends Enum<T> & MetaDataInfo> {

    private final Class<T> metaDataInfoClass;
    private final Set<T> requiredMetaData;

    /**
     * Constructor for FBMetaDataTestBase.
     *
     * @param metaDataInfoClass
     *         Class of the enum containing the metadata columns
     */
    MetaDataTestSupport(Class<T> metaDataInfoClass) {
        this.metaDataInfoClass = metaDataInfoClass;
        requiredMetaData = Collections.unmodifiableSet(EnumSet.allOf(metaDataInfoClass));
    }

    MetaDataTestSupport(Class<T> metaDataInfoClass, Set<T> requiredMetaData) {
        this.metaDataInfoClass = metaDataInfoClass;
        this.requiredMetaData = requiredMetaData.isEmpty()
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(requiredMetaData));
    }

    /**
     * Validates the current row in the ResultSet for the supplied rules.
     *
     * @param metaData
     *         ResultSet
     * @param valueRules
     *         Validation rules for column values
     * @throws SQLException
     *         For exceptions retrieving values
     */
    void validateRowValues(ResultSet metaData, Map<T, Object> valueRules) throws SQLException {
        for (Map.Entry<T, Object> rule : valueRules.entrySet()) {
            rule.getKey().getValidator().assertColumnValue(metaData, rule.getValue());
        }
    }

    /**
     * Validates the columns in the metadata resultset for position and type.
     *
     * @param metaDataResultSet
     *         A metadata resultset for the metadata represented by T
     */
    void validateResultSetColumns(ResultSet metaDataResultSet) throws Exception {
        for (T column : getRequiredMetaData()) {
            MetaDataValidator<?> validator = column.getValidator();
            validator.assertColumnPosition(metaDataResultSet);
            validator.assertColumnType(metaDataResultSet);
        }
    }

    /**
     * Checks if the validationRules contain all required metadata columns (as
     * provided by {@link #getRequiredMetaData()}.
     * <p>
     * If any validation rules are missing this will be printed to the error
     * console, including a stacktrace. No exception is thrown.
     * </p>
     *
     * @param validationRules
     *         Map of the validation rules
     */
    void checkValidationRulesComplete(Map<T, Object> validationRules) {
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
     * The returned set is a copy, so the caller is allowed to modify at will
     * </p>
     *
     * @return Set with the required columns in the metadata ResultSet
     */
    Set<T> getRequiredMetaData() {
        return requiredMetaData.isEmpty() ? EnumSet.noneOf(metaDataInfoClass) : EnumSet.copyOf(requiredMetaData);
    }
}
