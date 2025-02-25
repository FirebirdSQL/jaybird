/*
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2001 Boix i Oltra, S.L.
 SPDX-FileContributor: Alejandro Alberola (Boix i Oltra, S.L.)
 SPDX-FileCopyrightText: Copyright 2001-2010 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003 Nikolay Samofatov
 SPDX-FileCopyrightText: Copyright 2005-2006 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.jdbc.field.JdbcTypeConverter;
import org.firebirdsql.util.InternalApi;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static org.firebirdsql.jaybird.util.StringUtils.isNullOrEmpty;

/**
 * Information about the types and properties of the columns in a {@link ResultSet} object.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * For the public API, refer to the {@link ResultSetMetaData} and {@link FirebirdResultSetMetaData} interfaces.
 * </p>
 *
 * @author David Jencks
 * @author Mark Rotteveel
 */
@SuppressWarnings("RedundantThrows")
@InternalApi
public class FBResultSetMetaData extends AbstractFieldMetaData implements FirebirdResultSetMetaData {

    private final ColumnStrategy columnStrategy;

    /**
     * Creates a new {@code FBResultSetMetaData} instance.
     *
     * @param rowDescriptor
     *         a row descriptor
     * @param connection
     *         a {@code FBConnection} value
     * @throws SQLException
     *         if an error occurs
     */
    //TODO Need another constructor for metadata from constructed result set, where we supply the ext field info.
    protected FBResultSetMetaData(RowDescriptor rowDescriptor, FBConnection connection) throws SQLException {
        super(rowDescriptor, connection);

        // Decide how to handle column names and column labels
        if (isColumnLabelForName(connection)) {
            columnStrategy = ColumnStrategy.COLUMN_LABEL_FOR_NAME;
        } else {
            columnStrategy = ColumnStrategy.DEFAULT;
        }
    }

    private static boolean isColumnLabelForName(FBConnection connection) throws SQLException {
        if (connection == null) {
            return false;
        }
        GDSHelper gdsHelper = connection.getGDSHelper();
        return gdsHelper != null && gdsHelper.getConnectionProperties().isColumnLabelForName();
    }

    @Override
    public int getColumnCount() throws SQLException {
        return getFieldCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        return switch (getColumnType(column)) {
            case Types.SMALLINT, Types.INTEGER, Types.BIGINT -> {
                ExtendedFieldInfo extFieldInfo = getExtFieldInfo(column);
                yield extFieldInfo != null && extFieldInfo.autoIncrement();
            }
            default -> false;
        };
    }

    /**
     * {@inheritDoc}
     * <p>
     * The current implementation always returns {@code true}.
     * </p>
     */
    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        return true;
    }

    @Override
    public boolean isSearchable(int column) throws SQLException {
        final int sqlType = getFieldDescriptor(column).getType() & ~1;
        return !((sqlType == ISCConstants.SQL_ARRAY)
                || (sqlType == ISCConstants.SQL_BLOB));
    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        return false;
    }

    @Override
    public int isNullable(int column) throws SQLException {
        return (getFieldDescriptor(column).getType() & 1) == 1
                ? ResultSetMetaData.columnNullable
                : ResultSetMetaData.columnNoNulls;
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        return isSignedInternal(column);
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        final int precision = getPrecision(column);
        return switch (getColumnType(column)) {
            case Types.DECIMAL, Types.NUMERIC -> precision + 2; // sign + decimal separator
            case Types.FLOAT ->
                    7 + 6; // 7: number of decimal digits, 6: sign + decimal separator + E + sign exp + exp (2 pos)
            case Types.DOUBLE ->
                    15 + 7; // 15: number of decimal digits, 7: sign + decimal separator + E + sign exp + exp (3 pos)
            case Types.INTEGER, Types.BIGINT, Types.SMALLINT -> precision + 1; // sign
            case JaybirdTypeCodes.DECFLOAT -> {
                if (precision == 16) {
                    yield 16 + 7; // 7: sign + decimal separator + E + sign exp + exp (3 pos)
                }
                yield 34 + 8; // 8: sign + decimal separator + E + sign exp + exp (4 pos)
            }
            case Types.BOOLEAN -> 5; // assuming displaying true/false
            default -> precision;
        };
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return columnStrategy.getColumnLabel(getFieldDescriptor(column));
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return columnStrategy.getColumnName(getFieldDescriptor(column));
    }

    /**
     * {@inheritDoc}
     * 
     * @return Always {@code ""} as schemas are not supported.
     */
    @Override
    public String getSchemaName(int column) throws SQLException {
        return "";
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>NOTE</b> For {@code NUMERIC} and {@code DECIMAL} we attempt to retrieve the exact precision from the metadata,
     * if this is not possible (eg the column is dynamically defined in the query), the reported precision is
     * the maximum precision allowed by the underlying storage data type.
     * </p>
     */
    @Override
    public int getPrecision(int column) throws SQLException {
        return getPrecisionInternal(column);
    }

    @Override
    public int getScale(int column) throws SQLException {
        return getScaleInternal(column);
    }

    @Override
    public String getTableName(int column) throws SQLException {
        String result = getFieldDescriptor(column).getOriginalTableName();
        if (result == null) result = "";
        return result;
    }

    @Override
    public String getTableAlias(int column) throws SQLException {
        String result = getFieldDescriptor(column).getTableAlias();
        if (result == null) result = getTableName(column);
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @return Always {@code ""} as catalogs are not supported
     */
    @Override
    public String getCatalogName(int column) throws SQLException {
        return "";
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        return getFieldType(column);
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return getFieldTypeName(column);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The current implementation always returns {@code false}, except for a DB_KEY column.
     * </p>
     */
    @Override
    public boolean isReadOnly(int column) throws SQLException {
        // TODO Need to consider privileges!!
        return getFieldDescriptor(column).isDbKey();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The current implementation always returns {@code true}, except for a DB_KEY column.
     * </p>
     */
    @Override
    public boolean isWritable(int column) throws SQLException {
        // TODO Needs privileges?
        return !getFieldDescriptor(column).isDbKey();
    }

    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        return isWritable(column);
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        return getFieldClassName(column);
    }

    private static final int FIELD_INFO_RELATION_NAME = 1;
    private static final int FIELD_INFO_FIELD_NAME = 2;
    private static final int FIELD_INFO_FIELD_PRECISION = 3;
    private static final int FIELD_INFO_FIELD_AUTO_INC = 4;

    private static final String GET_FIELD_INFO_25 = """
            select
              RF.RDB$RELATION_NAME as RELATION_NAME,
              RF.RDB$FIELD_NAME as FIELD_NAME,
              F.RDB$FIELD_PRECISION as FIELD_PRECISION,
              'F' as FIELD_AUTO_INC
            from RDB$RELATION_FIELDS RF inner join RDB$FIELDS F
              on RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME
            where RF.RDB$FIELD_NAME = ? and RF.RDB$RELATION_NAME = ?""";

    private static final String GET_FIELD_INFO_30 = """
            select
              RF.RDB$RELATION_NAME as RELATION_NAME,
              RF.RDB$FIELD_NAME as FIELD_NAME,
              F.RDB$FIELD_PRECISION as FIELD_PRECISION,
              RF.RDB$IDENTITY_TYPE is not null as FIELD_AUTO_INC
            from RDB$RELATION_FIELDS RF inner join RDB$FIELDS F
              on RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME
            where RF.RDB$FIELD_NAME = ? and RF.RDB$RELATION_NAME = ?""";

    // Apparently there is a limit in the UNION. It is necessary to split in several queries. Although the problem
    // reported with 93 UNION, use only 70.
    private static final int MAX_FIELD_INFO_UNIONS = 70;

    @Override
    @SuppressWarnings({ "java:S1994", "java:S135" })
    protected Map<FieldKey, ExtendedFieldInfo> getExtendedFieldInfo(FBConnection connection) throws SQLException {
        if (connection == null || !connection.isExtendedMetadata()) return Collections.emptyMap();

        final int fieldCount = getFieldCount();
        int currentColumn = 1;
        var result = new HashMap<FieldKey, ExtendedFieldInfo>();
        FBDatabaseMetaData metaData = (FBDatabaseMetaData) connection.getMetaData();
        var params = new ArrayList<String>();
        var sb = new StringBuilder();
        boolean fb3OrHigher = metaData.getDatabaseMajorVersion() >= 3;
        String getFieldInfoQuery = fb3OrHigher ? GET_FIELD_INFO_30 : GET_FIELD_INFO_25;
        while (currentColumn <= fieldCount) {
            params.clear();
            sb.setLength(0);

            for (int unionCount = 0; currentColumn <= fieldCount && unionCount < MAX_FIELD_INFO_UNIONS; currentColumn++) {
                FieldDescriptor fieldDescriptor = getFieldDescriptor(currentColumn);
                if (!needsExtendedFieldInfo(fieldDescriptor, fb3OrHigher)) continue;

                String relationName = fieldDescriptor.getOriginalTableName();
                String fieldName = fieldDescriptor.getOriginalName();

                if (isNullOrEmpty(relationName) || isNullOrEmpty(fieldName)) continue;

                if (unionCount != 0) {
                    sb.append("\nunion all\n");
                }
                sb.append(getFieldInfoQuery);

                params.add(fieldName);
                params.add(relationName);

                unionCount++;
            }

            if (sb.isEmpty()) continue;

            try (ResultSet rs = metaData.doQuery(sb.toString(), params, true)) {
                while (rs.next()) {
                    ExtendedFieldInfo fieldInfo = extractExtendedFieldInfo(rs);
                    result.put(fieldInfo.fieldKey(), fieldInfo);
                }
            }
        }
        return result;
    }

    private static ExtendedFieldInfo extractExtendedFieldInfo(ResultSet rs) throws SQLException {
        return new ExtendedFieldInfo(rs.getString(FIELD_INFO_RELATION_NAME), rs.getString(FIELD_INFO_FIELD_NAME),
                rs.getInt(FIELD_INFO_FIELD_PRECISION), rs.getBoolean(FIELD_INFO_FIELD_AUTO_INC));
    }

    /**
     * @return {@code true} when the field descriptor needs extended field info (currently only NUMERIC and DECIMAL,
     * and - when {@code fb3OrHigher == true} - INTEGER, BIGINT and SMALLINT)
     */
    private static boolean needsExtendedFieldInfo(FieldDescriptor fieldDescriptor, boolean fb3OrHigher) {
        return switch (JdbcTypeConverter.toJdbcType(fieldDescriptor)) {
            case Types.NUMERIC, Types.DECIMAL -> true;
            case Types.INTEGER, Types.BIGINT, Types.SMALLINT -> fb3OrHigher;
            default -> false;
        };
    }

    /**
     * Strategy for retrieving column labels and column names
     */
    private enum ColumnStrategy {
        /**
         * Default, JDBC-compliant, strategy for column naming.
         * <p>
         * columnLabel is the AS clause (xsqlvar.aliasname) if specified,
         * otherwise xsqlvar.sqlname.
         * </p>
         * <p/>
         * columnName is xsqlvar.sqlname if specified, otherwise xsqlvar.aliasname (TODO: change this?)
         * <p/>
         */
        DEFAULT {
            @Override
            String getColumnName(FieldDescriptor fieldDescriptor) {
                return fieldDescriptor.getOriginalName() != null
                        ? fieldDescriptor.getOriginalName()
                        : getColumnLabel(fieldDescriptor);
            }
        },
        /**
         * Alternative strategy for column naming (related to columnLabelForName connection property)
         * <p>
         * This strategy is not JDBC-compliant, but is provided as a workaround for use with com.sun.rowset.CachedRowSetImpl and
         * for people expecting the old behavior.
         * <p>
         * columnLabel is the AS clause (xsqlvar.aliasname) if specified,
         * otherwise xsqlvar.sqlname.
         * </p>
         * <p>
         * columnName is identical to columnLabel.
         * </p>
         */
        COLUMN_LABEL_FOR_NAME {
            @Override
            String getColumnName(FieldDescriptor fieldDescriptor) {
                return getColumnLabel(fieldDescriptor);
            }
        };

        /**
         * Retrieve the columnName for the specified column.
         *
         * @param fieldDescriptor
         *         Column descriptor
         * @return value for the columnName
         */
        abstract String getColumnName(FieldDescriptor fieldDescriptor);

        /**
         * Retrieve the columnLabel for the specified column.
         *
         * @param fieldDescriptor
         *         Column descriptor
         * @return value for the columnLabel
         */
        String getColumnLabel(FieldDescriptor fieldDescriptor) {
            if (fieldDescriptor.getFieldName() != null) {
                return fieldDescriptor.getFieldName();
            } else if (fieldDescriptor.getOriginalName() != null) {
                return fieldDescriptor.getOriginalName();
            } else {
                return "";
            }
        }
    }
}
