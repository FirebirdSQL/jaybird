// SPDX-FileCopyrightText: Copyright 2018-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

/**
 * Implements the {@code LENGTH} JDBC escape.
 * <p>
 * The JDBC specification specifies <i>Number of characters in string, excluding trailing blanks</i>, we right-trim
 * ({@code TRIM(TRAILING FROM value)}) the string before passing the value to either {@code CHAR_LENGTH} or
 * {@code OCTETS_LENGTH}. As a result, the interpretation of what is a blank depends on the type of value. Is the value
 * a normal {@code (VAR)CHAR} (non-octets), then the blank is space (0x20), for a
 * {@code VAR(CHAR)CHARACTER SET OCTETS / (VAR)BINARY} the blank is NUL (0x00). This means that the optional
 * {@code CHARACTERS|OCTETS} parameter has no influence on which blanks are trimmed, but only whether we count
 * characters or bytes after trimming.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
final class LengthFunction implements SQLFunction {

    private static final PatternSQLFunction TRIM_TRAILING = new PatternSQLFunction("TRIM(TRAILING FROM {0})");
    private static final CharacterLengthFunction CHARACTER_LENGTH = new CharacterLengthFunction();

    @Override
    public String apply(String... parameters) throws FBSQLParseException {
        if (parameters.length < 1 || parameters.length > 2) {
            throw new FBSQLParseException(
                    "Expected 1 or 2 parameters for LENGTH, received " + parameters.length);
        }
        String[] clonedParameters = parameters.clone();
        clonedParameters[0] = TRIM_TRAILING.apply(parameters);
        return CHARACTER_LENGTH.apply(clonedParameters);
    }

}
