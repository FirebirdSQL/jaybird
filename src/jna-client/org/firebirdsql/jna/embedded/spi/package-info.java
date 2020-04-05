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
 * Defines a service provider interface to obtain Firebird Embedded library files from the class path.
 * <p>
 * See also {@link org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider}.
 * </p>
 * <p>
 * This feature is defined in <a href="https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2020-05-firebird-embedded-locator-service-provider.md">jdp-2020-05:
 * Firebird Embedded locator service provider</a>.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
package org.firebirdsql.jna.embedded.spi;