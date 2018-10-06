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

import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * An object that can be used to get information about the types and properties of the columns in
 * a <code>ResultSet</code> object.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@SuppressWarnings("RedundantThrows")
public class FBResultSetMetaData extends AbstractFieldMetaData implements FirebirdResultSetMetaData {

    private static final int ID_UNICODE_FSS = 3;

    private final ColumnStrategy columnStrategy;

    /**
     * Creates a new <code>FBResultSetMetaData</code> instance.
     *
     * @param rowDescriptor
     *         a row descriptor
     * @param connection
     *         a <code>FBConnection</code> value
     * @throws SQLException
     *         if an error occurs
     *         <p/>
     *         TODO Need another constructor for metadata from constructed
     *         result set, where we supply the ext field info.
     */
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
     * <p>
     * The current implementation always returns <code>false</code>.
     * </p>
     */
    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The current implementation always returns <code>true</code>.
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
        final int colType = getColumnType(column);
        final int precision = getPrecision(column);

        switch (colType) {
        case Types.DECIMAL:
        case Types.NUMERIC:
            return precision + 2; // sign + decimal separator
        case Types.FLOAT:
        case Types.DOUBLE:
            return precision + 2; // sign + decimal separator
        case Types.INTEGER:
        case Types.BIGINT:
        case Types.SMALLINT:
            return precision + 1; // sign
        case JaybirdTypeCodes.DECFLOAT:
            if (precision == 16) {
                return 16 + 7;  // 7: sign + decimal separator + E + sign exp + exp (3 pos)
            }
            return 34 + 8; // 8: sign + decimal separator + E + sign exp + exp (4 pos)
        case Types.BOOLEAN:
            return 5; // assuming displaying true/false
        default:
            return precision;
        }
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
     * <b>NOTE</b> For <code>NUMERIC</code> and <code>DECIMAL</code> we attempt to retrieve the exact precision from the
     * metadata, if this is not possible (eg the column is dynamically defined in the query), the reported precision is
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
     * The current implementation always returns <code>false</code>, except for a DB_KEY column.
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
     * The current implementation always returns <code>true</code>, except for a DB_KEY column.
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

    //@formatter:off
    private static final String GET_FIELD_INFO =
            "SELECT "
            + "  RF.RDB$RELATION_NAME as RELATION_NAME"
            + ", RF.RDB$FIELD_NAME as FIELD_NAME"
            + ", F.RDB$FIELD_LENGTH as FIELD_LENGTH"
            + ", F.RDB$FIELD_PRECISION as FIELD_PRECISION"
            + ", F.RDB$FIELD_SCALE as FIELD_SCALE"
            + ", F.RDB$FIELD_SUB_TYPE as FIELD_SUB_TYPE"
            + ", F.RDB$CHARACTER_SET_ID as CHARACTER_SET_ID"
            + ", F.RDB$SYSTEM_FLAG as SYSTEM_FLAG"
            + ", F.RDB$CHARACTER_LENGTH as CHAR_LEN"
            + " FROM"
            + "  RDB$RELATION_FIELDS RF "
            + ", RDB$FIELDS F "
            + " WHERE "
            + "  RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME"
            + " AND"
            + "  RF.RDB$FIELD_NAME = ?"
            + " AND"
            + "  RF.RDB$RELATION_NAME = ?";
    //@formatter:on

    @Override
    protected Map<FieldKey, ExtendedFieldInfo> getExtendedFieldInfo(FBConnection connection) throws SQLException {
        if (connection == null) return Collections.emptyMap();

        // Apparently there is a limit in the UNION
        // It is necessary to split in several queries
        // Although the problem reported with 93 UNION use only 70
        int pending = getFieldCount();
        Map<FieldKey, ExtendedFieldInfo> result = new HashMap<>();
        final FBDatabaseMetaData metaData = (FBDatabaseMetaData) connection.getMetaData();
        while (pending > 0) {
            StringBuilder sb = new StringBuilder();

            int maxLength = Math.min(pending, 70);
            List<String> params = new ArrayList<>(2 * maxLength);
            for (int i = 1; i <= maxLength; i++) {

                String relationName = getFieldDescriptor(i).getOriginalTableName();
                String fieldName = getFieldDescriptor(i).getOriginalName();

                if (relationName == null || relationName.equals("")
                        || fieldName == null || fieldName.equals("")) continue;

                if (sb.length() > 0) {
                    sb.append('\n').append("UNION ALL").append('\n');
                }
                sb.append(GET_FIELD_INFO);

                params.add(fieldName);
                params.add(relationName);
            }

            pending -= maxLength;

            if (sb.length() == 0) continue;

            try (ResultSet rs = metaData.doQuery(sb.toString(), params, true)) {
                while (rs.next()) {
                    ExtendedFieldInfo fieldInfo = new ExtendedFieldInfo();

                    fieldInfo.relationName = rs.getString("RELATION_NAME");
                    fieldInfo.fieldName = rs.getString("FIELD_NAME");
                    fieldInfo.fieldLength = rs.getInt("FIELD_LENGTH");
                    fieldInfo.fieldPrecision = rs.getInt("FIELD_PRECISION");
                    fieldInfo.fieldScale = rs.getInt("FIELD_SCALE");
                    fieldInfo.fieldSubtype = rs.getInt("FIELD_SUB_TYPE");
                    fieldInfo.characterSetId = rs.getInt("CHARACTER_SET_ID");
                    boolean systemField = rs.getBoolean("SYSTEM_FLAG");
                    fieldInfo.characterLength = rs.getInt("CHAR_LEN");

                    if (rs.wasNull()) {
                        if (systemField && fieldInfo.characterSetId == ID_UNICODE_FSS) {
                            fieldInfo.characterLength = fieldInfo.fieldLength;
                        } else {
                            IEncodingFactory encodingFactory = getRowDescriptor().getEncodingFactory();
                            final EncodingDefinition encodingDefinition =
                                    encodingFactory.getEncodingDefinitionByCharacterSetId(fieldInfo.characterSetId);
                            final int charsetSize = encodingDefinition != null
                                    ? encodingDefinition.getMaxBytesPerChar()
                                    : 1;
                            fieldInfo.characterLength = fieldInfo.fieldLength / charsetSize;
                        }
                    }

                    result.put(new FieldKey(fieldInfo.relationName, fieldInfo.fieldName), fieldInfo);
                }
            }
            params.clear();
        }
        return result;
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
