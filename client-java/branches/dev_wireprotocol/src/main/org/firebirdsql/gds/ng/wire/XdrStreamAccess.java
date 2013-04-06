/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbException;

/**
 * Provides access to the {@link XdrInputStream} and {@link XdrOutputStream}.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public interface XdrStreamAccess {

    /**
     * Gets the XdrInputStream.
     * 
     * @return Instance of XdrInputStream
     * @throws FbException
     *             If no connection is opened or when exceptions occur
     *             retrieving the InputStream
     */
    XdrInputStream getXdrIn() throws FbException;
    
    /**
     * Gets the XdrOutputStream.
     * 
     * @return Instance of XdrOutputStream
     * @throws FbException
     *             If no connection is opened or when exceptions occur
     *             retrieving the OutputStream
     */
    XdrOutputStream getXdrOut() throws FbException;
}
