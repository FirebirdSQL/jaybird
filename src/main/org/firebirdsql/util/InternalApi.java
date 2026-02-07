// SPDX-FileCopyrightText: Copyright 2018-2019 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.util;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the annotated package, class, method or value is for internal use only.
 * <p>
 * Future versions may radically change, move, or make inaccessible the annotated package, type, or method.
 * </p>
 *
 * @author Mark Rotteveel
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface InternalApi {
}
