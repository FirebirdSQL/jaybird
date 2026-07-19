// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.jaybird.util.ObjectReference;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Statement type identification.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@InternalApi
public final class StatementIdentification {

    private static final StatementIdentification UNKNOWN_INSTANCE =
            new StatementIdentification(LocalStatementType.UNKNOWN, null, false);

    private final LocalStatementType statementType;
    private final @Nullable ObjectReference targetObject;
    private final boolean returningClauseDetected;

    StatementIdentification(LocalStatementType statementType, @Nullable ObjectReference targetObject,
            boolean returningClauseDetected) {
        this.statementType = requireNonNull(statementType, "statementType");
        this.targetObject = targetObject;
        this.returningClauseDetected = returningClauseDetected;
    }

    /**
     * @return returns an instance with {@link LocalStatementType#UNKNOWN}, no target object, no returning clause
     */
    public static StatementIdentification unknown() {
        return UNKNOWN_INSTANCE;
    }

    public LocalStatementType getStatementType() {
        return statementType;
    }

    /**
     * Target object of the statement.
     * <p>
     * For example, for DML statements other than {@code SELECT}, this is the (target) table. If a target object is
     * available for a particular statement depends on the needs of Jaybird.
     * </p>
     *
     * @return target object if any, otherwise empty
     */
    public Optional<ObjectReference> getTargetObject() {
        return Optional.ofNullable(targetObject);
    }

    public boolean returningClauseDetected() {
        return returningClauseDetected;
    }

}
