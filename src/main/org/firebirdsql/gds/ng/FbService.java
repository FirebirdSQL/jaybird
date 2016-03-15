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

import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.ng.listeners.ExceptionListenable;
import org.firebirdsql.gds.ng.listeners.ServiceListener;

import java.sql.SQLException;

/**
 * Connection handle to a service.
 * <p>
 * All methods defined in this interface are required to notify all {@code SQLException} thrown from the methods
 * defined in this interface, and those exceptions notified by all {@link ExceptionListenable} implementations created
 * from them.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
     *         Service parameters
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
    <T> T getServiceInfo(ServiceParameterBuffer serviceParameterBuffer, ServiceRequestBuffer serviceRequestBuffer,
            int bufferLength, InfoProcessor<T> infoProcessor)
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
    byte[] getServiceInfo(ServiceParameterBuffer serviceParameterBuffer, ServiceRequestBuffer serviceRequestBuffer,
            int maxBufferLength) throws SQLException;

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
    void addServiceListener(ServiceListener listener);

    /**
     * Removes a {@link ServiceListener} instance from this database.
     *
     * @param listener
     *         Database Listener
     */
    void removeServiceListener(ServiceListener listener);
}
