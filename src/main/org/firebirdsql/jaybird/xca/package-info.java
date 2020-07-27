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

/**
 * XCA (or ex-connector-architecture), is an internal API of Jaybird for connection management.
 * <p>
 * Historically it was derived from the JavaEE Connector Architecture specification, but that tie has been cut
 * since Jaybird 5.
 * </p>
 * <p>
 * All classes, interfaces and other constructs in this package should be considered internal API of Jaybird, and may
 * change radically between point releases. Do not use it in your own code.
 * </p>
 */
@InternalApi
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.util.InternalApi;