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
package org.firebirdsql.gds.ng.dbcrypt;

/**
 * Plugin for Firebird database encryption callback.
 * <p>
 * Database encryption callbacks are allowed to be stateful (eg if they require multiple callbacks to work). A new
 * callback instance is created for each authentication phase of a connection (a connection can have multiple
 * authentication phases).
 * </p>
 * <p>
 * NOTE: This plugin is currently only internal to Jaybird, consider the API as unstable.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0.4
 */
public interface DbCryptCallback {

    /**
     * Name of the database encryption callback.
     *
     * @return Name for identifying this callback within Jaybird.
     * @see DbCryptCallbackSpi#getDbCryptCallbackName()
     */
    String getDbCryptCallbackName();

    /**
     * Callback method to be called with the server data.
     * <p>
     * The implementation should reply with a response for the provided data. If the plugin cannot provide a response
     * (eg because the server data is invalid), use an empty reply (eg use {@link DbCryptData#EMPTY_DATA}, or construct
     * your own). The plugin should <b>not</b> throw an exception.
     * </p>
     *
     * @param serverData
     *         Data received from the server (never {@code null}).
     * @return Reply data (never {@code null}, use {@link DbCryptData#EMPTY_DATA} if there is no (valid) reply).
     */
    DbCryptData handleCallback(DbCryptData serverData);

}
