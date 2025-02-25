// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.util.InternalApi;

/**
 * Thrown when the tokenizer required a character, but instead the end of input was reached.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@InternalApi
public class UnexpectedEndOfInputException extends RuntimeException {

    private static final long serialVersionUID = 5393338512797009183L;

    public UnexpectedEndOfInputException(String message) {
        super(message);
    }

}
