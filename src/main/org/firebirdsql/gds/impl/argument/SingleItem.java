/*
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2004 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2012-2023 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.gds.impl.argument;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ParameterBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;

/**
 * {@link Argument} implementation for items without a value.
 */
public final class SingleItem extends TypedArgument {

    @Serial
    private static final long serialVersionUID = -8732644692849743977L;
    
    public SingleItem(int item, ArgumentType argumentType) {
        super(item, argumentType);
        if (argumentType != ArgumentType.Wide && argumentType != ArgumentType.TraditionalDpb
                && argumentType != ArgumentType.SingleTpb) {
            throw new IllegalArgumentException("Invalid argument type: " + argumentType);
        }
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        outputStream.write(getType());
        argumentType.writeLength(0, outputStream);
    }

    public int getLength() {
        return 1 + argumentType.getLengthSize();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof final SingleItem otherSingleItem)) return false;
        return this.getType() == otherSingleItem.getType();
    }

    @Override
    public int hashCode() {
        return getType();
    }

    @Override
    public void copyTo(ParameterBuffer buffer, Encoding encoding) {
        buffer.addArgument(getType());
    }
}
