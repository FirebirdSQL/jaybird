// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.util.InternalApi;

/**
 * Interface for reserved words checks.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@InternalApi
public interface ReservedWords {

    /**
     * Checks case-insensitively if the supplied token text is a reserved word.
     *
     * @param tokenText
     *         Token text to check
     * @return {@code true} if the token text is a reserved word, {@code false} otherwise
     */
    boolean isReservedWord(CharSequence tokenText);

}
