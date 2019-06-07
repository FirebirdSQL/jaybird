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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;

import java.sql.SQLException;

import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;

/**
 * InfoProcessor to retrieve the (normal) execution plan of a statement.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class ExecutionPlanProcessor implements InfoProcessor<String> {

    /**
     * Plan information items
     */
    private static final byte[] DESCRIBE_PLAN_INFO_ITEMS = new byte[]{
            ISCConstants.isc_info_sql_get_plan,
            ISCConstants.isc_info_end
    };

    /**
     * Detailed plan information items
     */
    private static final byte[] DESCRIBE_EXPLAINED_PLAN_INFO_ITEMS = new byte[]{
            ISCConstants.isc_info_sql_explain_plan,
            ISCConstants.isc_info_end
    };

    private final FbStatement statement;

    public ExecutionPlanProcessor(FbStatement statement) {
        this.statement = statement;
    }

    @Override
    public String process(byte[] buffer) throws SQLException {
        if (buffer[0] == ISCConstants.isc_info_end) {
            return "";
        }

        if (buffer[0] == ISCConstants.isc_info_truncated) {
            buffer = statement.getSqlInfo(getDescribePlanInfoItems(), statement.getMaxSqlInfoSize());
            if (buffer[0] == ISCConstants.isc_info_truncated) {
                // TODO Throw truncation exception instead
                return null;
            }
        }

        if (buffer[0] != ISCConstants.isc_info_sql_get_plan && buffer[0] != ISCConstants.isc_info_sql_explain_plan) {
            // We only expect isc_info_sql_get_plan or isc_info_sql_explain_plan
            throw new FbExceptionBuilder().exception(ISCConstants.isc_infunk).toSQLException();
        }

        int len = iscVaxInteger2(buffer, 1);
        if (len > 1) {
            // Trimming, because first character is a linefeed (0x0A)
            // Not skipping to prevent (potential) encoding issues
            return statement.getDatabase().getEncoding().decodeFromCharset(buffer, 3, len).trim();
        } else {
            return "";
        }
    }

    /**
     * Get the byte array with the describe plan info items as supported by this processor, for use with
     * {@link FbStatement#getSqlInfo(byte[], int, InfoProcessor)} or {@link FbStatement#getSqlInfo(byte[], int)}.
     *
     * @return plan info items
     */
    public byte[] getDescribePlanInfoItems() {
        return DESCRIBE_PLAN_INFO_ITEMS.clone();
    }

    /**
     * Get the byte array with the describe detailed plan info items.
     *
     * @return detailed plan info items
     */
    public byte[] getDescribeExplainedPlanInfoItems() {
        return DESCRIBE_EXPLAINED_PLAN_INFO_ITEMS.clone();
    }
}
