// SPDX-FileCopyrightText: Copyright 2022-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl.argument;

import java.io.Serial;

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
}
