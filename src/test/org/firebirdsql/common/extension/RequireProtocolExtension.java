// SPDX-FileCopyrightText: Copyright 2022-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.extension;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static java.lang.String.format;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test extension to require support of a protocol version.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@NullMarked
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
