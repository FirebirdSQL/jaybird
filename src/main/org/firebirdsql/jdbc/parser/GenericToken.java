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
package org.firebirdsql.jdbc.parser;

import java.util.regex.Pattern;

/**
 * Any other token in the token stream that is not explicitly classified with a more specific type.
 * <p>
 * This includes non-reserved words, (non-quoted) identifiers, (some) boolean operators, (non-reserved) function names,
 * etc.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0.8
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
