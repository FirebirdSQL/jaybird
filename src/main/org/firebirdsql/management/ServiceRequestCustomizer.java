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
 * @since 5.0.13
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
