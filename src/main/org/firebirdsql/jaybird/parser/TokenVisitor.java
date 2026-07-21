// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import java.util.Collection;
import java.util.List;

/**
 * Visitor for tokens.
 * <p>
 * Used by {@link SqlParser} to notify the visitors of tokens.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
public interface TokenVisitor {

    /**
     * Notifies the visitor of a token.
     *
     * @param token
     *         Token
     * @param visitorRegistrar
     *         visitor registrar (can be used to remove itself, or add other visitors); the registrar is only guaranteed
     *         to be valid for the duration of this method call
     */
    void visitToken(Token token, VisitorRegistrar visitorRegistrar);

    /**
     * Signals that the last token was produced and the statement text was fully parsed.
     * <p>
     * The default implementation does nothing.
     * </p>
     *
     * @param visitorRegistrar
     *         visitor registrar (can be used to remove itself, or add/remove other visitors); the registrar is only
     *         guaranteed to be valid for the duration of this method call
     */
    default void complete(VisitorRegistrar visitorRegistrar) {
        // do nothing
    }

    /**
     * Notification that this visitor was removed from {@code visitorRegistrar}.
     * <p>
     * Can be used for clean-up or additional completion actions. Implementations overriding this method may want to
     * consider calling it from {@link #complete(VisitorRegistrar)} as well.
     * </p>
     *
     * @param visitorRegistrar
     *         visitor registrar (can be used to add/remove other visitors); the registrar is only guaranteed to be
     *         valid for the duration of this method call
     */
    default void afterRemove(VisitorRegistrar visitorRegistrar) {
        // do nothing
    }

    /**
     * Decorates this token visitor so that when it's removed, {@code visitors} will be registered.
     * <p>
     * The default implementation uses {@link RegisterOnRemoveTokenVisitor} and will <em>not</em> notify the visitors of
     * the last consumed token before removal. If that is needed, this method needs to be overridden.
     * </p>
     * <p>
     * For this to work, the returned visitor must be registered, not this instance.
     * </p>
     *
     * @param visitors
     *         visitors to register on removal of this visitor
     * @return decorating instance
     * @see #onRemoveRegister(TokenVisitor)
     * @since 7
     */
    default TokenVisitor onRemoveRegister(Collection<TokenVisitor> visitors) {
        return new RegisterOnRemoveTokenVisitor<>(this, visitors);
    }

    /**
     * Variant of {@link #onRemoveRegister(Collection)} to register a single visitor.
     * <p>
     * The default implementation calls {@link #onRemoveRegister(Collection)}, so in general there should be no need to
     * override it.
     * </p>
     * <p>
     * For this to work, the returned visitor must be registered, not this instance.
     * </p>
     *
     * @param visitor
     *         visitor to register on removal of this visitor
     * @return decorating instance
     * @see #onRemoveRegister(Collection)
     * @since 7
     */
    default TokenVisitor onRemoveRegister(TokenVisitor visitor) {
        return onRemoveRegister(List.of(visitor));
    }

}
