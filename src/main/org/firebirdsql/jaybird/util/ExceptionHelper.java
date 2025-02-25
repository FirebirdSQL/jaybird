// SPDX-FileCopyrightText: Copyright 2020-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import java.sql.SQLException;

/**
 * Helpers for exception handling
 *
 * @author Mark Rotteveel
 */
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
