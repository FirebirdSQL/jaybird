// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-FileCopyrightText: Copyright 2019 Vasiliy Yashkov
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;

import java.sql.SQLException;

import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;

/**
 * InfoProcessor to retrieve the (normal) execution plan of a statement.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class ExecutionPlanProcessor implements InfoProcessor<String> {

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
                throw new InfoTruncatedException(
                        "Received isc_info_truncated after fallback to simple execution plan with maximum size", 0);
            }
        }

        if (buffer[0] != ISCConstants.isc_info_sql_get_plan && buffer[0] != ISCConstants.isc_info_sql_explain_plan) {
            // We only expect isc_info_sql_get_plan or isc_info_sql_explain_plan
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_unexpectedInfoResponse)
                    .messageParameter("sql", "isc_info_sql_get_plan or isc_info_sql_explain_plan",
                            ISCConstants.isc_info_sql_get_plan + " or " + ISCConstants.isc_info_sql_explain_plan,
                            buffer[0])
                    .toSQLException();
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
