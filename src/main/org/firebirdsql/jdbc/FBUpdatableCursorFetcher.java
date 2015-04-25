/*
 * $Id$
 *
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.jdbc;

import java.sql.SQLException;

import org.firebirdsql.gds.impl.AbstractIscStmtHandle;
import org.firebirdsql.gds.impl.GDSHelper;

/**
 * Statement fetcher for updatable cursor case. This fetcher keeps cursor
 * position consistent, however we cannot tell now if we are on the last record.
 * Method {@link #isLast()}throws exception now.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy
 *         </a>
 */
public class FBUpdatableCursorFetcher extends FBStatementFetcher {

    FBUpdatableCursorFetcher(GDSHelper gdsHelper, Synchronizable syncProvider,
            AbstractIscStmtHandle stmth,
            FBObjectListener.FetcherListener fetcherListener, int maxRows,
            int fetchSize) throws SQLException {
        super(gdsHelper, syncProvider, stmth, fetcherListener, maxRows,
                fetchSize);
    }

    public boolean next() throws SQLException {
        if (isBeforeFirst()) {
            setIsBeforeFirst(false);
            setIsEmpty(false);
            setIsFirst(true);

            setRowNum(getRowNum() + 1);
            fetcherListener.rowChanged(this, getNextRow());

            return true;
        }

        setIsBeforeFirst(false);
        setIsFirst(false);
        setIsLast(false);
        setIsAfterLast(false);

        if (isEmpty())
            return false;
        else if (getNextRow() == null
                || (this.maxRows != 0 && getRowNum() == this.maxRows)) {
            setIsAfterLast(true);
            setRowNum(0);
            return false;
        } else {
            try {
                fetch();

                boolean maxRowReached = this.maxRows != 0
                        && getRowNum() == this.maxRows;

                if (getNextRow() == null || maxRowReached) {
                    setIsAfterLast(true);
                    return false;
                }

                fetcherListener.rowChanged(this, getNextRow());
                setRowNum(getRowNum() + 1);

                return true;
            } catch (SQLException sqle) {
                throw sqle;
            }
        }
    }

    public boolean isLast() throws SQLException {
        throw new FBDriverNotCapableException(
                "isLast() operation is not defined in case of "
                        + "updatable cursors, because server cannot determine cursor position "
                        + "without additional fetch.");
    }

}