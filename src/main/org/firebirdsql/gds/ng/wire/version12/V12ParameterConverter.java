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
package org.firebirdsql.gds.ng.wire.version12;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.impl.DatabaseParameterBufferImp;
import org.firebirdsql.gds.impl.ServiceParameterBufferImp;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.gds.ng.wire.WireServiceConnection;
import org.firebirdsql.gds.ng.wire.version11.V11ParameterConverter;

import static org.firebirdsql.gds.ISCConstants.isc_spb_current_version;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.ParameterConverter} for the version 12 protocol.
 * <p>
 * Adds support for {@code isc_dpb_utf8_filename} and encodes all string properties in UTF-8.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V12ParameterConverter extends V11ParameterConverter {

    protected DatabaseParameterBuffer createDatabaseParameterBuffer(WireDatabaseConnection connection) {
        final Encoding stringEncoding = connection.getEncodingFactory().getEncodingForFirebirdName("UTF8");
        DatabaseParameterBuffer dpb = new DatabaseParameterBufferImp(stringEncoding);
        dpb.addArgument(ISCConstants.isc_dpb_utf8_filename, 1);
        return dpb;
    }

    protected ServiceParameterBuffer createServiceParameterBuffer(WireServiceConnection connection) {
        final Encoding stringEncoding = connection.getEncodingFactory().getEncodingForFirebirdName("UTF8");
        ServiceParameterBuffer spb = new ServiceParameterBufferImp(stringEncoding);
        spb.addArgument(isc_spb_current_version);
        spb.addArgument(ISCConstants.isc_dpb_utf8_filename, 1);
        return spb;
    }
}
