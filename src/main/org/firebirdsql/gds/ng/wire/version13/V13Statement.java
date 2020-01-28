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
package org.firebirdsql.gds.ng.wire.version13;

import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.fields.*;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.version12.V12Statement;

import java.io.IOException;
import java.sql.SQLException;
import java.util.BitSet;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class V13Statement extends V12Statement {

    /**
     * Creates a new instance of V13Statement for the specified database.
     *
     * @param database
     *         FbWireDatabase implementation
     */
    public V13Statement(FbWireDatabase database) {
        super(database);
    }

    @Override
    protected RowValue readSqlData() throws SQLException, IOException {
        final RowDescriptor rowDescriptor = getRowDescriptor();
        final RowValue rowValue = rowDescriptor.createDefaultFieldValues();
        final BlrCalculator blrCalculator = getDatabase().getBlrCalculator();

        final XdrInputStream xdrIn = getXdrIn();
        final int nullBitsLen = (rowDescriptor.getCount() + 7) / 8;
        final byte[] nullBitsBytes = xdrIn.readRawBuffer(nullBitsLen);
        xdrIn.skipPadding(nullBitsLen);
        final BitSet nullBits = BitSet.valueOf(nullBitsBytes);

        for (int idx = 0; idx < rowDescriptor.getCount(); idx++) {
            final FieldDescriptor fieldDescriptor = rowDescriptor.getFieldDescriptor(idx);
            if (nullBits.get(idx)) {
                rowValue.setFieldData(idx, null);
            } else {
                final int len = blrCalculator.calculateIoLength(fieldDescriptor);
                final byte[] buffer = readColumnData(xdrIn, len);
                rowValue.setFieldData(idx, buffer);
            }
        }
        return rowValue;
    }

    @Override
    protected void writeSqlData(final RowDescriptor rowDescriptor, final RowValue fieldValues) throws IOException, SQLException {
        final XdrOutputStream xdrOut = getXdrOut();
        final BlrCalculator blrCalculator = getDatabase().getBlrCalculator();
        // null indicator bitmap
        final BitSet nullBits = new BitSet(fieldValues.getCount());
        for (int idx = 0; idx < fieldValues.getCount(); idx++) {
            nullBits.set(idx, fieldValues.getFieldData(idx) == null);
        }
        final byte[] nullBitsBytes = nullBits.toByteArray(); // Note only amount of bytes necessary for highest bit set
        xdrOut.write(nullBitsBytes);
        final int requiredBytes = (rowDescriptor.getCount() + 7) / 8;
        final int remainingBytes = requiredBytes - nullBitsBytes.length;
        if (remainingBytes > 0) {
            xdrOut.write(new byte[remainingBytes]);
        }
        xdrOut.writeAlignment(requiredBytes);

        for (int idx = 0; idx < fieldValues.getCount(); idx++) {
            if (!nullBits.get(idx)) {
                final byte[] buffer = fieldValues.getFieldData(idx);
                final FieldDescriptor fieldDescriptor = rowDescriptor.getFieldDescriptor(idx);
                final int len = blrCalculator.calculateIoLength(fieldDescriptor, buffer);
                final int fieldType = fieldDescriptor.getType();
                writeColumnData(xdrOut, len, buffer, fieldType);
            }
        }
    }
}
