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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class SqlParserTest {

    @Test
    void basicStatementTextParse(@Mock TokenVisitor visitor) {
        String statementText = "select * from sometable";
        SqlParser.withReservedWords(FirebirdReservedWords.latest()).of(statementText).add(visitor).parse();

        InOrder inOrder = inOrder(visitor);
        for (Token token : Arrays.<Token>asList(new ReservedToken(0, "select"), new WhitespaceToken(6, " "),
                new OperatorToken(7, "*"), new WhitespaceToken(8, " "), new ReservedToken(9, "from"),
                new WhitespaceToken(13, " "), new GenericToken(14, "sometable"))) {
            inOrder.verify(visitor).visitToken(eq(token), any());
        }
        inOrder.verify(visitor).complete(any());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void initialState(@Mock Iterator<Token> tokenIterator) {
        SqlParser parser = new SqlParser(tokenIterator);

        assertThat(parser.isParsing()).describedAs("parser.isParsing()").isFalse();
        assertThat(parser.isStarted()).describedAs("parser.isStarted()").isFalse();
        assertThat(parser.isCompleted()).describedAs("parser.isCompleted()").isFalse();
    }

    @Test
    void parseFailsWhenAlreadyStarted(@Mock Iterator<Token> tokenIterator, @Mock TokenVisitor visitor) {
        SqlParser parser = new SqlParser(tokenIterator).add(visitor);
        parser.parse();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(parser::parse)
                .withMessageContaining("already started");
    }

    @Test
    void basicParseToCompletion(@Mock TokenVisitor visitor) {
        List<Token> testTokens = Arrays.asList(new GenericToken(0, "A"), new GenericToken(1, "B"),
                new GenericToken(2, "C"));
        SqlParser parser = new SqlParser(testTokens.iterator()).add(visitor);

        parser.parse();

        InOrder inOrder = inOrder(visitor);
        for (Token token : testTokens) {
            inOrder.verify(visitor).visitToken(token, parser);
        }
        inOrder.verify(visitor).complete(parser);
        inOrder.verifyNoMoreInteractions();

        assertThat(parser.isParsing()).describedAs("parser.isParsing()").isFalse();
        assertThat(parser.isStarted()).describedAs("parser.isStarted()").isTrue();
        assertThat(parser.isCompleted()).describedAs("parser.isCompleted()").isTrue();
    }

    // Given implementation of parse() calls resumeParsing(), we only test resumeParsing for further behaviour

    @Test
    void resumeParsingFailsWithoutVisitor(@Mock Iterator<Token> tokenIterator) {
        SqlParser parser = new SqlParser(tokenIterator);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(parser::resumeParsing)
                .withMessageContaining("no visitors");
        verifyNoInteractions(tokenIterator);
    }

    @Test
    void isParsingTrueDuringParsing() {
        List<Token> testTokens = singletonList(new GenericToken(0, "A"));
        final SqlParser parser = new SqlParser(testTokens.iterator());
        final AtomicBoolean parsingValueVisitToken = new AtomicBoolean();
        final AtomicBoolean parsingValueComplete = new AtomicBoolean();
        TokenVisitor visitor = new AbstractTokenVisitor() {
            @Override
            protected void visitToken(Token token) {
                parsingValueVisitToken.set(parser.isParsing());
            }

            @Override
            protected void complete() {
                parsingValueComplete.set(parser.isParsing());
            }
        };
        parser.addVisitor(visitor);

        parser.resumeParsing();

        assertThat(parsingValueVisitToken).describedAs("isParsing during visitToken").isTrue();
        assertThat(parsingValueComplete).describedAs("isParsing during complete").isTrue();
    }

    @Test
    void haltsParsingWhenNoVisitorsSubscribed() {
        List<Token> testTokens = Arrays.asList(new GenericToken(0, "A"), new GenericToken(1, "B"),
                new GenericToken(2, "C"));
        HaltingVisitor visitor1 = new HaltingVisitor(testTokens.get(0));
        HaltingVisitor visitor2 = new HaltingVisitor(testTokens.get(1));
        SqlParser parser = new SqlParser(testTokens.iterator()).add(visitor1).add(visitor2);

        parser.resumeParsing();

        assertThat(parser.isParsing()).describedAs("parser.isParsing()").isFalse();
        assertThat(parser.isStarted()).describedAs("parser.isStarted()").isTrue();
        assertThat(parser.isCompleted()).describedAs("parser.isCompleted()").isFalse();
        Assertions.assertThat(visitor1.seenTokens).isEqualTo(testTokens.subList(0, 1));
        Assertions.assertThat(visitor2.seenTokens).isEqualTo(testTokens.subList(0, 2));
    }

    @Test
    void cannotResumeParsingWithoutVisitorsAfterInitialHalt() {
        List<Token> testTokens = Arrays.asList(new GenericToken(0, "A"), new GenericToken(1, "B"),
                new GenericToken(2, "C"));
        HaltingVisitor visitor1 = new HaltingVisitor(testTokens.get(0));
        HaltingVisitor visitor2 = new HaltingVisitor(testTokens.get(1));
        SqlParser parser = new SqlParser(testTokens.iterator()).add(visitor1).add(visitor2);
        parser.resumeParsing();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(parser::resumeParsing);
    }

    @Test
    void completeParsingAfterHalt(@Mock TokenVisitor lastVisitor) {
        List<Token> testTokens = Arrays.asList(new GenericToken(0, "A"), new GenericToken(1, "B"),
                new GenericToken(2, "C"));
        HaltingVisitor visitor1 = new HaltingVisitor(testTokens.get(0));
        HaltingVisitor visitor2 = new HaltingVisitor(testTokens.get(1));
        SqlParser parser = new SqlParser(testTokens.iterator()).add(visitor1).add(visitor2);
        parser.resumeParsing();

        parser.addVisitor(lastVisitor);
        parser.resumeParsing();

        verify(lastVisitor).visitToken(testTokens.get(2), parser);
        verify(lastVisitor).complete(parser);
        verifyNoMoreInteractions(lastVisitor);
        assertThat(parser.isParsing()).describedAs("parser.isParsing()").isFalse();
        assertThat(parser.isStarted()).describedAs("parser.isStarted()").isTrue();
        assertThat(parser.isCompleted()).describedAs("parser.isCompleted()").isTrue();
    }

    @Test
    void cannotResumeCompletedParser(@Mock Iterator<Token> iterator, @Mock TokenVisitor visitor) {
        SqlParser parser = new SqlParser(iterator).add(visitor);
        parser.resumeParsing();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(parser::resumeParsing)
                .withMessageContaining("already completed");
    }

    @Test
    void cannotResumeParsingWhenAlreadyParsing() {
        List<Token> testTokens = singletonList(new GenericToken(0, "A"));
        SqlParser parser = new SqlParser(testTokens.iterator());
        class ResumingVisitor extends AbstractTokenVisitor {
            Throwable throwableOnResume;

            @Override
            protected void visitToken(Token token) {
                try {
                    parser.resumeParsing();
                } catch (Throwable t) {
                    throwableOnResume = t;
                }
            }
        }
        ResumingVisitor visitor = new ResumingVisitor();
        parser.addVisitor(visitor);

        parser.resumeParsing();

        assertThat(visitor.throwableOnResume)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("in progress");
    }

    private static class HaltingVisitor implements TokenVisitor {

        private final Token stopToken;

        HaltingVisitor(Token stopToken) {
            this.stopToken = stopToken;
        }

        private final List<Token> seenTokens = new ArrayList<>();

        @Override
        public void visitToken(Token token, VisitorRegistrar visitorRegistrar) {
            seenTokens.add(token);
            if (stopToken.equals(token)) {
                visitorRegistrar.removeVisitor(this);
            }
        }

        @Override
        public void complete(VisitorRegistrar visitorRegistrar) {
            // do nothing
        }
    }

}