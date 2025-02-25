// SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
// SPDX-FileCopyrightText: Copyright 2003-2008 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2014-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds;

/**
 * Instance of this interface represents a Database Parameter Buffer from the
 * Firebird API documentation and specifies the attributes for the
 * current connection.
 * <p/>
 * Additionally, it is possible to change some database properties in a permanent
 * way, however this approach is not recommended. Please use instead management
 * API.
 */
public interface DatabaseParameterBuffer extends ConnectionParameterBuffer {

    /**
     * Make a deep copy of this object.
     *
     * @return deep copy of this object.
     */
    DatabaseParameterBuffer deepCopy();
}
