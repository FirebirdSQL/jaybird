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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;

import java.sql.SQLException;

import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.gds.VaxEncoding.iscVaxLong;

/**
 * Info processor for retrieving affected record count.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class SqlCountProcessor implements InfoProcessor<SqlCountHolder> {

    private static final byte[] RECORD_COUNT_INFO_ITEMS = {
            ISCConstants.isc_info_sql_records,
            ISCConstants.isc_info_end
    };

    @Override
    public SqlCountHolder process(byte[] infoResponse) throws SQLException {
        assert infoResponse.length > 0 : "Information response buffer should be non-zero length";
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
            // TODO Check necessity of checking this, seems to happen with DDL
            // TODO Return all -1 instead?
            return new SqlCountHolder(0, 0, 0, 0);
        } else {
            // TODO SQL state, better error?
            throw FbExceptionBuilder.forException(ISCConstants.isc_infunk)
                    .messageParameter(infoResponse[0])
                    .toSQLException();
        }
    }

    public byte[] getRecordCountInfoItems() {
        return RECORD_COUNT_INFO_ITEMS.clone();
    }

}
