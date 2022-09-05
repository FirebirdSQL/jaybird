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
package org.firebirdsql.util;

import java.sql.SQLException;

/**
 * Helpers for exception handling
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@InternalApi
public final class ExceptionHelper {

    private ExceptionHelper() {
        // no instances
    }

    /**
     * Iterates over the {@code SQLException} and concatenates all messages from the exception, its causes, and next
     * exceptions and their causes.
     * <p>
     * The messages are produced using {@link Throwable#toString()}, so they include the name of the exception.
     * </p>
     *
     * @param sqlException starting exception
     * @return All exception messages concatenated using new line
     */
    public static String collectAllMessages(SQLException sqlException) {
        StringBuilder sb = new StringBuilder();
        for (Throwable exception : sqlException) {
            sb.append(exception);
            sb.append('\n');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

}
