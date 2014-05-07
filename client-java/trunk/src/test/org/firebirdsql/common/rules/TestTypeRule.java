/*
 * $Id$
 * 
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
package org.firebirdsql.common.rules;

import org.firebirdsql.common.FBTestProperties;
import org.hamcrest.Matcher;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeThat;

/**
 * A Rule that allows for verifying assumptions of the test (GDS) types required (or excluded) for a test (class).
 * <p>
 * When a test type is not supported, an {@link org.junit.internal.AssumptionViolatedException} will be thrown
 * (by a call to {@link org.junit.Assume#assumeThat(Object, org.hamcrest.Matcher)})
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class TestTypeRule implements TestRule {

    private Matcher<String> testTypeMatcher;

    private TestTypeRule(Matcher<String> testTypeMatcher) {
        this.testTypeMatcher = testTypeMatcher;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return apply(statement);
    }

    private Statement apply(final Statement base) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                verifyTestType();
                base.evaluate();
            }
        };
    }

    private void verifyTestType() {
        String gdsType = FBTestProperties.GDS_TYPE;
        assumeThat("Test type not supported, test ignored", gdsType, testTypeMatcher);
    }

    /**
     * Creates an instance with a list of supported test types.
     *
     * @param supportedTypes The types to be supported
     * @return Instance
     */
    public static TestTypeRule supports(String... supportedTypes) {
        final Set<String> supportedTypesSet = new HashSet<String>(Arrays.asList(supportedTypes));
        return new TestTypeRule(isIn(supportedTypesSet));
    }

    /**
     * Creates an instance with a list of excluded test types.
     *
     * @param excludedTypes The types to be excluded
     * @return Instance
     */
    public static TestTypeRule excludes(String... excludedTypes) {
        final Set<String> excludedTypesSet = new HashSet<String>(Arrays.asList(excludedTypes));
        return new TestTypeRule(not(isIn(excludedTypesSet)));
    }
}
