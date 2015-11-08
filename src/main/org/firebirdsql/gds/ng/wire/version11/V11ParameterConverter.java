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
package org.firebirdsql.gds.ng.wire.version11;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.gds.ng.wire.version10.V10ParameterConverter;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.SQLException;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.ParameterConverter} for the version 11 protocol.
 * <p>
 * Adds support for including the process name and process id from the system properties
 * {@code org.firebirdsql.jdbc.processName} and {@code org.firebirdsql.jdbc.pid}
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V11ParameterConverter extends V10ParameterConverter {

    @Override
    protected void populateDefaultProperties(final WireDatabaseConnection connection, final DatabaseParameterBuffer dpb) throws SQLException {
        super.populateDefaultProperties(connection, dpb);

        addProcessName(dpb);
        addProcessId(dpb);
    }

    /**
     * Adds the processName to the dpb, if available.
     *
     * @param dpb
     *         Database parameter buffer
     */
    protected final void addProcessName(DatabaseParameterBuffer dpb) {
        String processName = getSystemPropertyPrivileged("org.firebirdsql.jdbc.processName");
        if (processName != null) {
            dpb.addArgument(DatabaseParameterBuffer.PROCESS_NAME, processName);
        }
    }

    /**
     * Adds the processId (pid) to the dpb, if available.
     *
     * @param dpb
     *         Database Database parameter buffer
     */
    protected final void addProcessId(DatabaseParameterBuffer dpb) {
        String pidStr = getSystemPropertyPrivileged("org.firebirdsql.jdbc.pid");
        if (pidStr != null) {
            try {
                int pid = Integer.parseInt(pidStr);
                dpb.addArgument(DatabaseParameterBuffer.PROCESS_ID, pid);
            } catch (NumberFormatException ex) {
                // ignore
            }
        }
    }

    private static String getSystemPropertyPrivileged(final String propertyName) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(propertyName);
            }
        });
    }
}
