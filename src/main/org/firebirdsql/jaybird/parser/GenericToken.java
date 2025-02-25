// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import java.util.regex.Pattern;

/**
 * Any other token in the token stream that is not explicitly classified with a more specific type.
 * <p>
 * This includes non-reserved words, (non-quoted) identifiers, (some) boolean operators, (non-reserved) function names,
 * etc.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class GenericToken extends AbstractToken {

    private static final Pattern VALID_IDENTIFIER_PATTERN =
            Pattern.compile("[A-Z][A-Z0-9_$]*", Pattern.CASE_INSENSITIVE);

    @SuppressWarnings("unused")
    GenericToken(int pos, CharSequence src, int start, int end) {
        super(pos, src, start, end);
    }

    public GenericToken(int pos, CharSequence tokenText) {
        super(pos, tokenText);
    }

    @Override
    public boolean isValidIdentifier() {
        return VALID_IDENTIFIER_PATTERN.matcher(textAsCharSequence()).matches();
    }

}
