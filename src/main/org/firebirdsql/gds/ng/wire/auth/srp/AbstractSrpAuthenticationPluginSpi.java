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
package org.firebirdsql.gds.ng.wire.auth.srp;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.wire.auth.AuthenticationPlugin;
import org.firebirdsql.gds.ng.wire.auth.AuthenticationPluginSpi;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

/**
 * Base class for SRP authentication plugin providers.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
public abstract class AbstractSrpAuthenticationPluginSpi implements AuthenticationPluginSpi {

    private final String pluginName;
    private final String clientProofHashAlgorithm;

    /**
     * Initializes this Srp provider.
     *
     * @param pluginName
     *         Firebird name of the authentication plugin
     * @param clientProofHashAlgorithm
     *         Client proof hash algorithm
     * @throws SQLException
     *         If the {@code clientProofHashAlgorithm} is not supported by the JVM.
     */
    protected AbstractSrpAuthenticationPluginSpi(String pluginName, String clientProofHashAlgorithm)
            throws SQLException {
        checkHashAlgorithmSupported(clientProofHashAlgorithm);
        this.pluginName = pluginName;
        this.clientProofHashAlgorithm = clientProofHashAlgorithm;
    }

    @Override
    public final String getPluginName() {
        return pluginName;
    }

    @Override
    public final AuthenticationPlugin createPlugin() {
        return new SrpAuthenticationPlugin(pluginName, clientProofHashAlgorithm);
    }

    /**
     * Checks if the hash algorithm supplied is supported by the JVM, otherwise it throws an exception.
     *
     * @param hashAlgorithm
     *         Hash algorithm name
     * @throws SQLException
     *         When the hash algorithm is not supported by the JVM.
     */
    private static void checkHashAlgorithmSupported(String hashAlgorithm) throws SQLException {
        try {
            MessageDigest.getInstance(hashAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_hashAlgorithmNotAvailable)
                    .messageParameter(hashAlgorithm)
                    .cause(e)
                    .toFlatSQLException();
        }
    }
}
