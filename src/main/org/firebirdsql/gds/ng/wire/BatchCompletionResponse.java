// SPDX-FileCopyrightText: Copyright 2022-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.BatchCompletion;

/**
 * Data from the {@link org.firebirdsql.gds.impl.wire.WireProtocolConstants#op_batch_cs} response.
 *
 * @param batchCompletion
 *         batch completion information
 * @author Mark Rotteveel
 * @since 5
 */
public record BatchCompletionResponse(BatchCompletion batchCompletion) implements Response {
}
