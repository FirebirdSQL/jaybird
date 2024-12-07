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
package org.firebirdsql.gds;

import org.jspecify.annotations.NullMarked;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A message template overriding the SQLstate of its parent.
 */
@NullMarked
final class OverriddenSqlStateMessageTemplate extends MessageTemplate {

    private final DefaultMessageTemplate parent;
    private final String sqlState;

    /**
     * Creates a message template based on {@code parent}, overriding the {@code sqlState}
     *
     * @param parent
     *         parent template
     * @param sqlState
     *         non-{@code null} SQLstate
     * @throws NullPointerException
     *         if {@code parent} or {@code sqlState} is {@code null}
     * @throws IllegalArgumentException
     *         if {@code sqlState} is not 5 characters long
     */
    OverriddenSqlStateMessageTemplate(DefaultMessageTemplate parent, String sqlState) {
        this.parent = requireNonNull(parent, "parent");
        this.sqlState = MessageTemplate.validateSqlState(sqlState);
    }

    @Override
    public int errorCode() {
        return parent.errorCode();
    }

    @Override
    public String sqlState() {
        return sqlState;
    }

    @Override
    public MessageTemplate withDefaultSqlState(String defaultSqlState) {
        // Never has a null sqlState, so return this instance
        return this;
    }

    @Override
    public MessageTemplate withSqlState(String sqlState) {
        if (sqlState.equals(sqlState())) return this;
        if (sqlState.equals(parent.sqlState())) return parent;
        return new OverriddenSqlStateMessageTemplate(parent, sqlState);
    }

    @Override
    public void appendMessage(StringBuilder messageBuffer, List<?> parameters) {
        parent.appendMessage(messageBuffer, parameters);
    }

}
