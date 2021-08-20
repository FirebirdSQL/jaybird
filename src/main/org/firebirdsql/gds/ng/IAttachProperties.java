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

import org.firebirdsql.jaybird.props.AttachmentProperties;
import org.firebirdsql.jaybird.props.PropertyConstants;

/**
 * Common properties for database and service attach.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface IAttachProperties<T extends IAttachProperties<T>> extends AttachmentProperties {

    int DEFAULT_PORT = 3050;
    String DEFAULT_SERVER_NAME = "localhost";
    int DEFAULT_SOCKET_BUFFER_SIZE = PropertyConstants.BUFFER_SIZE_NOT_SET;
    int DEFAULT_SO_TIMEOUT = PropertyConstants.TIMEOUT_NOT_SET;
    int DEFAULT_CONNECT_TIMEOUT = PropertyConstants.TIMEOUT_NOT_SET;

    /**
     * @return The name of the object to attach to (either a database or service name).
     */
    String getAttachObjectName();

    // TODO Do serverName and portNumber belong in AttachmentProperties?
    //  Considerations: this ties it too much to TCP/IP

    /**
     * Get the hostname or IP address of the Firebird server.
     * <p>
     * NOTE: Implementer should take care to return {@link #DEFAULT_SERVER_NAME}
     * if value hasn't been set yet.
     * </p>
     *
     * @return Hostname or IP address of the server
     */
    String getServerName();

    /**
     * Set the hostname or IP address of the Firebird server.
     * <p>
     * NOTE: Implementer should take care to use {@link #DEFAULT_SERVER_NAME} if
     * value hasn't been set yet.
     * </p>
     *
     * @param serverName
     *         Hostname or IP address of the server
     */
    void setServerName(String serverName);

    /**
     * Get the portnumber of the server.
     * <p>
     * NOTE: Implementer should take care to return {@link #DEFAULT_PORT} if
     * value hasn't been set yet.
     * </p>
     *
     * @return Portnumber of the server
     */
    int getPortNumber();

    /**
     * Set the port number of the server.
     * <p>
     * NOTE: Implementer should take care to use the {@link #DEFAULT_PORT} if
     * this method hasn't been called yet.
     * </p>
     *
     * @param portNumber
     *         Port number of the server
     */
    void setPortNumber(int portNumber);

    /**
     * @return The value of {@link #getWireCrypt()} as an instance of {@link WireCrypt}.
     * @since 5
     * @see #getWireCrypt()
     */
    WireCrypt getWireCryptAsEnum();

    /**
     * Set the wire encryption level.
     *
     * @param wireCrypt
     *         Wire encryption level ({@code null} not allowed)
     * @since 4.0
     * @see #setWireCrypt(String)
     */
    void setWireCrypt(WireCrypt wireCrypt);

    /**
     * @return An immutable version of this instance as an implementation of {@link IAttachProperties}
     */
    T asImmutable();

    /**
     * @return A new, mutable, instance as an implementation of {@link IAttachProperties} with all properties copied.
     */
    T asNewMutable();

    /**
     * @return {@code true} if this is an immutable implementation, {@code false} if mutable
     * @since 5
     */
    boolean isImmutable();
}
