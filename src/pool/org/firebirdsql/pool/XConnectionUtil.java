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
package org.firebirdsql.pool;

import java.io.IOException;

/**
 * Utility class.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
@Deprecated
public class XConnectionUtil {

    /**
     * Get stack trace of the specified Throwable as string.
     * 
     * @param t throwable.
     * 
     * @return stack trace of that throwable.
     */
    public static String getStackTrace(Throwable t) {
        java.io.StringWriter writer = new java.io.StringWriter();
        try {
            t.printStackTrace(new java.io.PrintWriter(writer));
            writer.flush();
            return writer.toString();
        } finally {
            try {
                writer.close();
            } catch(IOException ex) {
                // this will never happen
            }
        }
    }
}
