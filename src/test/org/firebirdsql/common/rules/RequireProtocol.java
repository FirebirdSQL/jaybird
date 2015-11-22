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
package org.firebirdsql.common.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.junit.Assume.assumeTrue;

/**
 * Test rule to require support of a protocol version.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class RequireProtocol implements TestRule {

    private final int protocolVersion;

    public static RequireProtocol requireProtocolVersion(int protocolVersion) {
        return new RequireProtocol(protocolVersion);
    }

    private RequireProtocol(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return apply(statement);
    }

    private Statement apply(final Statement base) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                verifyProtocolVersion();
                base.evaluate();
            }
        };
    }

    private void verifyProtocolVersion() {
        assumeTrue(String.format("Protocol version %s was required, but not supported", protocolVersion),
                getDefaultSupportInfo().supportsProtocol(protocolVersion));
    }
}
