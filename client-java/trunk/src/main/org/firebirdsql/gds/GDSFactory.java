/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */

package org.firebirdsql.gds;


import org.firebirdsql.jgds.GDS_Impl;

import java.util.Map;
import java.util.HashMap;

/**
 * The class <code>GDSFactory</code> exists to provide a way
 * to obtain objects implementing GDS and Clumplet.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class GDSFactory
    {

    public static GDS getDefaultGDS()
        {
        return getGDSForType(GDSType.PURE_JAVA);
        }

    public synchronized static GDS getGDSForType(GDSType gdsType)
        {
        GDS gds = (GDS) gdsTypeToGdsInstanceMap.get(gdsType);
        if (gds == null)
            {
            gds = createGDSForType(gdsType);
            gdsTypeToGdsInstanceMap.put(gdsType, gds);
            }

        return gds;
        }

    private static GDS createGDSForType(GDSType gdsType)
        {
        if (gdsType == GDSType.PURE_JAVA)
            return new GDS_Impl();
        else if (gdsType == GDSType.NATIVE || gdsType == GDSType.NATIVE_LOCAL || gdsType == GDSType.NATIVE_EMBEDDED)
            return new org.firebirdsql.ngds.GDS_Impl(gdsType);
        else
            throw new java.lang.IllegalArgumentException("gdsType not recognized.");
        }

    private static final Map gdsTypeToGdsInstanceMap = new HashMap();

    /** @link dependency */
    /*# AbstractGDS lnkAbstractGDS; */
    }

