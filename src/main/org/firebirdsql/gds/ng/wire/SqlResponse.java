// SPDX-FileCopyrightText: Copyright 2013-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire;

/**
 * @param count
 *         number of rows following (either {@code 0} or {@code 1})
 * @author Mark Rotteveel
 * @since 3.0
 */
public record SqlResponse(int count) implements Response {
}
