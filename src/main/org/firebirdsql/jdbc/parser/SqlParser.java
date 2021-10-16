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

import org.firebirdsql.util.InternalApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * Simple visiting SQL parser.
 * <p>
 * This parser is not thread-safe.
 * </p>
 *
 * @since 5
 */
@InternalApi
public final class SqlParser implements VisitorRegistrar {

    private static final Logger log = Logger.getLogger(SqlParser.class.getName());

    private final Set<TokenVisitor> visitors = new CopyOnWriteArraySet<>();

    private final Iterator<Token> tokenIterator;

    private boolean started;
    private boolean completed;
    private boolean parsing;

    // default access for tests
    SqlParser(Iterator<Token> tokenIterator) {
        this.tokenIterator = tokenIterator;
    }

    /**
     * Creates a builder for a SQL parser with a supplier of reserved words.
     *
     * @param reservedWords
     *         Reserved words
     * @return builder to complete initialization of a SQL parser
     */
    public static Builder withReservedWords(ReservedWords reservedWords) {
        return new Builder(SqlTokenizer.withReservedWords(reservedWords));
    }

    /**
     * Starts parsing of the statement text.
     *
     * @throws IllegalStateException
     *         When {@code parse()} has already been parsed, or there are no registered visitors.
     * @see #resumeParsing()
     */
    public void parse() {
        if (started) {
            throw new IllegalStateException("Parsing was already started");
        }
        resumeParsing();
    }

    /**
     * Start or resume parsing of the statement text.
     * <p>
     * If the parsing process previously stalled because there were no more visitors, this continues parsing at the
     * point the parser previously stopped. If parsing was not yet started (e.g. using {@link #parse()}), it will
     * start.
     * </p>
     *
     * @throws IllegalStateException
     *         When parsing was already completed, parsing is already in progress, or there are no registered visitors
     * @see #parse()
     */
    public void resumeParsing() {
        if (completed) {
            throw new IllegalStateException("Parsing was already completed");
        } else if (parsing) {
            throw new IllegalStateException("Parsing already in progress");
        } else if (visitors.isEmpty()) {
            throw new IllegalStateException("Parser has no visitors");
        } else if (!started) {
            started = true;
        }

        try {
            parsing = true;
            final Iterator<Token> tokenIterator = this.tokenIterator;
            final Set<TokenVisitor> visitors = this.visitors;
            // We allow visitors to remove themselves when they are no longer interested,
            // once there are no interested visitors, we stop parsing
            while (!visitors.isEmpty() && tokenIterator.hasNext()) {
                notifyToken(tokenIterator.next());
            }

            if (!tokenIterator.hasNext()) {
                completed = true;
                notifyComplete();
            }
        } finally {
            parsing = false;
        }
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isParsing() {
        return parsing;
    }

    public boolean isCompleted() {
        return completed;
    }

    private void notifyToken(Token token) {
        for (TokenVisitor visitor : visitors) {
            try {
                visitor.visitToken(token, this);
            } catch (Exception e) {
                log.log(Level.WARNING, format("Ignored exception notifying visitor %s of token %s", visitor, token), e);
            }
        }
    }

    private void notifyComplete() {
        for (TokenVisitor visitor : visitors) {
            try {
                visitor.complete(this);
            } catch (Exception e) {
                log.log(Level.WARNING, format("Ignored exception notifying visitor %s of completion", visitor), e);
            }
        }
    }

    /**
     * Fluent variant of {@link #addVisitor(TokenVisitor)}.
     *
     * @param tokenVisitor
     *         token visitor to add
     * @return this SQL parser
     */
    public SqlParser add(TokenVisitor tokenVisitor) {
        addVisitor(tokenVisitor);
        return this;
    }

    @Override
    public void addVisitor(TokenVisitor tokenVisitor) {
        visitors.add(tokenVisitor);
    }

    @Override
    public void removeVisitor(TokenVisitor tokenVisitor) {
        visitors.remove(tokenVisitor);
    }

    public static class Builder {

        private final SqlTokenizer.Builder tokenizerBuilder;
        private List<TokenVisitor> visitors;

        private Builder(SqlTokenizer.Builder tokenizerBuilder) {
            this.tokenizerBuilder = tokenizerBuilder;
        }

        public Builder withVisitor(TokenVisitor visitor) {
            if (visitors == null) {
                visitors = new ArrayList<>();
            }
            visitors.add(visitor);
            return this;
        }

        public Builder withVisitors(TokenVisitor... visitors) {
            if (this.visitors == null) {
                this.visitors = new ArrayList<>();
            }
            this.visitors.addAll(Arrays.asList(visitors));
            return this;
        }

        public SqlParser of(String statementText) {
            SqlParser parser = new SqlParser(tokenizerBuilder.of(statementText));
            if (visitors != null) {
                visitors.forEach(parser::addVisitor);
            }
            return parser;
        }

    }

}
