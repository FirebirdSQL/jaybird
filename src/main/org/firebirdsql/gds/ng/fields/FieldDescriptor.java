// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.fields;

import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.jaybird.util.StringUtils.trimToNull;

/**
 * The class {@code FieldDescriptor} contains the column metadata of the XSQLVAR server
 * data structure used to describe one column for input or output.
 * <p>
 * FieldDescriptor is an immutable type, the value of a field is maintained separately in {@link RowValue}.
 * </p>
 *
 * @author Mark Rotteveel
 * @version 3.0
 */
public final class FieldDescriptor {

    private final int position;
    private final DatatypeCoder datatypeCoder;
    private final int type;
    private final int subType;
    private final int scale;
    private final int length;
    private final @Nullable String fieldName;
    private final @Nullable String tableAlias;
    private final @Nullable String originalName;
    private final @Nullable String originalSchema;
    private final @Nullable String originalTableName;
    private final @Nullable String ownerName;

    // cached data derived from immutable state
    private int hash;
    private byte dbKey;

    /**
     * Constructor for metadata FieldDescriptor.
     *
     * @param position
     *         Position of this field (0-based), or {@code -1} if position is not known (e.g. for test code)
     * @param datatypeCoder
     *         Instance of DatatypeCoder to use when decoding column data (note that another instance may be derived
     *         internally, which then will be returned by {@link #getDatatypeCoder()})
     * @param type
     *         Column SQL type
     * @param subType
     *         Column subtype
     * @param scale
     *         Column scale
     * @param length
     *         Column defined length
     * @param fieldName
     *         Column alias name
     * @param tableAlias
     *         Column table alias
     * @param originalName
     *         Column original name
     * @param originalSchema
     *         Column original schema
     * @param originalTableName
     *         Column original table
     * @param ownerName
     *         Owner of the column/table
     */
    public FieldDescriptor(int position, DatatypeCoder datatypeCoder,
            int type, int subType, int scale, int length,
            @Nullable String fieldName, @Nullable String tableAlias,
            @Nullable String originalName, @Nullable String originalSchema, @Nullable String originalTableName,
            @Nullable String ownerName) {
        this.position = position;
        this.datatypeCoder = datatypeCoderForType(datatypeCoder, type, subType, scale);
        this.type = type;
        this.subType = subType;
        this.scale = scale;
        this.length = length;
        this.fieldName = fieldName;
        // Assign null if table alias is empty string
        // TODO May want to do the reverse, or handle this better; see FirebirdResultSetMetaData contract
        this.tableAlias = trimToNull(tableAlias);
        this.originalName = originalName;
        this.originalSchema = originalSchema;
        this.originalTableName = originalTableName;
        this.ownerName = ownerName;
    }

    /**
     * Constructor for metadata FieldDescriptor.
     * <p>
     * This constructor sets all string-fields {@code null}, and is primarily intended for testing purposes.
     * </p>
     *
     * @param position
     *         Position of this field (0-based), or {@code -1} if position is not known (e.g. for test code)
     * @param datatypeCoder
     *         Instance of DatatypeCoder to use when decoding column data (note that another instance may be derived
     *         internally, which then will be returned by {@link #getDatatypeCoder()})
     * @param type
     *         Column SQL type
     * @param subType
     *         Column subtype
     * @param scale
     *         Column scale
     * @param length
     *         Column defined length
     */
    public FieldDescriptor(int position, DatatypeCoder datatypeCoder, int type, int subType, int scale, int length) {
        this(position, datatypeCoder, type, subType, scale, length, null, null, null, null, null, null);
    }

    /**
     * The position of the field in the row or parameter set.
     * <p>
     * In general this should be equal to the position of this descriptor in {@link RowDescriptor}, but in some cases
     * (usually test code), it might be {@code -1} instead.
     * </p>
     *
     * @return The 0-based position of this field in the row or parameter set (or {@code -1} if unknown)
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return The {@link org.firebirdsql.gds.ng.DatatypeCoder} to use when decoding field data.
     */
    public DatatypeCoder getDatatypeCoder() {
        return datatypeCoder;
    }

    /**
     * @return The {@link org.firebirdsql.encodings.IEncodingFactory} for the associated connection.
     */
    public IEncodingFactory getEncodingFactory() {
        return datatypeCoder.getEncodingFactory();
    }

    /**
     * @return The Firebird type of this field
     */
    public int getType() {
        return type;
    }

    /**
     * @return The Firebird subtype of this field
     */
    public int getSubType() {
        return subType;
    }

    /**
     * @return The scale of this field
     */
    public int getScale() {
        return scale;
    }

    /**
     * @return The declared (maximum) length of this field
     */
    public int getLength() {
        return length;
    }

    /**
     * @return The (aliased) field name
     */
    public @Nullable String getFieldName() {
        return fieldName;
    }

    /**
     * @return The (aliased) table name
     */
    public @Nullable String getTableAlias() {
        return tableAlias;
    }

    /**
     * @return The original name of the field (e.g. the column name in the table)
     */
    public @Nullable String getOriginalName() {
        return originalName;
    }

    /**
     * @return The original schema ({@code null} if schemaless, e.g. Firebird 5.0 or older, or a column not backed by
     * a table)
     */
    public @Nullable String getOriginalSchema() {
        return originalSchema;
    }

    /**
     * @return The original table name
     */
    public @Nullable String getOriginalTableName() {
        return originalTableName;
    }

    /**
     * @return The owner
     */
    public @Nullable String getOwnerName() {
        return ownerName;
    }

    /**
     * @return {@code true} if the type is variable length (ie {@link org.firebirdsql.gds.ISCConstants#SQL_VARYING}).
     */
    public boolean isVarying() {
        return isFbType(SQL_VARYING);
    }

    /**
     * Check if the type of this field is the specified Firebird data type.
     * <p>
     * This method assumes the not-nullable data type is passed, on checking the nullable bit of {@link #getType()} is
     * set to {@code 0}.
     * </p>
     *
     * @param fbType
     *         One of the {@code SQL_} data type identifier values
     * @return {@code true} if the type is the same as the type of this field
     */
    public boolean isFbType(int fbType) {
        return (type & ~1) == fbType;
    }

    /**
     * @return {@code true} if this field is nullable.
     */
    public boolean isNullable() {
        return (type & 1) != 0;
    }

    /**
     * Determines if this is a db-key (RDB$DB_KEY) of a table.
     * <p>
     * NOTE: Technically it could also be a normal {@code CHAR CHARACTER SET OCTETS} column called {@code DB_KEY}.
     * </p>
     *
     * @return {@code true} if the field is a RDB$DB_KEY
     * @since 4.0
     */
    public boolean isDbKey() {
        return dbKey > 0 || dbKey == 0 && isDbKey0();
    }

    private boolean isDbKey0() {
        dbKey = ("DB_KEY".equals(originalName) && isFbType(SQL_TEXT) && (subType & 0xFF) == CS_BINARY)
                ? (byte) 1 : -1;
        return dbKey > 0;
    }

    /**
     * The length in characters of this field.
     * <p>
     * This takes into account the max bytes per character of the character set.
     * </p>
     *
     * @return Character length, or {@code -1} for non-character types (including blobs)
     */
    public int getCharacterLength() {
        return switch (type & ~1) {
            case SQL_TEXT, SQL_VARYING -> {
                int maxBytesPerChar = getDatatypeCoder().getEncodingDefinition().getMaxBytesPerChar();
                // In Firebird 1.5 and earlier, the CHAR(31) metadata columns are reported with a byte length of 31,
                // while UNICODE_FSS has maxBytesPerChar 3
                yield maxBytesPerChar > 1 && length % maxBytesPerChar == 0 ? length / maxBytesPerChar : length;
            }
            default -> -1;
        };
    }

    /**
     * Returns a type-specific coder for this datatype.
     * <p>
     * Primary intent is to handle character set conversion for char, varchar and blob sub_type text.
     * </p>
     *
     * @param datatypeCoder
     *         Datatype coder to use for obtaining the type-specific variant
     * @param type
     *         Firebird type code
     * @param subType
     *         Firebird subtype code
     * @param scale
     *         Scale
     * @return type-specific datatype coder
     */
    private static DatatypeCoder datatypeCoderForType(DatatypeCoder datatypeCoder, int type, int subType, int scale) {
        int characterSetId = getCharacterSetId(type, subType, scale);
        EncodingDefinition encodingDefinition = datatypeCoder.getEncodingFactory()
                .getEncodingDefinitionByCharacterSetId(characterSetId);
        return datatypeCoder.forEncodingDefinition(encodingDefinition);
    }

    /**
     * Determines the character set id (without collation id) for a combination of type, subtype and scale.
     *
     * @param type
     *         Firebird type code
     * @param subType
     *         Firebird subtype code
     * @param scale
     *         Firebird scale
     * @return Character set id for the type, if the type has no character set, than {@link ISCConstants#CS_dynamic}
     * is returned
     */
    private static int getCharacterSetId(int type, int subType, int scale) {
        return switch (type & ~1) {
            case SQL_TEXT, SQL_VARYING -> subType & 0xFF;
            case SQL_BLOB -> {
                if (subType == BLOB_SUB_TYPE_TEXT) {
                    yield scale & 0xFF;
                }
                // Assume binary/octets (instead of NONE)
                yield CS_BINARY;
            }
            // Technically not a character type, but assume connection character set
            default -> CS_dynamic;
        };
    }

    /**
     * Determines the character set id (without collation id).
     *
     * @return Character set id for the type, if the type has no character set, than {@link ISCConstants#CS_dynamic}
     * is returned
     */
    public int getCharacterSetId() {
        return getCharacterSetId(type, subType, scale);
    }

    /**
     * Limited equals that only checks if the data type in the provided field descriptor is the same as this descriptor.
     * <p>
     * The fields checked are:
     * <ul>
     * <li>type</li>
     * <li>subType</li>
     * <li>scale</li>
     * <li>length</li>
     * </ul>
     * </p>
     *
     * @param other
     *         Field descriptor to check
     * @return {@code true} when {@code other} is not null and has the same type definition as this instance,
     * {@code false} otherwise.
     */
    @SuppressWarnings("unused")
    public boolean typeEquals(@Nullable FieldDescriptor other) {
        return this == other
                || other != null
                && this.type == other.type
                && this.subType == other.subType
                && this.scale == other.scale
                && this.length == other.length;
    }

    /**
     * Padding to use for fields of this type.
     *
     * @return padding byte (generally 0x00, or 0x20 for non-binary character data).
     * @since 5
     */
    public byte getPaddingByte() {
        return switch (type & ~1) {
            case SQL_TEXT, SQL_VARYING -> {
                if (getCharacterSetId() == CS_BINARY) {
                    yield 0x00;
                }
                yield 0x20;
            }
            default -> 0x00;
        };
    }

    @Override
    public String toString() {
        // 180 - 124 literals in appendFieldDescriptor + 56 for values (estimated size)
        var sb = new StringBuilder(180);
        appendFieldDescriptor(sb);
        return sb.toString();
    }

    void appendFieldDescriptor(final StringBuilder sb) {
        sb.append("FieldDescriptor:[")
                .append("Position=").append(position)
                .append(",FieldName=").append(fieldName)
                .append(",TableAlias=").append(tableAlias)
                .append(",Type=").append(type)
                .append(",SubType=").append(subType)
                .append(",Scale=").append(scale)
                .append(",Length=").append(length)
                .append(",OriginalName=").append(originalName)
                .append(",OriginalTableName=").append(originalTableName)
                .append(",OwnerName=").append(ownerName)
                .append(']');
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FieldDescriptor other)) return false;
        return this.position == other.position
                && this.type == other.type
                && this.subType == other.subType
                && this.scale == other.scale
                && this.length == other.length
                && Objects.equals(this.fieldName, other.fieldName)
                && Objects.equals(this.tableAlias, other.tableAlias)
                && Objects.equals(this.originalName, other.originalName)
                && Objects.equals(this.originalTableName, other.originalTableName)
                && Objects.equals(this.ownerName, other.ownerName)
                && this.datatypeCoder.equals(other.datatypeCoder);
    }

    @Override
    public int hashCode() {
        // Depend on immutability to cache hashCode
        if (hash != 0) return hash;
        int newHash = Objects.hash(position, type, subType, scale, length, fieldName, tableAlias, originalName,
                originalTableName, ownerName);
        return hash = newHash != 0 ? newHash : 1;
    }

}
