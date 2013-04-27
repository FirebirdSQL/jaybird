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

import org.firebirdsql.gds.ng.FbException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public final class GenericResponse implements Response {

    private final int objectHandle;
    private final long blobId;
    private final byte[] data;
    private final FbException exception;

    public GenericResponse(int objectHandle, long blobId, byte[] data, FbException exception) {
        this.objectHandle = objectHandle;
        this.blobId = blobId;
        this.data = data;
        this.exception = exception;
    }

    public int getObjectHandle() {
        return objectHandle;
    }

    public long getBlobId() {
        return blobId;
    }

    public byte[] getData() {
        return data;
    }

    public FbException getException() {
        return exception;
    }
}