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

import java.io.OutputStream;
import java.sql.SQLException;

/**
 * The base Firebird Service API functionality.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine</a>
 */
public interface ServiceManager {

    /**
     * Sets the encoding used for encoding or decoding string values.
     * <p>
     * If not set (or null), defaults to the value of system property {@code file.encoding}/
     * </p>
     *
     * @param charSet Java charset name.
     */
    void setCharSet(String charSet);

    String getCharSet();

    /**
     * Sets the username for the connection to the service manager.
     *
     * @param user
     *         for the connection to the service manager.
     */
    void setUser(String user);

    /**
     * Returns the username for the connection to the service manager.
     *
     * @return the username for the connection to the service manager.
     */
    String getUser();

    /**
     * Sets the password for the connection to the service manager.
     *
     * @param password
     *         for the connection to the service manager.
     */
    void setPassword(String password);

    /**
     * Returns the password for the connection to the service manager.
     *
     * @return the password for the connection to the service manager.
     */
    String getPassword();

    /**
     * Sets the database path for the connection to the service manager.
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
     */
    String getHost();

    /**
     * Sets the host for the connection to the service manager.
     *
     * @param host
     *         for the connection to the service manager.
     */
    void setHost(String host);

    /**
     * Returns the port for the connection to the service manager.
     *
     * @return the port for the connection to the service manager.
     */
    int getPort();

    /**
     * Sets the port for the connection to the service manager.
     *
     * @param port
     *         for the connection to the service manager.
     */
    void setPort(int port);

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
