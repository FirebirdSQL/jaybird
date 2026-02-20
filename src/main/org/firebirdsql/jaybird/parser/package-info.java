// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
/**
 * Statement parser for generated keys support.
 * <p>
 * <b>DO NOT USE!</b> This package is for driver-internal purposes only.
 * </p>
 * <p>
 * The parser in this package is not a full implementation of the Firebird SQL dialect. It only serves to obtain the
 * statement information necessary for internal purposes of Jaybird (like generated keys support).
 * </p>
 */
@InternalApi
@NullMarked
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullMarked;