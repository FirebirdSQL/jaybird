// SPDX-FileCopyrightText: Copyright 2022-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl.argument;

import java.io.Serial;
import java.util.Collection;

/**
 * Argument with an argument type.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public abstract class TypedArgument extends Argument {

    @Serial
    private static final long serialVersionUID = -6422646924006860740L;
    
    final ArgumentType argumentType;

    TypedArgument(int type, ArgumentType argumentType) {
        super(type);
        this.argumentType = argumentType;
    }

    /**
     * Checks if {@code argumentType} is contained in {@code supportedArgumentTypes}.
     *
     * @param argumentType
     *         argument type
     * @param supportedArgumentTypes
     *         collection of argument types that are supported
     * @return {@code argumentType}, if supported
     * @throws IllegalArgumentException
     *         if {@code argumentType} is not supported
     * @since 7
     */
    static ArgumentType checkArgumentType(ArgumentType argumentType, Collection<ArgumentType> supportedArgumentTypes) {
        if (!supportedArgumentTypes.contains(argumentType)) {
            throw new IllegalArgumentException("Invalid argument type: " + argumentType);
        }
        return argumentType;
    }
}
