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
package org.firebirdsql.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Definition/description of a metadata result set.
 *
 * @author Mark Rotteveel
 * @since 6
 */
public record MetadataResultSetDefinition(List<MetaDataInfo> metaDataInfo) {

    public MetadataResultSetDefinition {
        metaDataInfo = List.copyOf(metaDataInfo);
    }

    public <T extends Enum<T> & MetaDataInfo> MetadataResultSetDefinition(Class<T> metadataEnumClass) {
        this(List.of(metadataEnumClass.getEnumConstants()));
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
    void validateRowValues(ResultSet metaData, Map<? extends MetaDataInfo, Object> valueRules) throws SQLException {
        for (Map.Entry<? extends MetaDataInfo, Object> rule : valueRules.entrySet()) {
            rule.getKey().assertColumnValue(metaData, rule.getValue());
        }
    }

    /**
     * Validates the columns in the metadata result set for position and type.
     *
     * @param metaDataResultSet
     *         A metadata resultset for the metadata represented by T
     */
    void validateResultSetColumns(ResultSet metaDataResultSet) throws Exception {
        for (MetaDataInfo column : metaDataInfo()) {
            column.assertColumnPosition(metaDataResultSet);
            column.assertColumnType(metaDataResultSet);
        }
    }

    /**
     * Checks if the validationRules contain all required metadata columns.
     * <p>
     * If any validation rules are missing this will be printed to the error console, including a stacktrace. No
     * exception is thrown.
     * </p>
     *
     * @param validationRules
     *         Map of the validation rules
     */
    void checkValidationRulesComplete(Map<? extends MetaDataInfo, Object> validationRules) {
        Set<MetaDataInfo> undefinedKeys = new HashSet<>(metaDataInfo);
        undefinedKeys.removeAll(validationRules.keySet());
        if (!undefinedKeys.isEmpty()) {
            new AssertionError(
                    "Some validation rules not defined for test method, entries missing: " + undefinedKeys)
                    .printStackTrace();
        }
    }

}
