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
package org.firebirdsql.jdbc;

import org.firebirdsql.util.InternalApi;

import java.sql.SQLException;

/**
 * Meta-information on stored procedures in a Firebird database.
 * <p>
 * This interface is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this
 * type.
 * </p>
 */
@InternalApi
public interface StoredProcedureMetaData {

    /**
     * Determine if the "selectability" of procedures is available. 
     * This functionality is only available starting from Firebird 2.1, 
     * and only with databases created by that version or later.
     * 
     * @return {@code true} if selectability information is available,  {@code false} otherwise
     */
    boolean canGetSelectableInformation();
    
    /**
     * Retrieve whether a given stored procedure is selectable.
     * <p>
     * A selectable procedure is one that can return multiple rows of results (i.e. it uses a {@code SUSPEND}
     * statement).
     * </p>
     * 
     * @param procedureName 
     *      The name of the procedure for which selectability information is to be retrieved
     * @return
     *      {@code true} if the procedure is selectable, {@code false} otherwise
     * @throws SQLException If no selectability information is available
     */
    boolean isSelectable(String procedureName) throws SQLException;
    
}
