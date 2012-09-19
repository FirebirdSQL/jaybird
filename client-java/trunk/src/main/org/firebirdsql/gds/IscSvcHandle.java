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

package org.firebirdsql.gds;

import java.util.List;

/**
 * The interface <code>isc_svc_handle</code> is a java mapping for a
 * isc_svc_handle handle.
 */
public interface IscSvcHandle {

    /**
     * Retrieve whether this service handle is valid.
     * 
     * @return <code>true</code> if the handle is valid, <code>false</code>
     *         otherwise
     */
    boolean isValid();

    /**
     * Retrieve whether this service handle is invalid.
     * 
     * @return <code>true</code> if the handle is invalid, <code>false</code>
     *         otherwise
     */
    boolean isNotValid();

    /**
     * Get list of warnings that were returned by the server.
     * 
     * @return instance of {@link java.util.List}containing instances of
     *         {@link GDSException}representing server warnings (method
     *         {@link GDSException#isWarning()}returns <code>true</code>).
     */
    List<GDSException> getWarnings();

    /**
     * Clear warning list associated with this connection.
     */
    void clearWarnings();
}
