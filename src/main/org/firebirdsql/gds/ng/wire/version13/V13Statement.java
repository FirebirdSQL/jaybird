// SPDX-FileCopyrightText: Copyright 2015 Hajime Nakagami
// SPDX-FileCopyrightText: Copyright 2015-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
 * @author Mark Rotteveel
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

        final XdrInputStream xdrIn = getXdrIn();
        final int nullBitsLen = (rowDescriptor.getCount() + 7) / 8;
        final BitSet nullBits = BitSet.valueOf(xdrIn.readBuffer(nullBitsLen));

        for (int idx = 0; idx < rowDescriptor.getCount(); idx++) {
            rowValue.setFieldData(idx,
                    nullBits.get(idx) ? null : readColumnData(xdrIn, rowDescriptor.getFieldDescriptor(idx)));
        }
        return rowValue;
    }

    @Override
    protected void writeSqlData(XdrOutputStream xdrOut, RowDescriptor rowDescriptor, RowValue fieldValues,
            boolean useActualLength) throws IOException, SQLException {
        writeSqlData(xdrOut, getBlrCalculator(), rowDescriptor, fieldValues, useActualLength);
    }
    
    protected void writeSqlData(XdrOutputStream xdrOut, BlrCalculator blrCalculator, RowDescriptor rowDescriptor,
            RowValue fieldValues, boolean useActualLength) throws IOException, SQLException {
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
                final int len = useActualLength
                        ? blrCalculator.calculateIoLength(fieldDescriptor, buffer)
                        : blrCalculator.calculateIoLength(fieldDescriptor);
                writeColumnData(xdrOut, len, buffer, fieldDescriptor);
            }
        }
    }

    @Override
    public int getDefaultSqlInfoSize() {
        return 512 * 1024;
    }

    @Override
    public int getMaxSqlInfoSize() {
        // It can be higher: 0xFFFE_FFFF serverside, and Integer.MAX_VALUE - 8 due to Java array size limitations
        // 16 MiB is probably already excessive
        return 16 * 1024 * 1024;
    }

}
