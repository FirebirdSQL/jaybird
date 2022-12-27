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
package org.firebirdsql.jaybird.props;

import org.firebirdsql.gds.ParameterBuffer;
import org.firebirdsql.jaybird.props.def.ConnectionPropertyType;

/**
 * Identifies the database (or service attach) parameter buffer type of a connection property.
 * <p>
 * In general, there is a 1-on-1 correspondence with {@link ConnectionPropertyType}, but in some cases mapping might be
 * different (current expectation: boolean connection properties that map to either a single item or a 0 / 1 value).
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public enum DpbType {

    /**
     * Parameter value is a string.
     */
    STRING {
        @Override
        public void addValue(ParameterBuffer pb, int pbItem, Object value, ConnectionPropertyType type) {
            pb.addArgument(pbItem, type.asString(value));
        }
    },
    /**
     * Parameter value is an integer.
     */
    INT {
        @Override
        public void addValue(ParameterBuffer pb, int pbItem, Object value, ConnectionPropertyType type) {
            pb.addArgument(pbItem, type.asInteger(value));
        }
    },
    /**
     * Parameter value as a byte.
     */
    BYTE {
        @Override
        public void addValue(ParameterBuffer pb, int pbItem, Object value, ConnectionPropertyType type) {
            pb.addArgument(pbItem, type.asInteger(value).byteValue());
        }
    },
    /**
     * Parameter value is not sent (just presence or absence of the parameter item), corresponding value must be a
     * {@code Boolean}.
     */
    SINGLE {
        @Override
        public void addValue(ParameterBuffer pb, int pbItem, Object value, ConnectionPropertyType type) {
            if (type.asBoolean(value)) {
                pb.addArgument(pbItem);
            }
        }
    },
    /**
     * Parameter is not sent at all (primarily to have a null-safe value for connection properties that should not be
     * sent to the server).
     */
    NONE {
        @Override
        public void addValue(ParameterBuffer pb, int pbItem, Object value, ConnectionPropertyType type) {
            // do nothing
        }
    };

    /**
     * Populate the parameter buffer {@code pb} with {@code pbItem} and {@code value}.
     *
     * @param pb Parameter buffer
     * @param pbItem Parameter buffer item
     * @param value Value
     * @param type Connection property type
     */
    public abstract void addValue(ParameterBuffer pb, int pbItem, Object value, ConnectionPropertyType type);

}
