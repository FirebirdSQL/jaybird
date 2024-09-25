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
package org.firebirdsql.jdbc.field;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;

/**
 * Instances of this field cache data in auto-commit case if no transaction is
 * yet available and must be flushed before transaction is committed.
 *
 * @author Roman Rokytskyy
 * @version 1.0
 */
@NullMarked
public interface FBFlushableField {

    class CachedObject {
        public final byte @Nullable[] bytes;
        public final @Nullable InputStream binaryStream;
        public final @Nullable Reader characterStream;
        public final long length;

        public CachedObject(byte @Nullable [] bytes, @Nullable InputStream binaryStream,
                @Nullable Reader characterStream, long length) {
            this.bytes = bytes;
            this.binaryStream = binaryStream;
            this.characterStream = characterStream;
            this.length = length;
        }
    }

    /**
     * Flush cached data to the database server.
     *
     * @throws SQLException
     *         if something went wrong.
     */
    void flushCachedData() throws SQLException;

    /**
     * Get cached data.
     *
     * @return cached object of this field.
     * @throws SQLException
     *         if something went wrong.
     */
    byte @Nullable[] getCachedData() throws SQLException;

    CachedObject getCachedObject() throws SQLException;

    void setCachedObject(CachedObject cachedObject) throws SQLException;
}