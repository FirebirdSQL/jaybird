/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.StatementState;
import org.firebirdsql.gds.ng.fields.FieldValue;

import java.util.*;

/**
 * Dispatcher to maintain and notify other {@link StatementListener}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since
 */
public final class StatementListenerDispatcher extends AbstractListenerDispatcher<StatementListener> implements StatementListener {

    @Override
    public void newRow(final FbStatement sender, final List<FieldValue> rowData) {
        for (StatementListener listener : this) {
            listener.newRow(sender, rowData);
        }
    }

    @Override
    public void allRowsFetched(final FbStatement sender) {
        for (StatementListener listener : this) {
            listener.allRowsFetched(sender);
        }
    }

    @Override
    public void statementExecuted(final FbStatement sender, final boolean hasResultSet, final boolean hasSingletonResult) {
        for (StatementListener listener : this) {
            listener.statementExecuted(sender, hasResultSet, hasSingletonResult);
        }
    }

    @Override
    public void statementStateChanged(FbStatement sender, StatementState newState, StatementState previousState) {
        for (StatementListener listener : this) {
            listener.statementStateChanged(sender, newState, previousState);
        }
    }
}
