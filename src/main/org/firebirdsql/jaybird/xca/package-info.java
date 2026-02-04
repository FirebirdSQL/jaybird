// SPDX-FileCopyrightText: Copyright 2022-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
/**
 * XCA (or ex-connector-architecture), is an internal API of Jaybird for connection management.
 * <p>
 * Historically it was derived from the JavaEE Connector Architecture specification, but that tie has been cut
 * since Jaybird 5.
 * </p>
 * <p>
 * All classes, interfaces and other constructs in this package should be considered internal API of Jaybird, and may
 * change radically between point releases. Do not use it in your own code.
 * </p>
 */
@InternalApi
@NullMarked
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullMarked;