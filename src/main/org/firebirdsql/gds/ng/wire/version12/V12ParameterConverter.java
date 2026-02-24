// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version12;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.impl.DatabaseParameterBufferImp.DpbMetaData;
import org.firebirdsql.gds.impl.ServiceParameterBufferImp.SpbMetaData;
import org.firebirdsql.jaybird.fb.constants.DpbItems;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.jaybird.fb.constants.SpbItems;
import org.firebirdsql.gds.impl.DatabaseParameterBufferImp;
import org.firebirdsql.gds.impl.ServiceParameterBufferImp;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.gds.ng.wire.WireServiceConnection;
import org.firebirdsql.gds.ng.wire.version11.V11ParameterConverter;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.ParameterConverter} for the version 12 protocol.
 * <p>
 * Adds support for {@code isc_dpb_utf8_filename} and encodes all string properties in UTF-8.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V12ParameterConverter extends V11ParameterConverter {

    @Override
    protected DatabaseParameterBuffer createDatabaseParameterBuffer(WireDatabaseConnection connection) {
        Encoding stringEncoding = connection.getEncodingFactory().getEncodingForFirebirdName("UTF8");
        var dpb = new DatabaseParameterBufferImp(DpbMetaData.DPB_VERSION_1, stringEncoding);
        dpb.addArgument(DpbItems.isc_dpb_utf8_filename);
        return dpb;
    }

    @Override
    protected ServiceParameterBuffer createServiceParameterBuffer(WireServiceConnection connection) {
        Encoding stringEncoding = connection.getEncodingFactory().getEncodingForFirebirdName("UTF8");
        var spb = new ServiceParameterBufferImp(SpbMetaData.SPB_VERSION_2_ATTACH, stringEncoding);
        spb.addArgument(SpbItems.isc_spb_utf8_filename);
        return spb;
    }
}
