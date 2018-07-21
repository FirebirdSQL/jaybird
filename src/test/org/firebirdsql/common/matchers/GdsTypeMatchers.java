/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.common.matchers;

import org.firebirdsql.gds.impl.jni.EmbeddedGDSFactoryPlugin;
import org.firebirdsql.gds.impl.jni.LocalGDSFactoryPlugin;
import org.firebirdsql.gds.impl.jni.NativeGDSFactoryPlugin;
import org.firebirdsql.gds.impl.oo.OOGDSFactoryPlugin;
import org.firebirdsql.gds.impl.wire.WireGDSFactoryPlugin;
import org.hamcrest.Matcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isIn;

/**
 * Matchers for checking GDS types (eg for type specific assumptions).
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class GdsTypeMatchers {

    private static final List<String> PURE_JAVA_TYPES = Collections.unmodifiableList(
            Arrays.asList(WireGDSFactoryPlugin.PURE_JAVA_TYPE_NAME, OOGDSFactoryPlugin.TYPE_NAME));
    private static final List<String> OTHER_NATIVE_TYPES = Collections.unmodifiableList(
            Arrays.asList(NativeGDSFactoryPlugin.NATIVE_TYPE_NAME, LocalGDSFactoryPlugin.LOCAL_TYPE_NAME));

    /**
     * @return Matcher for pure java types
     */
    public static Matcher<String> isPureJavaType() {
        return isIn(PURE_JAVA_TYPES);
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
        return isIn(OTHER_NATIVE_TYPES);
    }

}
