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

/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */

package org.firebirdsql.gds;

/**
 * The class <code>XSQLDA</code> is a java mapping of the XSQLDA server
 * data structure used to represent one row for input and output.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @version 1.0
 */
public class XSQLDA {

    /** Version of <code>XSQLDA</code> being used. */
    public int version;

    /** The number of input columns. */
    public int sqln;

    /** The number of output columns. */
    public int sqld;

    /** Array of column values. */
    public XSQLVAR[] sqlvar;

    /** 
     * Internal array of values containing information about the type and 
     * length of columns of this row.
     */
    public byte[] blr;

    /** Array of length (by type) values for each column in this row. */
    public int[] ioLength;	 // 0 varchar, >0 char, -4 int etc, -8 long etc

    public XSQLDA() {
        version = ISCConstants.SQLDA_VERSION1;
    }

    /**
     * Create a new instance of <code>XSQLDA</code> with a given number 
     * of columns.
     *
     * @param n The number of columns to be used
     */
    public XSQLDA(int n) {
        version = ISCConstants.SQLDA_VERSION1;
        sqln = n;
        sqld = n;
        sqlvar = new XSQLVAR[n];
    }

}
