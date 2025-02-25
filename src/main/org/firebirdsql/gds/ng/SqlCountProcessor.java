// SPDX-FileCopyrightText: Copyright 2013-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;

import java.sql.SQLException;

import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.gds.VaxEncoding.iscVaxLong;

/**
 * Info processor for retrieving affected record count.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class SqlCountProcessor implements InfoProcessor<SqlCountHolder> {

    private static final byte[] RECORD_COUNT_INFO_ITEMS = {
            ISCConstants.isc_info_sql_records,
            ISCConstants.isc_info_end
    };

    @Override
    public SqlCountHolder process(byte[] infoResponse) throws SQLException {
        if (infoResponse.length == 0) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_infoResponseEmpty)
                    .messageParameter("sql")
                    .toSQLException();
        }
        int pos = 0;
        if (infoResponse[pos++] == ISCConstants.isc_info_sql_records) {
            // Skipping info size
            pos += 2;
            long updateCount = 0;
            long insertCount = 0;
            long deleteCount = 0;
            long selectCount = 0;
            int t;
            while ((t = infoResponse[pos++]) != ISCConstants.isc_info_end) {
                final int countLength = iscVaxInteger2(infoResponse, pos);
                pos += 2;
                final long count = iscVaxLong(infoResponse, pos, countLength);
                switch (t) {
                case ISCConstants.isc_info_req_select_count:
                    selectCount = count;
                    break;
                case ISCConstants.isc_info_req_insert_count:
                    insertCount = count;
                    break;
                case ISCConstants.isc_info_req_update_count:
                    updateCount = count;
                    break;
                case ISCConstants.isc_info_req_delete_count:
                    deleteCount = count;
                    break;
                default:
                    throw FbExceptionBuilder.forException(ISCConstants.isc_infunk)
                            .messageParameter(t)
                            .toSQLException();
                }
                pos += countLength;
            }
            return new SqlCountHolder(updateCount, deleteCount, insertCount, selectCount);
            // NOTE: we assume we always use a sufficiently large buffer
        } else if (infoResponse[0] == ISCConstants.isc_info_end) {
            // Happens with statement types that don't have update counts, etc (eg DDL)
            return SqlCountHolder.empty();
        } else {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_unexpectedInfoResponse)
                    .messageParameter("sql", "isc_info_sql_records or isc_info_end",
                            ISCConstants.isc_info_sql_records + " or " + ISCConstants.isc_info_end, infoResponse[0])
                    .toSQLException();
        }
    }

    public byte[] getRecordCountInfoItems() {
        return RECORD_COUNT_INFO_ITEMS.clone();
    }

}
