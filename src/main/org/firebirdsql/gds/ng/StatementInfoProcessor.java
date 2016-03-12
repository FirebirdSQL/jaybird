/*
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a source repository history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.SQLException;

import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;

/**
 * InfoProcessor to retrieve the statement information associated with {@link org.firebirdsql.gds.ng.AbstractFbStatement#getStatementInfoRequestItems()}
 * and {@link org.firebirdsql.gds.ng.AbstractFbStatement#getParameterDescriptionInfoRequestItems()}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class StatementInfoProcessor implements InfoProcessor<InfoProcessor.StatementInfo> {

    private static final Logger log = LoggerFactory.getLogger(StatementInfoProcessor.class);

    private final AbstractFbStatement statement;
    private final FbDatabase database;

    /**
     * Creates an instance of this class.
     *
     * @param statement
     *         Statement object
     * @param database
     *         Database object
     */
    public StatementInfoProcessor(final AbstractFbStatement statement, final FbDatabase database) {
        this.statement = statement;
        this.database = database;
    }

    @Override
    public InfoProcessor.StatementInfo process(final byte[] infoResponse) throws SQLException {
        final StatementInfo info = new StatementInfo(statement.getDefaultSqlInfoSize(), infoResponse);

        while ((info.currentItem = info.buffer[info.currentIndex++]) != ISCConstants.isc_info_end) {
            switch (info.currentItem) {
            case ISCConstants.isc_info_sql_stmt_type:
                info.statementType = StatementType.valueOf(readIntValue(info));
                break;

            case ISCConstants.isc_info_truncated:
                handleTruncatedInfo(info);
                break;

            case ISCConstants.isc_info_sql_select:
            case ISCConstants.isc_info_sql_bind:
                if (info.buffer[info.currentIndex] == ISCConstants.isc_info_truncated) {
                    // break to get normal handling of isc_info_truncated
                    break;
                }
                handleDescriptors(info);
                break;

            default:
                log.debug(String.format("Unexpected item type %d", info.currentItem));
                throw new FbExceptionBuilder().exception(ISCConstants.isc_dsql_sqlda_err).toSQLException();
            }
        }
        return info;
    }

    /**
     * Handles info buffer truncation by requesting new information
     *
     * @param info
     *         StatementInfo
     * @throws SQLException
     */
    private void handleTruncatedInfo(final StatementInfo info) throws SQLException {
        final byte[] originalInfo = statement.getStatementInfoRequestItems();
        // Adding 2 * 4 bytes for the isc_info_sql_sqlda_start item (see handling of isc_info_sql_select and isc_info_sql_bind)
        final byte[] newInfoItems = new byte[originalInfo.length + 2 * 4];
        int newIndex = 0;
        for (final byte infoItem : originalInfo) {
            assert newIndex < newInfoItems.length : "newInfoItems size too short";
            switch (infoItem) {
            case ISCConstants.isc_info_sql_select:
            case ISCConstants.isc_info_sql_bind:
                final RowDescriptorBuilder currentBuilder =
                        infoItem == ISCConstants.isc_info_sql_select ? info.fieldBuilder : info.parameterBuilder;
                // Index of first descriptor to request; adding 1 to descriptor index as Firebird uses 1-based index for fields/parameters and builders are 0-based
                final int descriptorIndex =
                        currentBuilder != null ? currentBuilder.getFirstUnprocessedIndex() + 1 : 1;
                // Request server to resend info starting at the specified index of the fields or parameters
                newInfoItems[newIndex++] = ISCConstants.isc_info_sql_sqlda_start;
                newInfoItems[newIndex++] = 2; // size of short
                newInfoItems[newIndex++] = (byte) (descriptorIndex & 0xFF);
                newInfoItems[newIndex++] = (byte) (descriptorIndex >> 8);
                newInfoItems[newIndex++] = infoItem;
                break;
            default:
                newInfoItems[newIndex++] = infoItem;
                break;
            }
        }
        assert newIndex == newInfoItems.length : "newInfoItems size too long";
        // Doubling request buffer up to the maximum
        info.requestBufferSize = Math.min(2 * info.requestBufferSize, statement.getMaxSqlInfoSize());
        info.buffer = statement.getSqlInfo(newInfoItems, info.requestBufferSize);
        info.currentIndex = 0;
    }

    /**
     * Handles a bind description (for <tt>isc_info_sql_select</tt> or <tt>isc_info_sql_bind</tt>.
     *
     * @param info
     *         StatementInfo
     */
    private void handleDescriptors(final StatementInfo info) throws SQLException {
        // Skipping isc_info_sql_num_variables item
        // TODO Check if it is isc_info_sql_num_variables?
        info.currentIndex++;
        int descriptorCount = readIntValue(info);
        if (descriptorCount == 0) {
            return;
        }
        switch (info.currentItem) {
        case ISCConstants.isc_info_sql_select:
            if (info.fieldBuilder == null) {
                info.fieldBuilder = new RowDescriptorBuilder(descriptorCount, database.getDatatypeCoder());
            }
            processDescriptors(info, info.fieldBuilder);
            break;
        case ISCConstants.isc_info_sql_bind:
            if (info.parameterBuilder == null) {
                info.parameterBuilder = new RowDescriptorBuilder(descriptorCount, database.getDatatypeCoder());
            }
            processDescriptors(info, info.parameterBuilder);
            break;
        }
    }

    /**
     * Process descriptor info into the supplied {@link RowDescriptorBuilder}.
     *
     * @param info
     *         StatementInfo
     * @param rdb
     *         RowDescriptorBuilder
     */
    private void processDescriptors(final StatementInfo info, final RowDescriptorBuilder rdb) throws SQLException {
        int fieldCount = rdb.getFirstUnprocessedIndex();
        // If all fields have already been processed this stops processing immediately
        while (fieldCount < rdb.getSize()) {
            info.currentItem = info.buffer[info.currentIndex++];
            switch (info.currentItem) {
            case ISCConstants.isc_info_sql_sqlda_seq:
                // isc_info_sql_sqlda_seq is 1-based, builder uses 0-based index
                rdb.setFieldIndex(readIntValue(info) - 1);
                break;

            case ISCConstants.isc_info_sql_type:
                rdb.setType(readIntValue(info));
                break;

            case ISCConstants.isc_info_sql_sub_type:
                rdb.setSubType(readIntValue(info));
                break;

            case ISCConstants.isc_info_sql_scale:
                rdb.setScale(readIntValue(info));
                break;

            case ISCConstants.isc_info_sql_length:
                rdb.setLength(readIntValue(info));
                break;

            case ISCConstants.isc_info_sql_field:
                rdb.setOriginalName(readStringValue(info));
                break;

            case ISCConstants.isc_info_sql_alias:
                rdb.setFieldName(readStringValue(info));
                break;

            case ISCConstants.isc_info_sql_relation:
                rdb.setOriginalTableName(readStringValue(info));
                break;

            case ISCConstants.isc_info_sql_relation_alias:
                rdb.setTableAlias(readStringValue(info));
                break;

            case ISCConstants.isc_info_sql_owner:
                rdb.setOwnerName(readStringValue(info));
                break;

            case ISCConstants.isc_info_sql_describe_end:
                rdb.addField();
                fieldCount++;
                break;

            case ISCConstants.isc_info_truncated:
                // Clear current field data from the builder
                rdb.resetField();
                // Rewind index so isc_info_truncated can be handled in process(byte[]).
                info.currentIndex--;
                return;

            default:
                log.debug(String.format("Unexpected item type %d", info.currentItem));
                throw new FbExceptionBuilder().exception(ISCConstants.isc_dsql_sqlda_err).toSQLException();
            }
        }
    }

    private int readIntValue(StatementInfo info) {
        int len = iscVaxInteger2(info.buffer, info.currentIndex);
        info.currentIndex += 2;
        int value = iscVaxInteger(info.buffer, info.currentIndex, len);
        info.currentIndex += len;
        return value;
    }

    private String readStringValue(StatementInfo info) {
        int len = iscVaxInteger2(info.buffer, info.currentIndex);
        info.currentIndex += 2;
        // TODO use correct characterset
        String value = new String(info.buffer, info.currentIndex, len);
        info.currentIndex += len;
        return value;
    }

    /**
     * Class for holding values about the statement
     */
    private final class StatementInfo implements InfoProcessor.StatementInfo {
        private int requestBufferSize;
        private int currentIndex;
        private byte currentItem;
        private StatementType statementType = StatementType.NONE;
        private RowDescriptorBuilder fieldBuilder;
        private RowDescriptorBuilder parameterBuilder;
        private byte[] buffer;

        private StatementInfo(int initialRequestBufferSize, byte[] initialBuffer) {
            requestBufferSize = initialRequestBufferSize;
            buffer = initialBuffer;
        }

        @Override
        public StatementType getStatementType() {
            return statementType;
        }

        @Override
        public RowDescriptor getFields() {
            return fieldBuilder != null ? fieldBuilder.toRowDescriptor() : database.emptyRowDescriptor();
        }

        @Override
        public RowDescriptor getParameters() {
            return parameterBuilder != null ? parameterBuilder.toRowDescriptor() : database.emptyRowDescriptor();
        }
    }
}
