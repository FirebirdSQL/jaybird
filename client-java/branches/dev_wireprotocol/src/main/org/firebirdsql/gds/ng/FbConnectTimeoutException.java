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

/**
 * Exception for the connect timeout.
 * TODO Revise hierarchy if necessary
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public class FbConnectTimeoutException extends FbException {

    private static final long serialVersionUID = 8563535871732562761L;

    public FbConnectTimeoutException(int type, int intParam) {
        super(type, intParam);
    }

    public FbConnectTimeoutException(int type, String strParam) {
        super(type, strParam);
    }

    public FbConnectTimeoutException(int type, int fbErrorCode, String strParam) {
        super(type, fbErrorCode, strParam);
    }
    
    public FbConnectTimeoutException(int type, int fbErrorCode, String strParam, Throwable cause) {
        super(type, fbErrorCode, strParam, cause);
    }

    public FbConnectTimeoutException(int fbErrorCode) {
        super(fbErrorCode);
    }

    public FbConnectTimeoutException(String message) {
        super(message);
    }

    public FbConnectTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
