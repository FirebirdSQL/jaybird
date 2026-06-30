// SPDX-FileCopyrightText: Copyright 2004-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2004 Steven Jardine
// SPDX-FileCopyrightText: Copyright 2016-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.management;

import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.WireCrypt;
import org.firebirdsql.jaybird.props.ServiceConnectionProperties;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

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
     * <p>
     * NOTE: This method is marked {@code @NullUnmarked} because the nullability of this property is questionable. For
     * services that require a database path, setting {@code null} may result in failure on execute when executing the
     * service action. Subinterfaces and their implementations may declare themselves to be {@code @NonNull} and throw
     * a {@code NullPointerException} on receiving {@code null}.
     * </p>
     *
     * @param database
     *         path for the connection to the service manager.
     */
    @NullUnmarked
    void setDatabase(String database);

    /**
     * Returns the database path for the connection to the service manager.
     *
     * @return the database path for the connection to the service manager.
     */
    @Nullable String getDatabase();

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
    @Nullable OutputStream getLogger();

    /**
     * Sets the logger for the connection to the service manager.
     *
     * @param logger
     *         for the connection to the service manager.
     */
    void setLogger(@Nullable OutputStream logger);

    /**
     * Sets a service request customizer on this service manager. This replaces any previously set customizer.
     * <p>
     * The customizer is called just before the request is sent to the server, allowing users to modify the request.
     * </p>
     * <p>
     * If you set a customizer to access a feature not implemented by Jaybird, please consider creating an improvement
     * ticket on <a href="https://github.com/FirebirdSQL/jaybird/issues">the Jaybird GitHub repository</a> as well.
     * </p>
     *
     * @param customizer
     *         service request customizer, {@code null} to remove a previous customizer
     * @see #getServiceRequestCustomizer()
     * @see ServiceRequestCustomizer
     * @since 7
     */
    void setServiceRequestCustomizer(@Nullable ServiceRequestCustomizer customizer);

    /**
     * @return service request customizer, {@code null} if none set
     * @see #setServiceRequestCustomizer(ServiceRequestCustomizer)
     * @since 7
     */
    @Nullable ServiceRequestCustomizer getServiceRequestCustomizer();

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
