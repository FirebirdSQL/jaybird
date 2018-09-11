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

/**
 * Statement parser for generated keys support.
 * <p>
 * <b>DO NOT USE!</b> This packages is for driver-internal purposes only.
 * </p>
 * <p>
 * The parser in this package is not a full implementation of the Firebird SQL dialect. It only serves to obtain the
 * statement information necessary to support the JDBC generated keys feature.
 * </p>
 */
package org.firebirdsql.jdbc.parser;