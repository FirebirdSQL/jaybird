// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Stored procedure argument.
 *
 * @since 7
 */
public abstract sealed class ProcedureArgument {

    private final String text;

    private ProcedureArgument(String text) {
        this.text = requireNonNull(text, "text");
    }

    /**
     * @return text of the procedure argument
     */
    public final String text() {
        return text;
    }

    /**
     * @return text of the procedure argument
     * @see #text()
     */
    @Override
    public final String toString() {
        return text;
    }

    @Override
    public final int hashCode() {
        return text.hashCode();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Equality is determined <em>only</em> by the argument text. Instances of any subclass of
     * {@code ProcedureArgument} with the same argument text are considered to be equal.
     * </p>
     *
     * @see #exactEquals(Object)
     */
    @Override
    public final boolean equals(Object obj) {
        return obj instanceof ProcedureArgument that && this.text.equals(that.text);
    }

    /**
     * Exact equals (primarily for testing purposes) to check if this object and {@code obj} are the same class and
     * are equal on all values (e.g. including parameter count).
     *
     * @param obj object to compare
     * @return {@code true} if {@code obj} is the same class and all its fields have the same values, otherwise
     * {@code false}
     */
    public abstract boolean exactEquals(Object obj);

    /**
     * Count of parameters in this argument.
     * <p>
     * The current implementation only counts positional parameters (question marks).
     * </p>
     *
     * @return number of (positional) parameters in the argument
     */
    public abstract int parameterCount();

    static ProcedureArgument of(List<Token> argumentTokens) {
        final int tokenCount = argumentTokens.size();
        if (tokenCount == 0) throw new IllegalArgumentException("argumentTokens must contain one or more tokens");
        var sb = new StringBuilder(Math.max(16, tokenCount * 5));
        int parameterCount = 0;
        int commentOrWhiteSpaceCount = 0;
        for (Token token : argumentTokens) {
            sb.append(token.textAsCharSequence());
            if (token.isWhitespaceOrComment()) {
                commentOrWhiteSpaceCount++;
            } else if (token instanceof PositionalParameterToken) {
                // NOTE: If question marks are ever going to be used for other syntax, this could be wrong
                parameterCount++;
            }
        }
        if (tokenCount == commentOrWhiteSpaceCount) {
            throw new IllegalArgumentException(
                    "argumentTokens must contain at least one token that is not a comment or whitespace");
        }
        String argumentText = sb.toString();
        if (parameterCount == 1 && commentOrWhiteSpaceCount + 1 == tokenCount) {
            return new PositionalParameter(argumentText);
        }
        return new Expression(argumentText, parameterCount);
    }

    /**
     * Procedure argument that is a bare positional parameter (bar comments or whitespace).
     */
    public static final class PositionalParameter extends ProcedureArgument {

        // package private for testing
        PositionalParameter(String text) {
            super(text);
        }

        @Override
        public int parameterCount() {
            return 1;
        }

        @Override
        public boolean exactEquals(Object obj) {
            return obj instanceof PositionalParameter that
                    && this.text().equals(that.text());
        }

    }

    /**
     * Any procedure argument that is not a bare positional parameter (see {@link PositionalParameter}).
     */
    public static final class Expression extends ProcedureArgument {

        private final int parameterCount;

        // package private for testing
        Expression(String text, int parameterCount) {
            super(text);
            this.parameterCount = parameterCount;
        }

        @Override
        public int parameterCount() {
            return parameterCount;
        }

        @Override
        public boolean exactEquals(Object obj) {
            return obj instanceof Expression that
                    && this.parameterCount == that.parameterCount
                    && this.text().equals(that.text());
        }

    }

}
