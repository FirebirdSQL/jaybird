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
 * can be obtained from a source repository history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;

import java.sql.SQLException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public abstract class AbstractFbStatement implements FbStatement {

    private static final int EXECUTION_PLAN_BUFFER_SIZE = 32768;

    /**
     * Plan information items
     */
    private static final byte[] DESCRIBE_PLAN_INFO_ITEMS = new byte[] {
            ISCConstants.isc_info_sql_get_plan
    };

    /**
     * Records affected items
     * TODO: Compare with current implementation
     */
    private static final byte[] ROWS_AFFECTED_INFO_ITEMS = new byte[] {
            ISCConstants.isc_info_sql_records
    };

    /**
     * Describe information items
     * TODO: isc_info_sql_relation_alias is not supported in Firebird 1.5 and earlier, take this into account by making it dependent on the protocol version?
     */
    private static final byte[] DESCRIBE_INFO_AND_BIND_INFO_ITEMS = new byte[] {
            ISCConstants.isc_info_sql_stmt_type,
            ISCConstants.isc_info_sql_select,
            ISCConstants.isc_info_sql_describe_vars,
            ISCConstants.isc_info_sql_sqlda_seq,
            ISCConstants.isc_info_sql_type, ISCConstants.isc_info_sql_sub_type,
            ISCConstants.isc_info_sql_scale, ISCConstants.isc_info_sql_length,
            ISCConstants.isc_info_sql_field,
            ISCConstants.isc_info_sql_alias,
            ISCConstants.isc_info_sql_relation,
            ISCConstants.isc_info_sql_relation_alias,
            ISCConstants.isc_info_sql_owner,
            ISCConstants.isc_info_sql_describe_end,

            ISCConstants.isc_info_sql_bind,
            ISCConstants.isc_info_sql_describe_vars,
            ISCConstants.isc_info_sql_sqlda_seq,
            ISCConstants.isc_info_sql_type, ISCConstants.isc_info_sql_sub_type,
            ISCConstants.isc_info_sql_scale, ISCConstants.isc_info_sql_length,
            ISCConstants.isc_info_sql_field,
            ISCConstants.isc_info_sql_alias,
            ISCConstants.isc_info_sql_relation,
            ISCConstants.isc_info_sql_relation_alias,
            ISCConstants.isc_info_sql_owner,
            ISCConstants.isc_info_sql_describe_end
    };

    protected byte[] getDescribePlanInfoItems() {
        return DESCRIBE_PLAN_INFO_ITEMS;
    }

    protected byte[] getRowsAffectedInfoItems() {
        return ROWS_AFFECTED_INFO_ITEMS;
    }

    protected byte[] getDescribeInfoAndBindInfoItems() {
        // TODO Make abstract and move into protocol specific version, see todo on DESCRIBE_INFO_AND_BIND_INFO_ITEMS
        return DESCRIBE_INFO_AND_BIND_INFO_ITEMS;
    }

    @Override
    public String getExecutionPlan() throws SQLException {
        return getSqlInfo(getDescribePlanInfoItems(), EXECUTION_PLAN_BUFFER_SIZE, new InfoProcessor<String>() {
            @Override
            public String process(final byte[] infoResponse) throws SQLException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    /**
     * Request statement info.
     *
     * @param requestItems
     *         Array of info items to request
     * @param bufferLength
     *         Response buffer length to use
     * @param infoProcessor
     *         Implementation of {@link InfoProcessor} to transform
     *         the info response
     * @return Transformed info response of type T
     * @throws SQLException
     *         For errors retrieving or transforming the response.
     */
    protected abstract <T> T getSqlInfo(byte[] requestItems, int bufferLength, InfoProcessor<T> infoProcessor) throws SQLException;

    protected abstract void free(int option);
}
