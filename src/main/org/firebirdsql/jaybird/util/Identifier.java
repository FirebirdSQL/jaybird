// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.firebirdsql.jdbc.QuoteStrategy;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.jaybird.util.StringUtils.trimToNull;

/**
 * An identifier is an object reference consisting of a single name.
 *
 * @author Mark Rotteveel
 * @since 7
 * @see ObjectReference
 */
public final class Identifier extends ObjectReference {

    private final String name;
    private final Scope scope;

    public Identifier(String name) {
        this(name, Scope.UNKNOWN);
    }

    public Identifier(String name, Scope scope) {
        name = trimToNull(name);
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null, empty, or blank");
        }
        this.name = name;
        this.scope = requireNonNull(scope, "scope");
    }

    public String name() {
        return name;
    }

    public Scope scope() {
        return scope;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Identifier at(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException(index);
        }
        return this;
    }

    @Override
    public Identifier first() {
        return this;
    }

    @Override
    public Identifier last() {
        return this;
    }

    /**
     * The name, quoted using {@code quoteStrategy}.
     *
     * @param quoteStrategy
     *         quote strategy
     * @return name, possibly quoted
     */
    public String toString(QuoteStrategy quoteStrategy) {
        // 12 is 2 quotes, 1 scope specifier, 7 scope PACKAGE + 2 slack
        return append(new StringBuilder(name.length() + 12), quoteStrategy).toString();
    }

    /**
     * Appends name to {@code sb} using {@code quoteStrategy}.
     *
     * @param sb
     *         string builder to append to
     * @param quoteStrategy
     *         quote strategy
     * @return {@code sb} for chaining
     */
    public StringBuilder append(StringBuilder sb, QuoteStrategy quoteStrategy) {
        quoteStrategy.appendQuoted(name, sb);
        if (scope != Scope.UNKNOWN) {
            sb.append('%').append(scope.name());
        }
        return sb;
    }

    @Override
    public Stream<Identifier> stream() {
        return Stream.of(this);
    }

    @Override
    public List<Identifier> toList() {
        return List.of(this);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The {@code scope} is <em>not</em> considered for equality.
     * </p>
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Identifier other) {
            return name.equals(other.name);
        } else if (obj instanceof ObjectReference otherRef) {
            // We're using ObjectReference, not IdentifierChain, so it'll also work for future subclasses, if any
            return otherRef.size() == 1 && name.equals(otherRef.at(0).name);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The {@code scope} is <em>not</em> considered for hash code.
     * </p>
     */
    @Override
    public int hashCode() {
        // This needs to be consistent with IdentifierChain.hashCode (as if this is a chain with a single item).
        // We're clearing the sign bit, because that is what IdentifierChain does to avoid negative values.
        return (31 + name.hashCode()) & 0x7FFF_FFFF;
    }

    /**
     * Scope (type) of the identifier.
     * <p>
     * During equality checks, the scope is <em>not</em> considered significant.
     * </p>
     */
    public enum Scope {
        /**
         * Scope of the identifier is not specified or unknown (the default).
         */
        UNKNOWN,
        /**
         * Scope of the identifier is a schema.
         * <p>
         * This will generally only be used when this was explicitly detected using the scope specifier or other
         * context-specific information.
         * </p>
         */
        SCHEMA,
        /**
         * Scope of the identifier is a package.
         * <p>
         * This will generally only be used when this was explicitly detected using the scope specifier or other
         * context-specific information.
         * </p>
         */
        PACKAGE,
    }

}
