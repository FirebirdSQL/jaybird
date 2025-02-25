// SPDX-FileCopyrightText: Copyright 2016 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a test is potentially unstable (may lead to false positives).
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE})
public @interface Unstable {

    /**
     * (optional) explanation for instability.
     */
    String value() default "";
}
