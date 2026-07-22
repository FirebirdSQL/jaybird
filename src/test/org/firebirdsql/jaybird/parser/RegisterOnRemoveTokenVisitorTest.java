// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class RegisterOnRemoveTokenVisitorTest {

    @Mock
    private TokenVisitor decoratedVisitor;
    @Mock
    private TokenVisitor newVisitor;
    @Mock
    private VisitorRegistrar visitorRegistrar;

    @Test
    void visitToken_forwardedToDecoratedVisitor(@Mock Token token) {
        var registerOnRemove = new RegisterOnRemoveTokenVisitor<>(decoratedVisitor, List.of(newVisitor));

        registerOnRemove.visitToken(token, visitorRegistrar);

        //noinspection DataFlowIssue
        verify(decoratedVisitor).visitToken(eq(token), not(same(visitorRegistrar)));
        verifyNoInteractions(newVisitor, visitorRegistrar);
    }

    @Test
    void selfRemoveDuringVisitToken_registersNewVisitor_noTokenOnNewVisitor(@Mock Token token) {
        var registerOnRemove = new RegisterOnRemoveTokenVisitor<>(decoratedVisitor, List.of(newVisitor));
        doAnswer(invocation -> {
            invocation.getArgument(1, VisitorRegistrar.class).removeVisitor(decoratedVisitor);
            return null;
        }).when(decoratedVisitor).visitToken(any(), any());
        doAnswer(invocation -> {
            invocation.getArgument(0, TokenVisitor.class).afterRemove(visitorRegistrar);
            return null;
        }).when(visitorRegistrar).removeVisitor(any());

        registerOnRemove.visitToken(token, visitorRegistrar);

        // NOTE: The actually removed visitor is the RegisterOnRemoveTokenVisitor instance
        verify(visitorRegistrar).removeVisitor(registerOnRemove);
        // NOTE: The RegisterOnRemoveTokenVisitor is a visitor registrar for the decoratedVisitor
        verify(decoratedVisitor).afterRemove(registerOnRemove);
        verify(visitorRegistrar).addVisitor(newVisitor);
        verifyNoMoreInteractions(visitorRegistrar, decoratedVisitor, newVisitor);
    }

    @Test
    void selfRemoveDuringVisitToken_registersNewVisitors_tokenOnNewVisitors(@Mock Token token,
            @Mock TokenVisitor newVisitor2) {
        var registerOnRemove =
                new RegisterOnRemoveTokenVisitor<>(decoratedVisitor, List.of(newVisitor, newVisitor2), true);
        doAnswer(invocation -> {
            invocation.getArgument(1, VisitorRegistrar.class).removeVisitor(decoratedVisitor);
            return null;
        }).when(decoratedVisitor).visitToken(any(), any());
        doAnswer(invocation -> {
            invocation.getArgument(0, TokenVisitor.class).afterRemove(visitorRegistrar);
            return null;
        }).when(visitorRegistrar).removeVisitor(any());

        registerOnRemove.visitToken(token, visitorRegistrar);

        // NOTE: The actually removed visitor is the RegisterOnRemoveTokenVisitor instance
        verify(visitorRegistrar).removeVisitor(registerOnRemove);
        // NOTE: The RegisterOnRemoveTokenVisitor is a visitor registrar for the decoratedVisitor
        verify(decoratedVisitor).afterRemove(registerOnRemove);
        verify(visitorRegistrar).addVisitor(newVisitor);
        verify(visitorRegistrar).addVisitor(newVisitor2);
        verify(newVisitor).visitToken(eq(token), same(visitorRegistrar));
        verify(newVisitor2).visitToken(eq(token), same(visitorRegistrar));
        verifyNoMoreInteractions(visitorRegistrar, decoratedVisitor, newVisitor, newVisitor2);
    }

    @Test
    void selfRemoveDuringVisitToken_registersNewVisitors_tokenOnNewVisitors_exceptionsIgnored(@Mock Token token,
            @Mock TokenVisitor newVisitor2) {
        var registerOnRemove =
                new RegisterOnRemoveTokenVisitor<>(decoratedVisitor, List.of(newVisitor, newVisitor2), true);
        doAnswer(invocation -> {
            invocation.getArgument(1, VisitorRegistrar.class).removeVisitor(decoratedVisitor);
            return null;
        }).when(decoratedVisitor).visitToken(any(), any());
        doAnswer(invocation -> {
            invocation.getArgument(0, TokenVisitor.class).afterRemove(visitorRegistrar);
            return null;
        }).when(visitorRegistrar).removeVisitor(any());
        doAnswer(invocation -> {
            throw new RuntimeException("From newVisitor");
        }).when(newVisitor).visitToken(any(), any());
        doAnswer(invocation -> {
            throw new RuntimeException("From newVisitor2");
        }).when(newVisitor2).visitToken(any(), any());

        registerOnRemove.visitToken(token, visitorRegistrar);

        // NOTE: The actually removed visitor is the RegisterOnRemoveTokenVisitor instance
        verify(visitorRegistrar).removeVisitor(registerOnRemove);
        // NOTE: The RegisterOnRemoveTokenVisitor is a visitor registrar for the decoratedVisitor
        verify(decoratedVisitor).afterRemove(registerOnRemove);
        verify(visitorRegistrar).addVisitor(newVisitor);
        verify(visitorRegistrar).addVisitor(newVisitor2);
        verify(newVisitor).visitToken(eq(token), same(visitorRegistrar));
        verify(newVisitor2).visitToken(eq(token), same(visitorRegistrar));
        verifyNoMoreInteractions(visitorRegistrar, decoratedVisitor, newVisitor, newVisitor2);
    }

    @Test
    void registeringOtherVisitor_forwarded(@Mock Token token, @Mock TokenVisitor otherVisitor) {
        var registerOnRemove = new RegisterOnRemoveTokenVisitor<>(decoratedVisitor, List.of(newVisitor), true);
        doAnswer(invocation -> {
            invocation.getArgument(1, VisitorRegistrar.class).addVisitor(otherVisitor);
            return null;
        }).when(decoratedVisitor).visitToken(any(), any());

        registerOnRemove.visitToken(token, visitorRegistrar);

        verify(visitorRegistrar).addVisitor(otherVisitor);
        verifyNoMoreInteractions(visitorRegistrar, decoratedVisitor, newVisitor, otherVisitor);
    }

    @Test
    void removeOtherVisitor_forwarded(@Mock Token token, @Mock TokenVisitor otherVisitor) {
        var registerOnRemove = new RegisterOnRemoveTokenVisitor<>(decoratedVisitor, List.of(newVisitor), true);
        doAnswer(invocation -> {
            invocation.getArgument(1, VisitorRegistrar.class).removeVisitor(otherVisitor);
            return null;
        }).when(decoratedVisitor).visitToken(any(), any());

        registerOnRemove.visitToken(token, visitorRegistrar);

        verify(visitorRegistrar).removeVisitor(otherVisitor);
        verifyNoMoreInteractions(visitorRegistrar, decoratedVisitor, newVisitor, otherVisitor);
    }

    @Test
    void complete_forwardedToDecoratedVisitor_andOtherVisitors() {
        var registerOnRemove = new RegisterOnRemoveTokenVisitor<>(decoratedVisitor, List.of(newVisitor));

        registerOnRemove.complete(visitorRegistrar);

        //noinspection DataFlowIssue
        verify(decoratedVisitor).complete(not(same(visitorRegistrar)));
        verify(newVisitor).complete(same(visitorRegistrar));
        verifyNoMoreInteractions(visitorRegistrar, decoratedVisitor, newVisitor);
    }

}