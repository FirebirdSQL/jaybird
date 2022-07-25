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

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static java.lang.String.format;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test extension to require support of a protocol version.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public class RequireProtocolExtension implements BeforeAllCallback {

    private final int protocolVersion;

    private RequireProtocolExtension(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public static RequireProtocolExtension requireProtocolVersion(int protocolVersion) {
        return new RequireProtocolExtension(protocolVersion);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        assumeTrue(getDefaultSupportInfo().supportsProtocol(protocolVersion),
                () -> format("Protocol version %s was required, but not supported", protocolVersion));
    }

}
