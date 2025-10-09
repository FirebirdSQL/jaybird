// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.firebirdsql.jdbc.QuoteStrategy;

import java.util.List;
import java.util.stream.Stream;

/**
 * An identifier chain is an object reference consisting of one or more identifiers.
 * <p>
 * In practice, we'll use {@link Identifier} if there is only one identifier.
 * </p>
 * <p>
 * The recommended way to create this object is through {@link ObjectReference#of(String...)} or
 * {@link ObjectReference#of(List)}.
 * </p>
 *
 * @since 7
 * @see ObjectReference
 */
final class IdentifierChain extends ObjectReference {

    private final List<Identifier> identifiers;
    // cached hashcode, -1 signals not yet cached
    private int hashCode = -1;

    IdentifierChain(List<Identifier> identifiers) {
        if (identifiers.isEmpty()) {
            throw new IllegalArgumentException("identifier chain cannot be empty");
        }
        this.identifiers = List.copyOf(identifiers);
    }

    @Override
    public int size() {
        return identifiers.size();
    }

    @Override
    public Identifier at(int index) {
        return identifiers.get(index);
    }

    @Override
    public String toString(QuoteStrategy quoteStrategy) {
        // Estimate 16 characters per element (including quotes and separator)
        return append(new StringBuilder(size() * 16), quoteStrategy).toString();
    }

    @Override
    public StringBuilder append(StringBuilder sb, QuoteStrategy quoteStrategy) {
        for (Identifier identifier : identifiers) {
            identifier.append(sb, quoteStrategy).append('.');
        }
        // Remove last dot separator
        sb.setLength(sb.length() - 1);
        return sb;
    }

    @Override
    public Stream<Identifier> stream() {
        return identifiers.stream();
    }

    @Override
    public List<Identifier> toList() {
        return identifiers;
    }

    @Override
    public int hashCode() {
        int hashCode = this.hashCode;
        return hashCode != -1 ? hashCode : hashCode0();
    }

    private int hashCode0() {
        // This needs to be consistent with Identifier.hashCode for an instance with a single Identifier
        int hashCode = 1;
        for (Identifier identifier : identifiers) {
            hashCode = 31 * hashCode + identifier.name().hashCode();
        }
        // Clear sign bit to avoid -1 (and any other negative value) as it's used as a "not cached" marker
        return this.hashCode = hashCode & 0x7FFF_FFFF;
    }

}
