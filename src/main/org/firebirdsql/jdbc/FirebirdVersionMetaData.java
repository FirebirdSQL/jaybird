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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.impl.GDSServerVersion;

import java.sql.SQLException;

/**
 * Version specific metadata information.
 * <p>
 * Be aware that some metadata is also derived from {@link org.firebirdsql.util.FirebirdSupportInfo}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
enum FirebirdVersionMetaData {
    // Order is intentionally from higher versions to lower versions, see getVersionMetaDataFor!
    FIREBIRD_4_0(4, 0) {
        @Override
        public String getSqlKeywords() {
            return "ADD,ADMIN,BIT_LENGTH,COMMENT,CURRENT_CONNECTION,CURRENT_TRANSACTION,DECFLOAT,DELETING,GDSCODE,"
                    + "INDEX,INSERTING,LONG,OFFSET,PLAN,POST_EVENT,RDB$DB_KEY,RDB$ERROR,RDB$GET_CONTEXT,"
                    + "RDB$GET_TRANSACTION_CN,RDB$RECORD_VERSION,RDB$ROLE_IN_USE,RDB$SET_CONTEXT,RDB$SYSTEM_PRIVILEGE,"
                    + "RECORD_VERSION,RECREATE,RETURNING_VALUES,ROW_COUNT,SQLCODE,UNBOUNDED,UPDATING,VARBINARY,"
                    + "VARIABLE,VIEW,WHILE";
        }
    },
    FIREBIRD_3_0(3, 0) {
        @Override
        public String getSqlKeywords() {
            return "ADD,ADMIN,BIT_LENGTH,CURRENT_CONNECTION,CURRENT_TRANSACTION,DELETING,GDSCODE,INDEX,INSERTING,LONG,"
                    + "OFFSET,PLAN,POST_EVENT,RDB$DB_KEY,RDB$RECORD_VERSION,RECORD_VERSION,RECREATE,RETURNING_VALUES,"
                    + "ROW_COUNT,SQLCODE,UPDATING,VARIABLE,VIEW,WHILE";
        }
    },
    FIREBIRD_2_5(2, 5) {
        @Override
        public String getSqlKeywords() {
            return "ADD,ADMIN,BIT_LENGTH,CURRENT_CONNECTION,CURRENT_TRANSACTION,GDSCODE,INDEX,LONG,MAXIMUM_SEGMENT,"
                    + "PLAN,POST_EVENT,RDB$DB_KEY,RECORD_VERSION,RECREATE,RETURNING_VALUES,ROW_COUNT,SQLCODE,VARIABLE,"
                    + "VIEW,WHILE";
        }
    },
    FIREBIRD_2_1(2, 1) {
        @Override
        public String getSqlKeywords() {
            return "ACTIVE,ADD,ADMIN,AFTER,ASC,ASCENDING,AUTO,BEFORE,BIT_LENGTH,COMMITTED,COMPUTED,CONDITIONAL,"
                    + "CONTAINING,CSTRING,CURRENT_CONNECTION,CURRENT_TRANSACTION,DATABASE,DEBUG,DESC,DESCENDING,DO,"
                    + "DOMAIN,ENTRY_POINT,EXCEPTION,EXIT,FILE,GDSCODE,GENERATOR,GEN_ID,IF,INACTIVE,INDEX,INPUT_TYPE,"
                    + "ISOLATION,KEY,LENGTH,LEVEL,LONG,MANUAL,MAXIMUM_SEGMENT,MODULE_NAME,NAMES,OPTION,OUTPUT_TYPE,"
                    + "OVERFLOW,PAGE,PAGES,PAGE_SIZE,PASSWORD,PLAN,POST_EVENT,PRIVILEGES,PROTECTED,RDB$DB_KEY,READ,"
                    + "RECORD_VERSION,RECREATE,RESERV,RESERVING,RETAIN,RETURNING_VALUES,ROW_COUNT,SCHEMA,SEGMENT,"
                    + "SHADOW,SHARED,SINGULAR,SIZE,SNAPSHOT,SORT,SQLCODE,STABILITY,STARTING,STARTS,STATISTICS,"
                    + "SUB_TYPE,SUSPEND,TRANSACTION,UNCOMMITTED,VARIABLE,VIEW,WAIT,WHILE,WORK,WRITE";
        }
    },
    FIREBIRD_2_0(2, 0) {
        @Override
        public String getSqlKeywords() {
            return "ACTIVE,ADD,ADMIN,AFTER,ASC,ASCENDING,AUTO,BEFORE,BIT_LENGTH,COMMITTED,COMPUTED,CONDITIONAL,"
                    + "CONTAINING,CSTRING,CURRENT_CONNECTION,CURRENT_TRANSACTION,DATABASE,DEBUG,DESC,DESCENDING,DO,"
                    + "DOMAIN,ENTRY_POINT,EXCEPTION,EXIT,FILE,GDSCODE,GENERATOR,GEN_ID,IF,INACTIVE,INDEX,INPUT_TYPE,"
                    + "ISOLATION,KEY,LENGTH,LEVEL,LONG,MANUAL,MAXIMUM_SEGMENT,MESSAGE,MODULE_NAME,NAMES,OPTION,"
                    + "OUTPUT_TYPE,OVERFLOW,PAGE,PAGES,PAGE_SIZE,PASSWORD,PLAN,POST_EVENT,PRIVILEGES,PROTECTED,"
                    + "RDB$DB_KEY,READ,RECORD_VERSION,RECREATE,RESERV,RESERVING,RETAIN,RETURNING_VALUES,ROW_COUNT,"
                    + "SCHEMA,SEGMENT,SHADOW,SHARED,SINGULAR,SIZE,SNAPSHOT,SORT,SQLCODE,STABILITY,STARTING,STARTS,"
                    + "STATISTICS,SUB_TYPE,SUSPEND,TRANSACTION,UNCOMMITTED,VARIABLE,VIEW,WAIT,WHILE,WORK,WRITE";
        }
    };

    private final int majorVersion;
    private final int minorVersion;

    FirebirdVersionMetaData(int majorVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    /**
     * Returns a comma-separated list of the reserved words that are not also reserved by SQL:2003.
     * <p>
     * NOTE: {@code DatabaseMetaData.getSQLKeywords()} talks about SQL keywords, but we are intentionally restricting
     * this to reserved words, and ignore non-standard non-reserved keywords as we believe that is closer to the intent
     * of {@code DatabaseMetaData.getSQLKeywords()}.
     * </p>
     *
     * @return comma-separated list of the reserved words
     * @see java.sql.DatabaseMetaData#getSQLKeywords()
     */
    abstract String getSqlKeywords();

    static FirebirdVersionMetaData getVersionMetaDataFor(GDSServerVersion version) {
        for (FirebirdVersionMetaData versionMetaData : values()) {
            if (version.isEqualOrAbove(versionMetaData.majorVersion, versionMetaData.minorVersion)) {
                return versionMetaData;
            }
        }
        // If no match, return lowest supported
        return FIREBIRD_2_0;
    }

    static FirebirdVersionMetaData getVersionMetaDataFor(FirebirdConnection connection) throws SQLException {
        return getVersionMetaDataFor(connection.getFbDatabase().getServerVersion());
    }
}
