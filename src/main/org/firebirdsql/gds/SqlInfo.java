 /*
 * Firebird Open Source J2ee connector - jdbc driver
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
package org.firebirdsql.gds;

import org.firebirdsql.gds.*;

/**
 * The class <code>SqlInfo</code> interprets return info from the server
 * into update, insert, and delete counts..
 *
 * @author <a href="mailto:brodsom@users.sourceforge.net">Blas Rodriguez Somoza</a>
 * @version 1.0
 */
public class SqlInfo {
    private int statementType;
    private int insertCount;
    private int updateCount;
    private int deleteCount;
    private int selectCount; //????

    public SqlInfo(byte[] buffer, GDS gds) {
        int pos = 0;
        int length;
        int type;
        while ((type = buffer[pos++]) != GDS.isc_info_end) {
            length = gds.isc_vax_integer(buffer, pos, 2);
            pos += 2;
            switch (type) {
            case GDS.isc_info_sql_records:
                int l;
                int t;
                while ((t = buffer[pos++]) != GDS.isc_info_end) {
                    l = gds.isc_vax_integer(buffer, pos, 2);
                    pos += 2;
                    switch (t) {
                    case GDS.isc_info_req_insert_count:
                        insertCount = gds.isc_vax_integer(buffer, pos, l);
                        break;
                    case GDS.isc_info_req_update_count:
                        updateCount = gds.isc_vax_integer(buffer, pos, l);
                        break;
                    case GDS.isc_info_req_delete_count:
                        deleteCount = gds.isc_vax_integer(buffer, pos, l);
                        break;
                    case GDS.isc_info_req_select_count:
                        selectCount = gds.isc_vax_integer(buffer, pos, l);
                        break;
                    default:
                        break;
                    }
                    pos += l;
                }
                break;
            case GDS.isc_info_sql_stmt_type:
                statementType = gds.isc_vax_integer(buffer, pos, length);
                pos += length;
                break;
            default:
                pos += length;
                break;
            }
        }
    }

    public int getStatementType() {
        return statementType;
    }

    public int getInsertCount() {
        return insertCount;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public int getDeleteCount() {
        return deleteCount;
    }

    public int getSelectCount() {
        return selectCount;
    }
}
