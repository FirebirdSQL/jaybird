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
package org.firebirdsql.jna.embedded.spi;

/**
 * Exception to signal errors when loading or identifying a Firebird Embedded library.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public class FirebirdEmbeddedLoadingException extends Exception {

    private static final long serialVersionUID = 1L;

    public FirebirdEmbeddedLoadingException(String message) {
        super(message);
    }

    public FirebirdEmbeddedLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

}
