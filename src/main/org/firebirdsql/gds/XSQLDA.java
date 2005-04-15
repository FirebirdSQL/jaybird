/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
