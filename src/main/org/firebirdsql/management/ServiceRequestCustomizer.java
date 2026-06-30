// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.management;

import org.firebirdsql.gds.ServiceRequestBuffer;

/**
 * Functional interface for modifying service requests.
 * <p>
 * This can be used to modify service requests before they are sent to the server, for example to access features not
 * currently supported by Jaybird.
 * </p>
 * <p>
 * If you implement this interface to access a feature not implemented by Jaybird, please consider creating an
 * improvement ticket on <a href="https://github.com/FirebirdSQL/jaybird/issues">the Jaybird GitHub repository</a> as
 * well.
 * </p>
 *
 * @see ServiceManager#setServiceRequestCustomizer(ServiceRequestCustomizer)
 * @since 7
 */
@FunctionalInterface
public interface ServiceRequestCustomizer {

    /**
     * Provides access to, and allows customization of, a service request.
     * <p>
     * Called by the service manager just before the request is sent to the server. Exceptions thrown by the customizer
     * terminate the service request before it's sent to the server.
     * </p>
     * <p>
     * Incorrect use (e.g. adding wrong arguments or values, removing or replacing arguments, etc.) may result in
     * errors on Firebird or in Jaybird.
     * </p>
     *
     * @param serviceRequest
     *         service request buffer populated by the service manager, to be modified by this customizer
     * @param requestContext
     *         service request context information
     */
    void customize(ServiceRequestBuffer serviceRequest, ServiceRequestContext requestContext);

}
