/*
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

import org.firebirdsql.util.InternalApi;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;

/**
 * Provides a default implementation of {@link org.firebirdsql.jdbc.field.ObjectConverter}.
 * <p>
 * This {@link ObjectConverter} provides no conversion.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
@InternalApi
@SuppressWarnings("unused")
final class DefaultObjectConverter implements ObjectConverter {
    /**
     * {@inheritDoc}
     * <p>
     * <b>This implementation always returns <code>false</code>.</b>
     * </p>
     */
    @Override
    public boolean setObject(FBField field, Object object) throws java.sql.SQLException {
        return false;
    }

    @Override
    public <T> T getObject(FBField field, Class<T> type) throws SQLException {
        throw new SQLNonTransientException("Unsupported conversion requested for field " + field.getName() + " requested type: " + type.getName());
    }
}
