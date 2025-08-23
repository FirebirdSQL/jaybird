// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.firebirdsql.jdbc.QuoteStrategy;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static org.firebirdsql.jaybird.util.StringUtils.trimToNull;

/**
 * An identifier is an object reference consisting of a single name.
 *
 * @since 7
 * @see ObjectReference
 */
public final class Identifier extends ObjectReference {

    private final String name;

    public Identifier(String name) {
        name = trimToNull(name);
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null, empty, or blank");
        }
        this.name = name;
    }

    public String name() {
        return name;
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
        return quoteStrategy.quoteObjectName(name);
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
        return quoteStrategy.appendQuoted(name, sb);
    }

    @Override
    public Stream<Identifier> stream() {
        return Stream.of(this);
    }

    @Override
    public List<Identifier> toList() {
        return List.of(this);
    }

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

    @Override
    public int hashCode() {
        // This needs to be consistent with IdentifierChain.hashCode (as if this is a chain with a single item)
        // We're clearing the sign bit, because that is what IdentifierChain does to avoid negative values
        return (31 + name.hashCode()) & 0x7FFF_FFFF;
    }

}
