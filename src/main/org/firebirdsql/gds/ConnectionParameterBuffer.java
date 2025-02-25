// SPDX-FileCopyrightText: Copyright 2015-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds;

import org.firebirdsql.encodings.Encoding;

/**
 * @author Mark Rotteveel
 */
public interface ConnectionParameterBuffer extends ParameterBuffer {

    /**
     * @return The tag mapping.
     */
    ParameterTagMapping getTagMapping();

    /**
     * @return The default encoding of string properties in this parameter buffer.
     */
    Encoding getDefaultEncoding();
}
