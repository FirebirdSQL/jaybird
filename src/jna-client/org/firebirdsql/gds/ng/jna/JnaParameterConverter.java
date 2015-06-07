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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.impl.DatabaseParameterBufferImp;
import org.firebirdsql.gds.impl.ServiceParameterBufferImp;
import org.firebirdsql.gds.ng.AbstractParameterConverter;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.gds.ng.IServiceProperties;
import org.firebirdsql.gds.ng.ParameterConverter;

import static org.firebirdsql.gds.ISCConstants.isc_spb_current_version;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.ParameterConverter} for JNA.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class JnaParameterConverter extends AbstractParameterConverter implements ParameterConverter {
    @Override
    public DatabaseParameterBuffer toDatabaseParameterBuffer(IConnectionProperties props,
            IEncodingFactory encodingFactory) {
        final DatabaseParameterBuffer dpb = new DatabaseParameterBufferImp();
        final Encoding stringEncoding = encodingFactory.getDefaultEncoding();

        // Map standard properties
        populateDefaultProperties(props, encodingFactory, dpb, stringEncoding);

        // Map non-standard properties
        populateNonStandardProperties(props, dpb, stringEncoding);

        return dpb;
    }

    @Override
    public ServiceParameterBuffer toServiceParameterBuffer(IServiceProperties serviceProperties,
            IEncodingFactory encodingFactory) {
        final ServiceParameterBuffer spb = new ServiceParameterBufferImp();
        spb.addArgument(isc_spb_current_version);
        final Encoding stringEncoding = encodingFactory.getDefaultEncoding();

        // Map standard properties
        populateDefaultProperties(serviceProperties, encodingFactory, spb, stringEncoding);

        return spb;
    }
}
