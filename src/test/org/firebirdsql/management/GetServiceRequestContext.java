// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.management;

import org.firebirdsql.gds.ServiceRequestBuffer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Service request customizer that obtains the service request context for assertions.
 */
final class GetServiceRequestContext implements ServiceRequestCustomizer {

    private ServiceRequestContext serviceRequestContext;

    /**
     * @return last seen service request context, or empty if
     * {@link #customize(ServiceRequestBuffer, ServiceRequestContext)} was not called
     */
    Optional<ServiceRequestContext> lastRequestContext() {
        return Optional.ofNullable(serviceRequestContext);
    }

    @Override
    public void customize(ServiceRequestBuffer serviceRequest, ServiceRequestContext requestContext) {
        serviceRequestContext = requestContext;
    }

    /**
     * Asserts that the last operation is {@code expectedOperation}.
     *
     * @param expectedOperation
     *         expected operation, or {@code null} to assert that there was no last operation
     */
    void assertLastOperation(String expectedOperation) {
        assertEquals(expectedOperation, lastRequestContext().map(ServiceRequestContext::operation).orElse(null),
                "operation");
    }

}
