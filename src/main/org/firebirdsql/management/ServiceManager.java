// SPDX-FileCopyrightText: Copyright 2004-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2004 Steven Jardine
// SPDX-FileCopyrightText: Copyright 2016-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.management;

import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.WireCrypt;
import org.firebirdsql.jaybird.props.ServiceConnectionProperties;

import java.io.OutputStream;
import java.sql.SQLException;

/**
 * The base Firebird Service API functionality.
 *
 * @author Roman Rokytskyy
 * @author Steven Jardine
 * @author Mark Rotteveel
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
