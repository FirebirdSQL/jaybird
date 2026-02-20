// SPDX-FileCopyrightText: Copyright 2015-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.ng.listeners.ExceptionListenable;
import org.firebirdsql.gds.ng.listeners.ServiceListener;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;

/**
 * Connection handle to a service.
 * <p>
 * All methods defined in this interface are required to notify all {@code SQLException} thrown from the methods
 * defined in this interface, and those exceptions notified by all {@link ExceptionListenable} implementations created
 * from them.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface FbService extends FbAttachment {

    /**
     * @return The service handle value
     */
    @Override
    int getHandle();

    /**
     * Request service info (service query).
     *
     * @param serviceParameterBuffer
     *         Service parameters (can be null)
     * @param serviceRequestBuffer
     *         Service request info
     * @param bufferLength
     *         Response buffer length to use
     * @param infoProcessor
     *         Implementation of {@link InfoProcessor} to transform
     *         the info response
     * @return Transformed info response of type T
     * @throws SQLException
     *         For errors retrieving or transforming the response.
     */
    <T extends @Nullable Object> T getServiceInfo(@Nullable ServiceParameterBuffer serviceParameterBuffer,
            ServiceRequestBuffer serviceRequestBuffer, int bufferLength, InfoProcessor<T> infoProcessor)
            throws SQLException;

    /**
     * Performs a service info request (service query.
     *
     * @param serviceParameterBuffer
     *         Service parameters (can be null)
     * @param serviceRequestBuffer
     *         Service request info
     * @param maxBufferLength
     *         Maximum response buffer length to use
     * @return The response buffer (note: length is the actual length of the response, not {@code maxBufferLength}
     * @throws SQLException
     *         For errors retrieving the information.
     */
    byte[] getServiceInfo(@Nullable ServiceParameterBuffer serviceParameterBuffer,
            ServiceRequestBuffer serviceRequestBuffer, int maxBufferLength) throws SQLException;

    /**
     * Starts a service action.
     *
     * @param serviceRequestBuffer
     *         Service action request details
     * @throws SQLException
     *         For errors starting the service action.
     */
    void startServiceAction(ServiceRequestBuffer serviceRequestBuffer) throws SQLException;

    /**
     * Creates an empty {@link ServiceParameterBuffer}.
     * <p>
     * Attach expects a service parameter buffer to have the version as the first item. This needs to be added
     * explicitly.
     * </p>
     *
     * @return Service
     */
    ServiceParameterBuffer createServiceParameterBuffer();

    /**
     * @return An empty service request buffer
     */
    ServiceRequestBuffer createServiceRequestBuffer();

    /**
     * Adds a {@link ServiceListener} instance to this database.
     *
     * @param listener
     *         Database listener
     */
    @SuppressWarnings("unused")
    void addServiceListener(ServiceListener listener);

    /**
     * Removes a {@link ServiceListener} instance from this database.
     *
     * @param listener
     *         Database Listener
     */
    @SuppressWarnings("unused")
    void removeServiceListener(ServiceListener listener);
}
