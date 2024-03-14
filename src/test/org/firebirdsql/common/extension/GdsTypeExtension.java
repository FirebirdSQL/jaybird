/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.common.extension;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSFactoryPlugin;
import org.firebirdsql.gds.impl.jni.NativeGDSFactoryPlugin;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Set;

import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * An extension that allows for verifying assumptions of the test (GDS) types required (or excluded) for a test class.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public class GdsTypeExtension implements BeforeAllCallback {

    private final Matcher<String> testTypeMatcher;

    private GdsTypeExtension(Matcher<String> testTypeMatcher) {
        this.testTypeMatcher = testTypeMatcher;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        assumeThat("Test type not supported, test ignored", FBTestProperties.GDS_TYPE, testTypeMatcher);
    }

    /**
     * Creates an instance with a list of supported test types.
     *
     * @param supportedTypes The types to be supported
     * @return Instance
     */
    public static GdsTypeExtension supports(String... supportedTypes) {
        return new GdsTypeExtension(is(in(Set.of(supportedTypes))));
    }

    /**
     * Creates an instance with a list of excluded test types.
     *
     * @param excludedTypes The types to be excluded
     * @return Instance
     */
    public static GdsTypeExtension excludes(String... excludedTypes) {
        return new GdsTypeExtension(not(in(Set.of(excludedTypes))));
    }

    /**
     * Creates an instance that supports only all (known) native test types.
     *
     * @return Instance
     */
    public static GdsTypeExtension supportsNativeOnly() {
        return supports(NativeGDSFactoryPlugin.NATIVE_TYPE_NAME, EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);
    }

    /**
     * Creates an instance that excludes all (known) native test types.
     *
     * @return Instance
     */
    public static GdsTypeExtension excludesNativeOnly() {
        return excludes(NativeGDSFactoryPlugin.NATIVE_TYPE_NAME, EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);
    }
}
