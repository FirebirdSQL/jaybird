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
package org.firebirdsql.jdbc.field;

/**
 * Holder for the instance of {@link ObjectConverter} to use.
 * <p>
 * This implementation is for JDBC 4.2.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
enum ObjectConverterHolder {
    INSTANCE(new JDBC42ObjectConverter());

    private final ObjectConverter objectConverter;

    private ObjectConverterHolder(ObjectConverter objectConverter) {
        this.objectConverter = objectConverter;
    }

    ObjectConverter getObjectConverter() {
        return objectConverter;
    }
}
