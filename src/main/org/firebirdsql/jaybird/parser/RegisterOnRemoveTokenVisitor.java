// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import static java.lang.System.Logger.Level.TRACE;

/**
 * Decorating token visitor that will register other token visitors when the decorated visitor removes itself.
 * <p>
 * Optionally, it can also post the last token consumed by the decorated token visitor to those newly registered token
 * visitors. This can be used if the decorated visitor can only detect its completion by consuming the next token,
 * while the newly registered visitors also need to consume that token. In general, implementations of token visitors
 * ignore whitespace and comments, so this notified token is unlikely to be whitespace or a comment.
 * </p>
 * <p>
 * If {@link #complete(VisitorRegistrar)} is notified, the completion is forwarded to the other token visitors, but
 * those visitors are <em>not</em> registered (unless the decorated visitor removes itself on completion).
 * </p>
 * <p>
 * Recommended use is through {@link TokenVisitor#onRemoveRegister(Collection)} instead of creating this class directly.
 * </p>
 *
 * @since 7
 */
final class RegisterOnRemoveTokenVisitor<T extends TokenVisitor> implements TokenVisitor, VisitorRegistrar {

    private static final WhitespaceToken DUMMY_NULL_TOKEN = new WhitespaceToken(0, " ");

    private final T decoratedTokenVisitor;
    private final Collection<TokenVisitor> registerOnRemove;
    private final boolean notifyLastToken;
    private Token lastTokenSeen = DUMMY_NULL_TOKEN;
    // Self-removal of the decorated visitor means afterRemove may be invoked during visitToken or complete
    private final Deque<VisitorRegistrar> registrarStack = new ArrayDeque<>(2);

    /**
     * Creates a token visitor that registers other token visitors on self-removal of the decorated
     * {@code tokenVisitor}.
     * <p>
     * The last consumed token will not be notified. If token notification is needed, use
     * {@link #RegisterOnRemoveTokenVisitor(TokenVisitor, Collection, boolean)} with {@code true}.
     * </p>
     *
     * @param tokenVisitor
     *         token visitor decorated by this instance
     * @param registerOnRemove
     *         token visitors to register on self-removal of {@code tokenVisitor} (the collection is used directly)
     * @see #RegisterOnRemoveTokenVisitor(TokenVisitor, Collection, boolean)
     */
    RegisterOnRemoveTokenVisitor(T tokenVisitor, Collection<TokenVisitor> registerOnRemove) {
        this(tokenVisitor, registerOnRemove, false);
    }

    /**
     * Creates a token visitor that registers other token visitors on self-removal of the decorated
     * {@code tokenVisitor}.
     *
     * @param tokenVisitor
     *         token visitor decorated by this instance
     * @param registerOnRemove
     *         token visitors to register on self-removal of {@code tokenVisitor}
     * @param notifyLastToken
     *         if {@code true}, after visitors in {@code registerOnRemove} have been registered, they will be notified
     *         of the last token consumed by {@code tokenVisitor}
     */
    RegisterOnRemoveTokenVisitor(T tokenVisitor, Collection<TokenVisitor> registerOnRemove,
            boolean notifyLastToken) {
        decoratedTokenVisitor = tokenVisitor;
        this.registerOnRemove = registerOnRemove;
        this.notifyLastToken = notifyLastToken;
    }

    @Override
    public void visitToken(Token token, VisitorRegistrar visitorRegistrar) {
        lastTokenSeen = token;
        registrarStack.push(visitorRegistrar);
        try {
            decoratedTokenVisitor.visitToken(token, this);
        } finally {
            registrarStack.pop();
        }
    }

    @Override
    public void complete(VisitorRegistrar visitorRegistrar) {
        lastTokenSeen = DUMMY_NULL_TOKEN;
        registrarStack.push(visitorRegistrar);
        try {
            decoratedTokenVisitor.complete(this);
            for (TokenVisitor visitor : registerOnRemove) {
                try {
                    visitor.complete(visitorRegistrar);
                } catch (Exception e) {
                    System.getLogger(getClass().getName()).log(TRACE,
                            () -> "Ignored exception notifying visitor %s of completion".formatted(visitor), e);
                }
            }
        } finally {
            registrarStack.pop();
        }
    }

    @Override
    public void addVisitor(TokenVisitor tokenVisitor) {
        currentRegistrar().addVisitor(tokenVisitor);
    }

    @Override
    public void removeVisitor(TokenVisitor tokenVisitor) {
        currentRegistrar().removeVisitor(tokenVisitor != decoratedTokenVisitor ? tokenVisitor : this);
    }

    @Override
    public void afterRemove(VisitorRegistrar visitorRegistrar) {
        registrarStack.push(visitorRegistrar);
        try {
            registerOnRemove.forEach(visitorRegistrar::addVisitor);
            if (notifyLastToken && lastTokenSeen != DUMMY_NULL_TOKEN) {
                registerOnRemove.forEach(visitor -> {
                    try {
                        // Notification is done with the real registrar
                        visitor.visitToken(lastTokenSeen, visitorRegistrar);
                    } catch (RuntimeException e) {
                        System.getLogger(getClass().getName()).log(TRACE,
                                () -> "Ignored exception notifying visitor %s of afterRemove".formatted(visitor), e);
                    }
                });
            }
        } finally {
            registrarStack.pop();
        }
    }

    private VisitorRegistrar currentRegistrar() {
        return registrarStack.isEmpty() ? VisitorRegistrar.noActionRegistrar() : registrarStack.peek();
    }

}
