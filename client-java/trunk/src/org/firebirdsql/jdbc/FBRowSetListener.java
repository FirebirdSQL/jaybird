/*   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Alternatively, the contents of this file may be used under the
 *   terms of the GNU Lesser General Public License Version 2 or later (the
 *   "LGPL"), in which case the provisions of the GPL are applicable
 *   instead of those above. You may obtain a copy of the Licence at
 *   http://www.gnu.org/copyleft/lgpl.html
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    relevant License for more details.
 *
 *    This file was created by members of the firebird development team.
 *    All individual contributions remain the Copyright (C) of those
 *    individuals.  Contributors to this file are either listed here or
 *    can be obtained from a CVS history command.
 *
 *    All rights reserved.

 */

package org.firebirdsql.jdbc;


// imports --------------------------------------
import javax.sql.RowSetListener;
import javax.sql.RowSetEvent;
/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */


/**
 * <P>The RowSetListener interface is implemented by a
 * component that wants to be notified when a significant
 * event happens in the life of a RowSet
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
