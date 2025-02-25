// SPDX-FileCopyrightText: Copyright 2013-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ISCConstants;

/**
 * @param status
 *         fetch status ({@link ISCConstants#FETCH_OK} or {@link ISCConstants#FETCH_NO_MORE_ROWS}
 * @param count
 *         number of rows following (in practice, either {@code 0} or {@code 1})
 * @author Mark Rotteveel
 * @since 3.0
 */
public record FetchResponse(int status, int count) implements Response {
}
