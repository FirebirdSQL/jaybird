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

package org.firebirdsql.gds;



import org.firebirdsql.jgds.GDS_Impl;



/**
 * The class <code>GDSFactory</code> exists to provide a way
 * to obtain objects implementing GDS and Clumplet.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
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

