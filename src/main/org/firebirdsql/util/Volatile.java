// SPDX-FileCopyrightText: Copyright 2018 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.util;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the annotated class, method or value is volatile, and may change in a next version.
 *
 * @author Mark Rotteveel
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface Volatile {

    String reason();
    
}
