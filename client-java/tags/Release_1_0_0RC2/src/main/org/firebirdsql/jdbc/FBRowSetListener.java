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


import javax.sql.RowSetListener;
import javax.sql.RowSetEvent;

/**
 * Describe class <code>FBRowSetListener</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBRowSetListener implements RowSetListener {

  /**
   * <P>Called when the rowset is changed.
   *
   * @param event an object describing the event
   */
    public void rowSetChanged(RowSetEvent event) {
    }


  /**
   * <P>Called when a row is inserted, updated, or deleted.
   *
   * @param event an object describing the event
   */
    public void rowChanged(RowSetEvent event) {
    }


  /**
   * Called when a rowset's cursor is moved.
   *
   * @param event an object describing the event
   */
    public void cursorMoved(RowSetEvent event) {
    }

}
