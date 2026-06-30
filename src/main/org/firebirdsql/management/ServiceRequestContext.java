// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.management;

/**
 * Context information about a service request for {@link ServiceRequestCustomizer}.
 *
 * @param operation
 *         name of the operation (the initiating service method name)
 * @since 7
 */
public record ServiceRequestContext(String operation) {
}
