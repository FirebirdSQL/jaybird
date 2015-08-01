/*
 * $Id$
 *
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
package org.firebirdsql.gds.ng.wire.version13;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.DatabaseParameterBufferImp;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.gds.ng.wire.version12.V12ParameterConverter;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.ParameterConverter} for the version 13 protocol.
 * <p>
 * Adds support for {@code isc_dpb_utf8_filename} and encodes all string properties in UTF-8.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V13ParameterConverter extends V12ParameterConverter {
    @Override
    public DatabaseParameterBuffer toDatabaseParameterBuffer(IConnectionProperties props,
            IEncodingFactory encodingFactory) {
        final DatabaseParameterBuffer dpb = new DatabaseParameterBufferImp();
        final Encoding stringEncoding = encodingFactory.getEncodingForFirebirdName("UTF8");

        dpb.addArgument(ISCConstants.isc_dpb_utf8_filename, 1);

        // Map standard properties
        populateDefaultProperties(props, encodingFactory, dpb, stringEncoding);

        // Map non-standard properties
        populateNonStandardProperties(props, dpb, stringEncoding);

        return dpb;
    }
}
