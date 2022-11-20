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
package org.firebirdsql.jdbc.metadata;

import java.sql.DatabaseMetaData;

/**
 * Maps Firebird foreign key action to {@link java.sql.DatabaseMetaData} value.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
final class ForeignKeyActionMapping {

    private ForeignKeyActionMapping() {
        // no instances
    }

    /**
     * Maps a Firebird foreign key action name to the corresponding {@link java.sql.DatabaseMetaData} constant.
     *
     * @param firebirdActionName
     *         Firebird action name
     * @return database metadata constant value
     */
    static Integer mapAction(String firebirdActionName) {
        switch (firebirdActionName) {
        case "NO ACTION":
        // NOTE: Firebird has no "RESTRICT", however this mapping (to importedKeyNoAction) was also present in
        // the previous implementation, so preserving it just in case.
        case "RESTRICT":
            return DatabaseMetaData.importedKeyNoAction;
        case "CASCADE":
            return DatabaseMetaData.importedKeyCascade;
        case "SET NULL":
            return DatabaseMetaData.importedKeySetNull;
        case "SET DEFAULT":
            return DatabaseMetaData.importedKeySetDefault;
        default:
            return null;
        }
    }
}
