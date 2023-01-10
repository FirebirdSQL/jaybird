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
package org.firebirdsql.gds.ng;

import java.sql.SQLException;

/**
 * Factory for {@link org.firebirdsql.gds.ng.FbDatabase} instances.
 * <p>
 * A <code>FbDatabaseFactory</code> knows how to create connected (but unattached) instance of {@link org.firebirdsql.gds.ng.FbDatabase}
 * for a specific protocol type (eg wire protocol, embedded or native).
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface FbDatabaseFactory {

    /**
     * Connects to a Firebird server based on the supplied connection properties.
     * <p>
     * The {@link org.firebirdsql.gds.ng.FbDatabase} instance will be connected to the server, but is not yet attached.
     * </p>
     *
     * @param connectionProperties Connection properties
     * @return Database instance
     */
    FbDatabase connect(IConnectionProperties connectionProperties) throws SQLException;

    /**
     * Connects to the service manager of a Firebird server with the supplied service properties.
     *
     * @param serviceProperties Service properties
     * @return Service instance
     */
    FbService serviceConnect(IServiceProperties serviceProperties) throws SQLException;
}
