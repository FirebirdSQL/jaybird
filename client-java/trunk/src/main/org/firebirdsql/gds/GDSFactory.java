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
import java.io.Serializable;


/**
 * The class <code>GDSFactory</code> exists to provide a way
 * to obtain objects implementing GDS and Clumplet.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class GDSFactory
    {
    public static class GdsType implements Serializable
        {
        private static int nextOrdinal = 0;

        /**
         * Type 4 GDS Implementation.
         */
        public static final GdsType PURE_JAVA = new GdsType();

        /**
         * Type 2 GDS Implementation. Expects to be able to find fbembed.dll
         */
        public static final GdsType NATIVE_EMBEDDED = new GdsType();

        /**
         * Type 2 GDS Implementation. Expects to be able to find fbclient.dll or gds32.dll in that order.
         */
        public static final GdsType NATIVE = new GdsType();


        private static final GdsType[] PRIVATE_VALUES = new GdsType[]{PURE_JAVA, NATIVE_EMBEDDED, NATIVE};

        private GdsType()
            {
            ordinal = nextOrdinal++;
            }

        public Object readResolve()
            {
            return PRIVATE_VALUES[ordinal];
            }

        final int ordinal;
        }


    public static GDS getDefaultGDS()
        {
        return getGDSForType(GdsType.PURE_JAVA);
        }

    public synchronized static GDS getGDSForType(GdsType gdsType)
        {
        GDS gds = (GDS) gdsTypeToGdsInstanceMap.get(gdsType);
        if (gds == null)
            {
            gds = createGDSForType(gdsType);
            gdsTypeToGdsInstanceMap.put(gdsType, gds);
            }

        return gds;
        }

    private static GDS createGDSForType(GdsType gdsType)
        {
        if (gdsType == GdsType.PURE_JAVA)
            return new GDS_Impl();
        else if (gdsType == GdsType.NATIVE || gdsType == GdsType.NATIVE_EMBEDDED)
            return new org.firebirdsql.ngds.GDS_Impl(gdsType);
        else
            throw new java.lang.IllegalArgumentException("gdsType not recognized.");
        }

    private static final Map gdsTypeToGdsInstanceMap = new HashMap();
    }

