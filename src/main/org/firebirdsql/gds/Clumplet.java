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

import java.io.Serializable;

/**
 * The interface <code>Clumplet</code> models various Firebird datastructures
 * consisting of a length, type, and data. These are used in database parameter
 * blocks among other places.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface Clumplet 
    extends Serializable
{

    void append(Clumplet c);

    byte[] find(int type);

    String findString(int type);

    int getLength();

    boolean equals(Object o);

    int hashCode();
    
    /**
     * Remove clumplet of the specified type.
     * 
     * @param type type to remove.
     * 
     * @return head of the clumplet without removed clumplet.
     */
    Clumplet remove(int type);

    /** @link dependency */
    /*# ISCConstants lnkISCConstants; */
//    public void write(OutputStream out) throws IOException;

}
