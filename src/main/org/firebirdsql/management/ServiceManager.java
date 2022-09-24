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
package org.firebirdsql.management;

import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.WireCrypt;
import org.firebirdsql.jaybird.props.AttachmentProperties;
import org.firebirdsql.jaybird.props.ServiceConnectionProperties;

import java.io.OutputStream;
import java.sql.SQLException;

/**
 * The base Firebird Service API functionality.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public interface ServiceManager extends ServiceConnectionProperties {

    /**
     * Sets the database path for the connection to the service manager.
     * <p>
     * Will also set the {@code expectedDb} property. If a different value must be used, it must be set <em>after</em>
     * calling this method.
     * </p>
     *
     * @param database
     *         path for the connection to the service manager.
     */
    void setDatabase(String database);

    /**
     * Returns the database path for the connection to the service manager.
     *
     * @return the database path for the connection to the service manager.
     */
    String getDatabase();

    /**
     * Returns the host for the connection to the service manager.
     *
     * @return the host for the connection to the service manager.
     * @deprecated Use {@link #getServerName()}; will be removed in Jaybird 6 or later
     */
    @Deprecated
    String getHost();

    /**
     * Sets the host for the connection to the service manager.
     * <p>
     * See {@link AttachmentProperties#setServerName(String)} for details.
     * </p>
     *
     * @param host
     *         for the connection to the service manager.
     * @deprecated Use {@link #setServerName(String)}; will be removed in Jaybird 6 or later
     */
    @Deprecated
    void setHost(String host);

    /**
     * Returns the port for the connection to the service manager.
     *
     * @return the port for the connection to the service manager.
     * @deprecated Use {@link #getPortNumber()}; will be removed in Jaybird 6 or later
     */
    @Deprecated
    int getPort();

    /**
     * Sets the port for the connection to the service manager.
     *
     * @param port
     *         for the connection to the service manager.
     * @deprecated Use {@link #setPortNumber(int)}; will be removed in Jaybird 6 or later
     */
    @Deprecated
    void setPort(int port);

    /**
     * Get the wire encryption level.
     *
     * @return Wire encryption level
     * @since 5
     */
    WireCrypt getWireCryptAsEnum();

    /**
     * Set the wire encryption level.
     *
     * @param wireCrypt Wire encryption level ({@code null} not allowed)
     * @since 5
     */
    void setWireCryptAsEnum(WireCrypt wireCrypt);

    /**
     * Returns the logger for the connection to the service manager.
     *
     * @return the logger for the connection to the service manager.
     */
    OutputStream getLogger();

    /**
     * Sets the logger for the connection to the service manager.
     *
     * @param logger
     *         for the connection to the service manager.
     */
    void setLogger(OutputStream logger);

    /**
     * Obtains the server version through a service call.
     *
     * @return Parsed server version, or {@link org.firebirdsql.gds.impl.GDSServerVersion#INVALID_VERSION} if parsing
     * failed.
     * @throws SQLException
     *         For errors connecting to the service manager.
     */
    GDSServerVersion getServerVersion() throws SQLException;
}
