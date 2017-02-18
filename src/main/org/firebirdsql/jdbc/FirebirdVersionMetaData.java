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
    FIREBIRD_3_0(3, 0) {
        @Override
        public String getSqlKeywords() {
            return "ADMIN,AVG,BIT_LENGTH,BOOLEAN,CHAR_LENGTH,CHARACTER_LENGTH,CHAR_LENGTH,CORR,COVAR_POP,COVAR_SAMP,"
                    + "COUNT,CURRENT_CONNECTION,CURRENT_TRANSACTION,DELETING,EXTRACT,GDSCODE,INDEX,INSERTING,LONG,"
                    + "LOWER,MAX,MIN,OCTET_LENGTH,OFFSET,PLAN,POSITION,POST_EVENT,RDB$DB_KEY,RDB$RECORD_VERSION,"
                    + "RECORD_VERSION,RECREATE,REGR_AVGX,REGR_AVGY,REGR_COUNT,REGR_INTERCEPT,REGR_R2,REGR_SLOPE,"
                    + "REGR_SXX,REGR_SXY,REGR_SYY,RETURNING_VALUES,ROW_COUNT,SQLCODE,STDDEV_POP,STDDEV_SAMP,SUM,TRIM,"
                    + "UPDATING,UPPER,VAR_POP,VAR_SAMP,VARIABLE,VIEW";
        }
    },
    FIREBIRD_2_5(2, 5) {
        @Override
        public String getSqlKeywords() {
            return "ADMIN,AVG,BIT_LENGTH,CHAR_LENGTH,CHARACTER_LENGTH,CHAR_LENGTH,COUNT,CURRENT_CONNECTION,"
                    + "CURRENT_TRANSACTION,EXTRACT,GDSCODE,INDEX,LONG,LOWER,MAX,MAXIMUM_SEGMENT,MIN,OCTET_LENGTH,PLAN,"
                    + "POSITION,POST_EVENT,RDB$DB_KEY,RECORD_VERSION,RECREATE,RETURNING_VALUES,ROW_COUNT,SQLCODE,SUM,"
                    + "TRIM,UPPER,VARIABLE,VIEW";
        }
    },
    FIREBIRD_2_1(2, 1) {
        @Override
        public String getSqlKeywords() {
            return "ACTIVE,ADMIN,AFTER,ASC,ASCENDING,AUTO,AVG,BEFORE,BIT_LENGTH,CHAR_LENGTH,CHARACTER_LENGTH,"
                    + "CHAR_LENGTH,COMMITTED,COMPUTED,CONDITIONAL,CONTAINING,COUNT,CSTRING,CURRENT_CONNECTION,"
                    + "CURRENT_TRANSACTION,DATABASE,DEBUG,DESC,DESCENDING,DOMAIN,ENTRY_POINT,EXCEPTION,EXTRACT,FILE,"
                    + "GDSCODE,GENERATOR,GEN_ID,INACTIVE,INDEX,INPUT_TYPE,ISOLATION,KEY,LENGTH,LEVEL,LONG,LOWER,MANUAL,"
                    + "MAX,MAXIMUM_SEGMENT,MIN,MODULE_NAME,NAMES,OCTET_LENGTH,OPTION,OUTPUT_TYPE,OVERFLOW,PAGE,PAGES,"
                    + "PAGE_SIZE,PASSWORD,PLAN,POSITION,POST_EVENT,PRIVILEGES,PROTECTED,RDB$DB_KEY,READ,RECORD_VERSION,"
                    + "RECREATE,RESERV,RESERVING,RETAIN,RETURNING_VALUES,ROW_COUNT,SCHEMA,SEGMENT,SHADOW,SHARED,"
                    + "SINGULAR,SIZE,SNAPSHOT,SORT,SQLCODE,STABILITY,STARTING,STARTS,STATISTICS,SUB_TYPE,SUM,SUSPEND,"
                    + "TRANSACTION,TRIM,UNCOMMITTED,UPPER,VARIABLE,VIEW,WAIT,WORK,WRITE";
        }
    },
    FIREBIRD_2_0(2, 0) {
        @Override
        public String getSqlKeywords() {
            return "ACTIVE,ADMIN,AFTER,ASC,ASCENDING,AUTO,AVG,BEFORE,BIT_LENGTH,CHAR_LENGTH,CHARACTER_LENGTH,"
                    + "CHAR_LENGTH,COMMITTED,COMPUTED,CONDITIONAL,CONTAINING,COUNT,CSTRING,CURRENT_CONNECTION,"
                    + "CURRENT_TRANSACTION,DATABASE,DEBUG,DESC,DESCENDING,DOMAIN,ENTRY_POINT,EXCEPTION,EXTRACT,FILE,"
                    + "GDSCODE,GENERATOR,GEN_ID,INACTIVE,INDEX,INPUT_TYPE,ISOLATION,KEY,LENGTH,LEVEL,LONG,LOWER,MANUAL,"
                    + "MAX,MAXIMUM_SEGMENT,MESSAGE,MIN,MODULE_NAME,NAMES,OCTET_LENGTH,OPTION,OUTPUT_TYPE,OVERFLOW,PAGE,"
                    + "PAGES,PAGE_SIZE,PASSWORD,PLAN,POSITION,POST_EVENT,PRIVILEGES,PROTECTED,RDB$DB_KEY,READ,"
                    + "RECORD_VERSION,RECREATE,RESERV,RESERVING,RETAIN,RETURNING_VALUES,ROW_COUNT,SCHEMA,SEGMENT,"
                    + "SHADOW,SHARED,SINGULAR,SIZE,SNAPSHOT,SORT,SQLCODE,STABILITY,STARTING,STARTS,STATISTICS,SUB_TYPE,"
                    + "SUM,SUSPEND,TRANSACTION,TRIM,UNCOMMITTED,UPPER,VARIABLE,VIEW,WAIT,WORK,WRITE";
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
