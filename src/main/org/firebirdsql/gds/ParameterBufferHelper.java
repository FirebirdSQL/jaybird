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
package org.firebirdsql.gds;

import org.firebirdsql.jaybird.fb.constants.TpbItems;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class maps the extended JDBC properties to parameter buffer types (for transaction and database
 * parameter buffers). It uses {@code java.lang.reflection} to determine correct type of the parameter
 * passed to the {@link java.sql.Driver#connect(String, Properties)} method.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 1.0
 */
public class ParameterBufferHelper {

    // TODO Can we get rid of this class entirely?

    public static final String TPB_PREFIX = "isc_tpb_";

    private static final Map<String, Integer> tpbTypes;

    /*
     * Initialize mappings between various GDS constant names and
     * their values. This operation should be executed only once.
     */
    static {
        final Map<String, Integer> tempTpbTypes = new HashMap<>(64);

        final Field[] fields = TpbItems.class.getFields();

        for (Field field : fields) {
            final String name = field.getName();
            if (!(name.startsWith(TPB_PREFIX) && field.getType().equals(int.class))) {
                continue;
            }

            final Integer value;
            try {
                value = field.getInt(null);
            } catch (IllegalAccessException iaex) {
                continue;
            }

            // put the correct parameter name
            tempTpbTypes.put(name.substring(TPB_PREFIX.length()), value);
            // put the full name to tolerate people's mistakes
            tempTpbTypes.put(name, value);
        }

        tpbTypes = Collections.unmodifiableMap(tempTpbTypes);
    }

    /**
     * Get value of TPB parameter for the specified name. This method tries to
     * match string representation of the TPB parameter with its value.
     *
     * @param name
     *         string representation of TPB parameter, can have "isc_tpb_" prefix.
     * @return value corresponding to the specified parameter name or {@code null} if nothing was found.
     */
    public static Integer getTpbParam(String name) {
        return tpbTypes.get(name);
    }

}
