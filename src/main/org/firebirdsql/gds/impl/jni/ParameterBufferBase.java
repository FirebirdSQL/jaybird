/*
 * $Id$
 *
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
package org.firebirdsql.gds.impl.jni;

/**
 * Provides implementation common to both ServiceParameterBufferImp and
 * ServiceRequestBufferImp
 */
public abstract class ParameterBufferBase extends org.firebirdsql.gds.impl.ParameterBufferBase implements java.io.Serializable {

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ParameterBufferBase))
            return false;

        final ParameterBufferBase otherServiceBufferBase = (ParameterBufferBase) other;

        return otherServiceBufferBase.getArgumentsList().equals(this.getArgumentsList());
    }

    @Override
    public int hashCode() {
        return getArgumentsList().hashCode();
    }
}
