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

package org.firebirdsql.gds;


// imports --------------------------------------


import org.firebirdsql.jgds.GDS_Impl;


/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */

public class GDSFactory {

    public static GDS newGDS() {
        return new GDS_Impl();
    }


    public static Clumplet newClumplet(int type, String content) {
        return GDS_Impl.newClumplet(type, content);
    }

    public static Clumplet newClumplet(int type){
        return GDS_Impl.newClumplet(type);
    }


    public static Clumplet newClumplet(int type, int c){
        return GDS_Impl.newClumplet(type, c);
    }

    public static Clumplet newClumplet(int type, byte[] content) {
        return GDS_Impl.newClumplet(type, content);
    }

    public static Clumplet cloneClumplet(Clumplet c) {
        return GDS_Impl.cloneClumplet(c);
    }

}

