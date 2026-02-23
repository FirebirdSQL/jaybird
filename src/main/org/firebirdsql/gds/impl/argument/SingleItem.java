/*
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2004 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.gds.impl.argument;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ParameterBuffer;
import org.firebirdsql.gds.impl.ParameterBufferMetaData;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.util.EnumSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * {@link Argument} implementation for items without a value.
 */
public final class SingleItem extends TypedArgument {

    private static final Set<ArgumentType> SUPPORTED_ARGUMENT_TYPES = unmodifiableSet(
            EnumSet.of(ArgumentType.TraditionalDpb, ArgumentType.Wide, ArgumentType.SingleTpb));
    @Serial
    private static final long serialVersionUID = -8732644692849743977L;
    
    public SingleItem(int item, ArgumentType argumentType) {
        super(item, checkArgumentType(argumentType, SUPPORTED_ARGUMENT_TYPES));
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        outputStream.write(getType());
        argumentType.writeLength(0, outputStream);
    }

    public int getLength() {
        return 1 + argumentType.getLengthSize();
    }

    @Override
    public SingleItem transformTo(ParameterBufferMetaData parameterBufferMetaData) {
        ArgumentType newArgumentType = parameterBufferMetaData.getSingleArgumentType(getType());
        if (newArgumentType == argumentType) return this;
        return new SingleItem(getType(), newArgumentType);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) return true;
        if (!(other instanceof final SingleItem otherSingleItem)) return false;
        return this.getType() == otherSingleItem.getType();
    }

    @Override
    public int hashCode() {
        return getType();
    }

    @Override
    public void copyTo(ParameterBuffer buffer, @Nullable Encoding encoding) {
        buffer.addArgument(getType());
    }
}
