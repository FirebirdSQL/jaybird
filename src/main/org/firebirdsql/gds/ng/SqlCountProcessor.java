/*
 * $Id$
 * 
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;

import java.sql.SQLException;

/**
 * Info processor for retrieving affected record count.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public class SqlCountProcessor implements InfoProcessor<SqlCountHolder> {

    private static final byte[] RECORD_COUNT_INFO_ITEMS = {
            ISCConstants.isc_info_sql_records,
            ISCConstants.isc_info_end
    };

    private final FbStatement statement;

    public SqlCountProcessor(FbStatement statement) {
        this.statement = statement;
    }

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
                final int countLength = statement.getDatabase().iscVaxInteger2(infoResponse, pos);
                pos += 2;
                final long count = statement.getDatabase().iscVaxLong(infoResponse, pos, countLength);
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
                    // TODO Log, throw exception?
                    break;
                }
                pos += countLength;
            }
            return new SqlCountHolder(updateCount, deleteCount, insertCount, selectCount);
            // TODO Handle isc_info_truncated, or do we simply assume we always use a sufficiently large buffer?
        } else {
            // TODO SQL state, better error?
            throw new FbExceptionBuilder().exception(ISCConstants.isc_infunk).toSQLException();
        }
    }

    public byte[] getRecordCountInfoItems() {
        return RECORD_COUNT_INFO_ITEMS.clone();
    }

}
