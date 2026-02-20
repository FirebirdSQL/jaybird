// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
/**
 * Implementation of fields for getting or setting result set columns or prepared statement parameters.
 */
@InternalApi
// Most methods accept and/or return null, so annotating those that are non-null is less invasive
@NullUnmarked
package org.firebirdsql.jdbc.field;

import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullUnmarked;