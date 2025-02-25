// SPDX-FileCopyrightText: Copyright 2018-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.matchers;

import org.firebirdsql.gds.impl.jni.EmbeddedGDSFactoryPlugin;
import org.firebirdsql.gds.impl.jni.NativeGDSFactoryPlugin;
import org.firebirdsql.gds.impl.wire.WireGDSFactoryPlugin;
import org.hamcrest.Matcher;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;

/**
 * Matchers for checking GDS types (eg for type specific assumptions).
 *
 * @author Mark Rotteveel
 */
public class GdsTypeMatchers {

    private static final List<String> PURE_JAVA_TYPES = List.of(WireGDSFactoryPlugin.PURE_JAVA_TYPE_NAME);
    private static final List<String> OTHER_NATIVE_TYPES = List.of(NativeGDSFactoryPlugin.NATIVE_TYPE_NAME);

    /**
     * @return Matcher for pure java types
     */
    public static Matcher<String> isPureJavaType() {
        return is(in(PURE_JAVA_TYPES));
    }

    /**
     * @return Matcher for embedded types
     */
    public static Matcher<String> isEmbeddedType() {
        return equalTo(EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);
    }

    /**
     * @return Matcher for native types (excluding embedded)
     */
    public static Matcher<String> isOtherNativeType() {
        return is(in(OTHER_NATIVE_TYPES));
    }

}
