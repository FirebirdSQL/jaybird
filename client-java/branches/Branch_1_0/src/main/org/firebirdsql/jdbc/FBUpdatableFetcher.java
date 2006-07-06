/*
 * Firebird Open Source J2ee connector - jdbc driver
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
package org.firebirdsql.jdbc;

import java.sql.SQLException;
import org.firebirdsql.gds.isc_stmt_handle;

/**
 * Statement fetcher for updatable cursor case. This fetcher keeps cursor 
 * position consistent, however we cannot tell now if we are on the last
 * record. Method {@link #getIsLast()} throws exception now.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBUpdatableFetcher extends FBStatementFetcher {

    FBUpdatableFetcher(FBConnection c, FBStatement fbStatement, 
        isc_stmt_handle stmth, FBResultSet rs) throws SQLException 
    {
        super(c, fbStatement, stmth, rs);
    }

    public boolean next() throws SQLException {
        if (getIsBeforeFirst()) {
            setIsBeforeFirst(false);
            setIsEmpty(false);
            setIsFirst(true);

            setRowNum(getRowNum() + 1);
            rs.row = nextRow;

            return true;
        }

        setIsBeforeFirst(false);
        setIsFirst(false);
        setIsLast(false);
        setIsAfterLast(false);

        if (getIsEmpty())
            return false;
        else 
        if (nextRow == null || (fbStatement.maxRows!=0 && getRowNum()==fbStatement.maxRows)){
            setIsAfterLast(true);
            setRowNum(0);
            return false;
        }
        else {
            try {
                fetch();

                boolean maxRowReached = 
                    fbStatement.maxRows!=0 && getRowNum()==fbStatement.maxRows;

                if((nextRow==null) || maxRowReached) {
                    setIsAfterLast(true);
                    return false;
                }

                rs.row = nextRow;
                setRowNum(getRowNum() + 1);

                return true;
            }
            catch (SQLException sqle) {
                throw sqle;
            }
        }
    }


    public boolean getIsLast() throws SQLException {
        throw new SQLException("isLast() operation is not defined in case of " +
            "updatable cursors, because server cannot determine cursor position " +
            "without additional fetch.");
    }


}