/*
 * Firebird Open Source J2ee connector - jdbc driver, public Firebird-specific 
 * JDBC extensions.
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
package org.firebirdsql.jdbc;

import java.sql.CallableStatement;


/**
 * Firebird extension to the {@link java.sql.CallableStatement} interface.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public interface FirebirdCallableStatement extends FirebirdPreparedStatement, CallableStatement {

    /**
     * Mark this callable statement as a call of the selectable procedure. By
     * default callable statement uses "EXECUTE PROCEDURE" SQL statement to 
     * invoke stored procedures that return single row of output parameters or
     * a result set. In former case it retrieves only the first row of the 
     * result set.
     *  
     * @see getSelectableProcedure
     * @param selectable <code>true</code> if the called procedure is selectable.
     */
    void setSelectableProcedure(boolean selectable);

    /**
     * Retrieve if this callable statement has been marked as selectable.
     * 
     * Starting from Firebird 2.1, this value is set automatically from metadata stored in the
     * database. Prior to Firebird 2.1, it must be set manually.
     * 
     * @see setSelectableProcedure
     * @return <code>true</code> if the called procedure is selectable, false otherwise
     */
	boolean isSelectableProcedure();
}
