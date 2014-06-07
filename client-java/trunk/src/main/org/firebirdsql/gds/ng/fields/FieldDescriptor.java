/*
 * $Id$
 *
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.gds.ng.fields;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.util.ObjectUtils;

/**
 * The class <code>FieldDescriptor</code> contains the column metadata of the XSQLVAR server
 * data structure used to describe one column for input or output.
 * <p>
 * FieldDescriptor is an immutable type, the value of a field is maintained separately in an instance of {@link FieldValue}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 2.3
 */
public final class FieldDescriptor {

    private final int type;
    private final int subType;
    private final int scale;
    private final int length;
    private final String fieldName;
    private final String tableAlias;
    private final String originalName;
    private final String originalTableName;
    private final String ownerName;
    private int hash;

    /**
     * Constructor for metadata FieldDescriptor.
     *
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
     * @param originalTableName
     *         Column original table
     * @param ownerName
     *         Owner of the column
     */
    public FieldDescriptor(int type, final int subType, final int scale, int length, final String fieldName, final String tableAlias, String originalName,
                           String originalTableName, final String ownerName) {
        this.type = type;
        this.subType = subType;
        this.scale = scale;
        this.length = length;
        this.fieldName = fieldName;
        this.tableAlias = tableAlias;
        this.originalName = originalName;
        this.originalTableName = originalTableName;
        this.ownerName = ownerName;
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
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @return The (aliased) table name
     */
    public String getTableAlias() {
        return tableAlias;
    }

    /**
     * @return The original name of the field (eg the column name in the table)
     */
    public String getOriginalName() {
        return originalName;
    }

    /**
     * @return The original table name
     */
    public String getOriginalTableName() {
        return originalTableName;
    }

    /**
     * @return The owner
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * Creates a default, uninitialized {@link FieldValue}
     *
     * @return A new {@link FieldValue}
     */
    public FieldValue createDefaultFieldValue() {
        return new FieldValue(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendFieldDescriptor(sb);
        return sb.toString();
    }

    StringBuilder appendFieldDescriptor(final StringBuilder sb) {
        sb.append("FieldDescriptor:[")
                .append("FieldName=").append(getFieldName())
                .append(",TableAlias=").append(getTableAlias())
                .append(",Type=").append(getType())
                .append(",SubType=").append(getSubType())
                .append(",Scale=").append(getScale())
                .append(",Length=").append(getLength())
                .append(",OriginalName=").append(getOriginalName())
                .append(",OriginalTableName=").append(getOriginalTableName())
                .append(",OwnerName=").append(getOwnerName())
                .append(']');
        return sb;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FieldDescriptor)) return false;
        FieldDescriptor other = (FieldDescriptor) obj;
        return this.type == other.type
                && this.subType == other.subType
                && this.scale == other.scale
                && this.length == other.length
                && ObjectUtils.equals(this.fieldName, other.fieldName)
                && ObjectUtils.equals(this.tableAlias, other.tableAlias)
                && ObjectUtils.equals(this.originalName, other.originalName)
                && ObjectUtils.equals(this.originalTableName, other.originalTableName)
                && ObjectUtils.equals(this.ownerName, other.ownerName);
    }

    @Override
    public int hashCode() {
        // Depend on immutability to cache hashCode
        if (hash == 0) {
            hash = ObjectUtils.hash(type, subType, scale, length, fieldName, tableAlias, originalName,
                    originalTableName, ownerName);
        }
        return hash;
    }

    /**
     * Temporary measure to derive an {@link org.firebirdsql.gds.XSQLVAR} from this FieldDescriptor.
     * <p>
     * TODO Remove this method before 3.0 release
     * </p>
     *
     * @return XSQLVAR derived from this FieldDescriptor
     */
    @Deprecated
    public XSQLVAR toXSQLVAR() {
        XSQLVAR xsqlvar = new XSQLVAR(getType(), getLength(), getOriginalName(), getOriginalTableName());
        xsqlvar.sqlscale = getScale();
        xsqlvar.sqlsubtype = getSubType();
        xsqlvar.relaliasname = getTableAlias();
        xsqlvar.ownname = getOwnerName();
        xsqlvar.aliasname = getFieldName();
        return xsqlvar;
    }
}
