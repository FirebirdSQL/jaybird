// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire;

import org.jspecify.annotations.Nullable;

import java.sql.SQLException;

/**
 * @param objectHandle
 *         object handle associated with the response
 * @param blobId
 *         blob id or status value
 * @param data
 *         data
 * @param exception
 *         exception or warning (or {@code null} for no exception or warning)
 * @author Mark Rotteveel
 * @since 3.0
 */
@SuppressWarnings("java:S6218")
public record GenericResponse(int objectHandle, long blobId, byte[] data, @Nullable SQLException exception)
        implements Response {
}
