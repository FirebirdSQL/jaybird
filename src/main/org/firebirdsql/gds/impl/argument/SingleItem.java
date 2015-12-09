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
package org.firebirdsql.gds.impl.argument;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ParameterBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link Argument} implementation for items without a value.
 */
public final class SingleItem extends Argument {

    public SingleItem(int item) {
        super(item);
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        outputStream.write(getType());
    }

    public int getLength() {
        return 1;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof SingleItem))
            return false;

        final SingleItem otherSingleItem = (SingleItem) other;

        return this.getType() == otherSingleItem.getType();
    }

    @Override
    public void copyTo(ParameterBuffer buffer, Encoding encoding) {
        buffer.addArgument(getType());
    }
}
