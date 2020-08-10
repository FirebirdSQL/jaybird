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

import org.firebirdsql.jaybird.props.ServiceConnectionProperties;

/**
 * Connection properties for a Firebird service attachment.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface IServiceProperties extends IAttachProperties<IServiceProperties>, ServiceConnectionProperties {

    String DEFAULT_SERVICE_NAME = "service_mgr";

    /**
     * Get the service name
     * <p>
     * NOTE: Implementer should take care to return {@link #DEFAULT_SERVICE_NAME} if
     * value hasn't been set yet.
     * </p>
     *
     * @return Service name
     */
    String getServiceName();

    /**
     * Set the service name.
     * <p>
     * NOTE: Implementer should take care to use the {@link #DEFAULT_SERVICE_NAME} if
     * this method hasn't been called yet.
     * </p>
     *
     * @param serviceName Service name
     */
    void setServiceName(String serviceName);

    /**
     * @return An immutable version of this instance as an implementation of {@link IServiceProperties}
     */
    @Override
    IServiceProperties asImmutable();

    /**
     * @return A new, mutable, instance as an implementation of {@link IServiceProperties} with all properties copied.
     */
    @Override
    IServiceProperties asNewMutable();
}
