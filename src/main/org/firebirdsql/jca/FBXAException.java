/*
 * Firebird Open Source J2ee connector - jdbc driver
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
package org.firebirdsql.jca;

import javax.transaction.xa.XAException;

/**
 * Convenience exception that adds constructor taking message and error code
 * together. 
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBXAException extends XAException {

    public FBXAException() {
        super();
    }

    public FBXAException(String msg) {
        super(msg);
    }

    public FBXAException(int errorCode) {
        super(errorCode);
    }
    
    public FBXAException(String msg, int errorCode) {
        this(msg);
        
        this.errorCode = errorCode;
    }

}
