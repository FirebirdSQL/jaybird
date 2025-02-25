// SPDX-FileCopyrightText: Copyright 2013 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;

import java.sql.SQLException;

/**
 * Provides access to the {@link XdrInputStream} and {@link XdrOutputStream}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface XdrStreamAccess {

    /**
     * Gets the XdrInputStream.
     *
     * @return Instance of XdrInputStream
     * @throws SQLException
     *         If no connection is opened or when exceptions occur
     *         retrieving the InputStream
     */
    XdrInputStream getXdrIn() throws SQLException;

    /**
     * Gets the XdrOutputStream.
     *
     * @return Instance of XdrOutputStream
     * @throws SQLException
     *         If no connection is opened or when exceptions occur
     *         retrieving the OutputStream
     */
    XdrOutputStream getXdrOut() throws SQLException;
}
