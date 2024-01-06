/*
 * Firebird Open Source JDBC Driver
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

import org.firebirdsql.gds.impl.GDSServerVersion;

import static org.firebirdsql.gds.ISCConstants.*;

/**
 * Strategy for handling server version specific information needs that do not depend on the wire protocol (or client
 * library) version, but on the Firebird server version.
 * <p>
 * Currently only contains information items for statement info requests.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
enum ServerVersionInformation {

    // IMPORTANT: Enums should be defined from low versions to high versions
    /**
     * Information for Version 1.0 and higher
     */
    VERSION_1_0(1, 0) {
        @Override
        public byte[] getStatementInfoRequestItems() {
            return Constants.V1_0_STATEMENT_INFO.clone();
        }

        @Override
        public byte[] getParameterDescriptionInfoRequestItems() {
            return Constants.V_1_0_PARAMETER_INFO.clone();
        }
    },
    /**
     * Information for Version 2.0 and higher
     */
    VERSION_2_0(2, 0) {
        @Override
        public byte[] getStatementInfoRequestItems() {
            return Constants.V_2_0_STATEMENT_INFO.clone();
        }

        @Override
        public byte[] getParameterDescriptionInfoRequestItems() {
            return Constants.V_2_0_PARAMETER_INFO.clone();
        }
    };

    private final int majorVersion;
    private final int minorVersion;

    ServerVersionInformation(int majorVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    /**
     * @return The (full) statement info request items.
     * @see #getParameterDescriptionInfoRequestItems()
     */
    public abstract byte[] getStatementInfoRequestItems();

    /**
     * TODO Do we actually need this separate from {@link #getStatementInfoRequestItems()}?
     *
     * @return The {@code isc_info_sql_describe_vars} info request items.
     * @see #getStatementInfoRequestItems()
     */
    public abstract byte[] getParameterDescriptionInfoRequestItems();

    /**
     * Convenience method to check if the majorVersion.minorVersion of this instance is equal to or smaller than the
     * specified version.
     *
     * @param majorVersion Required major version
     * @param minorVersion Required minor version
     * @return <code>true</code> when current majorVersion is smaller than required, or majorVersion is same and
     * minorVersion is equal to or smaller than required</code>
     */
    private boolean isEqualOrBelow(int majorVersion, int minorVersion) {
        return this.majorVersion < majorVersion ||
                (this.majorVersion == majorVersion && this.minorVersion <= minorVersion);
    }

    /**
     * Gets the instance with the highest version that is equal to or lower in version than the specified version.
     * <p>
     * If the specified version is too low, it will return {@link #VERSION_1_0}.
     * </p>
     *
     * @param majorVersion Required major version
     * @param minorVersion Required minor version
     * @return Instance
     * @see #getForVersion(org.firebirdsql.gds.impl.GDSServerVersion)
     */
    public static ServerVersionInformation getForVersion(int majorVersion, int minorVersion) {
        // NOTE: This depends on the contract of Enum.values() and the ordering of the definition of enums
        ServerVersionInformation highest = VERSION_1_0;
        for (ServerVersionInformation strategy : values()) {
            if (strategy.isEqualOrBelow(majorVersion, minorVersion)) {
                highest = strategy;
            } else {
                break;
            }
        }
        return highest;
    }

    /**
     * Gets the instance with the highest version that is equal to or lower in version than the specified version.
     * <p>
     * If the specified version is too low, it will return {@link #VERSION_1_0}.
     * </p>
     *
     * @param serverVersion Server version
     * @return Instance
     * @see #getForVersion(int, int)
     */
    public static ServerVersionInformation getForVersion(GDSServerVersion serverVersion) {
        return getForVersion(serverVersion.getMajorVersion(), serverVersion.getMinorVersion());
    }

    private static final class Constants {
        static final byte[] V1_0_STATEMENT_INFO = new byte[] {
                isc_info_sql_stmt_type,
                isc_info_sql_select,
                isc_info_sql_describe_vars,
                isc_info_sql_sqlda_seq,
                isc_info_sql_type, isc_info_sql_sub_type,
                isc_info_sql_scale, isc_info_sql_length,
                isc_info_sql_field,
                isc_info_sql_alias,
                isc_info_sql_relation,
                isc_info_sql_owner,
                isc_info_sql_describe_end,

                isc_info_sql_bind,
                isc_info_sql_describe_vars,
                isc_info_sql_sqlda_seq,
                isc_info_sql_type, isc_info_sql_sub_type,
                isc_info_sql_scale, isc_info_sql_length,
                // TODO: Information not available in normal queries, check for procedures, otherwise remove
                //isc_info_sql_field,
                //isc_info_sql_alias,
                //isc_info_sql_relation,
                //isc_info_sql_relation_alias,
                //isc_info_sql_owner,
                isc_info_sql_describe_end
        };
        static final byte[] V_1_0_PARAMETER_INFO = new byte[] {
                isc_info_sql_describe_vars,
                isc_info_sql_sqlda_seq,
                isc_info_sql_type, isc_info_sql_sub_type,
                isc_info_sql_scale, isc_info_sql_length,
                isc_info_sql_field,
                isc_info_sql_alias,
                isc_info_sql_relation,
                isc_info_sql_owner,
                isc_info_sql_describe_end
        };
        static final byte[] V_2_0_STATEMENT_INFO = new byte[] {
                isc_info_sql_stmt_type,
                isc_info_sql_select,
                isc_info_sql_describe_vars,
                isc_info_sql_sqlda_seq,
                isc_info_sql_type, isc_info_sql_sub_type,
                isc_info_sql_scale, isc_info_sql_length,
                isc_info_sql_field,
                isc_info_sql_alias,
                isc_info_sql_relation,
                isc_info_sql_relation_alias,
                isc_info_sql_owner,
                isc_info_sql_describe_end,

                isc_info_sql_bind,
                isc_info_sql_describe_vars,
                isc_info_sql_sqlda_seq,
                isc_info_sql_type, isc_info_sql_sub_type,
                isc_info_sql_scale, isc_info_sql_length,
                // TODO: Information not available in normal queries, check for procedures, otherwise remove
                //isc_info_sql_field,
                //isc_info_sql_alias,
                //isc_info_sql_relation,
                //isc_info_sql_relation_alias,
                //isc_info_sql_owner,
                isc_info_sql_describe_end
        };
        static final byte[] V_2_0_PARAMETER_INFO = new byte[] {
                isc_info_sql_describe_vars,
                isc_info_sql_sqlda_seq,
                isc_info_sql_type, isc_info_sql_sub_type,
                isc_info_sql_scale, isc_info_sql_length,
                isc_info_sql_field,
                isc_info_sql_alias,
                isc_info_sql_relation,
                isc_info_sql_relation_alias,
                isc_info_sql_owner,
                isc_info_sql_describe_end
        };

        private Constants() {
            // no instances
        }
    }
}
