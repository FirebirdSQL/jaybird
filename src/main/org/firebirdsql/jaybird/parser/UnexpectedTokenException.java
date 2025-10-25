// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import java.io.Serial;

/**
 * Used by some {@link TokenVisitor} implementations when it receives a token it did not expect.
 * <p>
 * Be aware that some token visitors may use other means of signalling this kind of parsing error.
 * </p>
 * <p>
 * Usage note: do not throw this from {@link TokenVisitor#visitToken(Token, VisitorRegistrar)} or
 * {@link TokenVisitor#complete(VisitorRegistrar)} as the parser will ignore exceptions thrown from those methods. Throw
 * it from methods that a user calls to obtain the result of a token visitor.
 * </p>
 *
 * @since 7
 */
public class UnexpectedTokenException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = -2350496918296695040L;

    private final Token token;

    public UnexpectedTokenException(String message, Token token) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

}
