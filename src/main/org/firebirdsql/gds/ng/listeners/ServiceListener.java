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
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbService;

import java.sql.SQLWarning;

/**
 * Listener for service events.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface ServiceListener {

    /**
     * Called before the {@code service} will be detached.
     * <p>
     * This event is intended for cleanup action, implementer should take care that
     * no exceptions are thrown from this method.
     * </p>
     *
     * @param service
     *         The service object that is detaching
     */
    void detaching(FbService service);

    /**
     * Called when the {@code service} connection has been detached
     *
     * @param service
     *         The database object that was detached
     */
    void detached(FbService service);

    /**
     * Called when a warning was received for the {@code service} connection.
     * <p>
     * In implementation it is possible that some warnings are not sent to listeners on the database, but only to
     * listeners on specific connection derived objects.
     * </p>
     *
     * @param service
     *         service receiving the warning
     * @param warning
     *         Warning
     */
    void warningReceived(FbService service, SQLWarning warning);
}
