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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.GDSException;

/**
 * TODO Revise hierarchy to not include GDSException
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public class FbException extends GDSException {

    private static final long serialVersionUID = -6369267917574908312L;

    public FbException(int type, int intParam) {
        super(type, intParam);
    }

    public FbException(int type, String strParam) {
        super(type, strParam);
    }

    public FbException(int type, int fbErrorCode, String strParam) {
        super(type, fbErrorCode, strParam);
    }
    
    public FbException(int type, int fbErrorCode, String strParam, Throwable cause) {
        super(type, fbErrorCode, strParam);
        initCause(cause);
    }

    public FbException(int fbErrorCode) {
        super(fbErrorCode);
    }
    
    public FbException(int fbErrorCode, Throwable cause) {
        super(fbErrorCode);
        initCause(cause);
    }

    public FbException(String message) {
        super(message);
    }
    
    public FbException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

}
