/*
 * $Id$
 *
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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.StatementState;
import org.firebirdsql.gds.ng.StatementType;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.fields.*;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.version12.V12Statement;

import java.io.IOException;
import java.sql.SQLException;
import java.util.BitSet;

import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

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
    public void execute(final RowValue parameters) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkStatementValid();
            checkTransactionActive(getTransaction());
            validateParameters(parameters);
            reset(false);

            final FbWireDatabase db = getDatabase();
            synchronized (db.getSynchronizationObject()) {
                // TODO Which state to switch to when an exception occurs (always ERROR might be wrong, see to do at start of class)
                switchState(StatementState.EXECUTING);
                final StatementType statementType = getType();
                int expectedResponseCount = 0;
                try {
                    if (statementType.isTypeWithSingletonResult()) {
                        expectedResponseCount++;
                    }
                    sendExecute(statementType.isTypeWithSingletonResult() ? WireProtocolConstants.op_execute2 : WireProtocolConstants.op_execute, parameters);
                    expectedResponseCount++;
                    getXdrOut().flush();
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                }

                final WarningMessageCallback statementWarningCallback = getStatementWarningCallback();
                try {
                    final boolean hasFields = getFieldDescriptor() != null && getFieldDescriptor().getCount() > 0;
                    try {
                        if (statementType.isTypeWithSingletonResult()) {
                            /* A type with a singleton result (ie an execute procedure), doesn't actually have a
                             * result set that will be fetched, instead we have a singleton result if we have fields
                             */
                            statementListenerDispatcher.statementExecuted(this, false, hasFields);
                            expectedResponseCount--;
                            processExecuteSingletonResponse(db.readSqlResponse(statementWarningCallback));
                            // TODO Do we need to set expectedResponseCount to 0 and exit if we get a cancelled error?
                            if (hasFields) {
                                setAllRowsFetched(true);
                            }
                        } else {
                            // A normal execute is never a singleton result (even if it only produces a single result)
                            statementListenerDispatcher.statementExecuted(this, hasFields, false);
                        }
                        expectedResponseCount--;
                        processExecuteResponse(db.readGenericResponse(statementWarningCallback));
                    } finally {
                        db.consumePackets(expectedResponseCount, getStatementWarningCallback());
                    }

                    if (getState() != StatementState.ERROR) {
                        switchState(statementType.isTypeWithCursor() ? StatementState.CURSOR_OPEN : StatementState.PREPARED);
                    }
                } catch (IOException ex) {
                    switchState(StatementState.ERROR);
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
                }

                /* Note contrary to V10 (and V11) we need to split retrieving update counts from the actual execute
                 * otherwise a cancel will not work.
                 */
                if (!statementType.isTypeWithCursor() && statementType.isTypeWithUpdateCounts()) {
                    getSqlCounts();
                }
            }
        }
    }

    /**
     * Reads a single row from the database.
     *
     * @return Row as a list of {@link FieldValue} instances
     * @throws SQLException
     * @throws IOException
     */
    protected RowValue readSqlData() throws SQLException, IOException {
        final RowDescriptor rowDescriptor = getFieldDescriptor();
        final RowValue rowValue = rowDescriptor.createDefaultFieldValues();
        final BlrCalculator blrCalculator = getDatabase().getBlrCalculator();

        synchronized (getDatabase().getSynchronizationObject()) {
            final XdrInputStream xdrIn = getXdrIn();
            final int nullBitsLen = (rowDescriptor.getCount() + 7) / 8;
            final byte[] nullBitsBytes = xdrIn.readRawBuffer(nullBitsLen);
            xdrIn.skipPadding(nullBitsLen);
            final BitSet nullBits = BitSet.valueOf(nullBitsBytes);

            for (int idx = 0; idx < rowDescriptor.getCount(); idx++) {
                final FieldDescriptor fieldDescriptor = rowDescriptor.getFieldDescriptor(idx);
                final FieldValue fieldValue = rowValue.getFieldValue(idx);
                if (nullBits.get(idx)) {
                    fieldValue.setFieldData(null);
                    continue;
                }
                int len = blrCalculator.calculateIoLength(fieldDescriptor);
                byte[] buffer;
                if (len == 0) {
                    // Length specified in response
                    len = xdrIn.readInt();
                    buffer = new byte[len];
                    xdrIn.readFully(buffer, 0, len);
                    xdrIn.skipPadding(len);
                } else if (len < 0) {
                    // Buffer is not padded
                    buffer = new byte[-len];
                    xdrIn.readFully(buffer, 0, -len);
                } else {
                    // len is incremented in calculateIoLength to avoid value 0 so it must be decremented
                    len--;
                    buffer = new byte[len];
                    xdrIn.readFully(buffer, 0, len);
                    xdrIn.skipPadding(len);
                }
                fieldValue.setFieldData(buffer);
            }
        }
        return rowValue;
    }

    /**
     * Write a set of SQL data from a list of {@link FieldValue} instances.
     *
     * @param rowDescriptor
     *         The row descriptor
     * @param fieldValues
     *         The List containing the SQL data to be written
     * @throws IOException
     *         if an error occurs while writing to the underlying output stream
     */
    protected void writeSqlData(final RowDescriptor rowDescriptor, final RowValue fieldValues) throws IOException, SQLException {
        synchronized (getDatabase().getSynchronizationObject()) {
            final XdrOutputStream xdrOut = getXdrOut();
            final BlrCalculator blrCalculator = getDatabase().getBlrCalculator();
            // null indicator bitmap
            final BitSet nullBits = new BitSet(fieldValues.getCount());
            for (int idx = 0; idx < fieldValues.getCount(); idx++) {
                final FieldValue fieldValue = fieldValues.getFieldValue(idx);
                nullBits.set(idx, fieldValue.getFieldData() == null);
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
                if (nullBits.get(idx)) {
                    continue;
                }
                final FieldValue fieldValue = fieldValues.getFieldValue(idx);
                final FieldDescriptor fieldDescriptor = rowDescriptor.getFieldDescriptor(idx);

                int len = blrCalculator.calculateIoLength(fieldDescriptor, fieldValue);
                final byte[] buffer = fieldValue.getFieldData();
                final int tempType = fieldDescriptor.getType() & ~1;

                // TODO Correctly pad with 0x00 instead of 0x20 for octets.
                if (tempType == ISCConstants.SQL_NULL) {
                    // Nothing to write for SQL_NULL (except null indicator, which happens at end)
                } else if (len == 0) {
                    if (buffer != null) {
                        len = buffer.length;
                        xdrOut.writeInt(len);
                        xdrOut.write(buffer, 0, len, (4 - len) & 3);
                    } else {
                        xdrOut.writeInt(0);
                    }
                } else if (len < 0) {
                    if (buffer != null) {
                        xdrOut.write(buffer, 0, -len);
                    } else {
                        xdrOut.writePadding(-len, 0x00); // TODO Used to be 0x20, check if use of 0x00 here is correct
                    }
                } else {
                    // decrement length because it was incremented before
                    // increment happens in BlrCalculator.calculateIoLength
                    len--;
                    if (buffer != null) {
                        final int buflen = buffer.length;
                        if (buflen >= len) {
                            xdrOut.write(buffer, 0, len, (4 - len) & 3);
                        } else {
                            xdrOut.write(buffer, 0, buflen, 0);
                            xdrOut.writePadding(len - buflen + ((4 - len) & 3), 0x20);
                        }
                    } else {
                        xdrOut.writePadding(len + ((4 - len) & 3), 0x20);
                    }
                }
            }
        }
    }

}
