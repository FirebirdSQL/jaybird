// SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import java.sql.SQLException;

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbStatement;

/**
 * Statement fetcher for updatable cursor case. This fetcher keeps cursor
 * position consistent, however we cannot tell now if we are on the last record.
 * Method {@link #isLast()} throws exception now.
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
final class FBUpdatableCursorFetcher extends FBStatementFetcher {

    FBUpdatableCursorFetcher(GDSHelper gdsHelper, FetchConfig fetchConfig, FbStatement stmth,
            FBObjectListener.FetcherListener fetcherListener) throws SQLException {
        super(gdsHelper, fetchConfig, stmth, fetcherListener);
    }

    @Override
    public boolean next() throws SQLException {
        if (isBeforeFirst()) {
            setIsBeforeFirst(false);
            setIsEmpty(false);
            setIsFirst(true);

            setRowNum(getRowNum() + 1);
            notifyRowChanged(getNextRow());

            return true;
        }

        setIsBeforeFirst(false);
        setIsFirst(false);
        setIsLast(false);
        setIsAfterLast(false);

        if (isEmpty()) return false;
        
        int maxRows = getMaxRows();
        if (getNextRow() == null || (maxRows != 0 && getRowNum() == maxRows)) {
            setIsAfterLast(true);
            setRowNum(0);
            return false;
        } else {
            fetch();

            boolean maxRowReached = maxRows != 0 && getRowNum() == maxRows;

            if ((getNextRow() == null) || maxRowReached) {
                setIsAfterLast(true);
                return false;
            }

            notifyRowChanged(getNextRow());
            setRowNum(getRowNum() + 1);

            return true;
        }
    }

    @Override
    public boolean isLast() throws SQLException {
        throw new FBDriverNotCapableException(
                "isLast() operation is not defined in case of "
                + "updatable cursors, because server cannot determine cursor position "
                + "without additional fetch.");
    }

}