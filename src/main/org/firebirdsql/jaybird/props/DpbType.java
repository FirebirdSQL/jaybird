// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jaybird.props;

import org.firebirdsql.gds.ParameterBuffer;
import org.firebirdsql.jaybird.props.def.ConnectionPropertyType;

import java.util.Objects;

/**
 * Identifies the database (or service attach) <em>parameter buffer type</em> of a connection property.
 * <p>
 * In general, there is a 1-on-1 correspondence with {@link ConnectionPropertyType}, but in some cases mapping might be
 * different (current expectation: boolean connection properties that map to either a single item or a 0 / 1 value).
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
public enum DpbType {

    /**
     * Parameter value is a string.
     */
    STRING {
        @Override
        public void addValue(ParameterBuffer pb, int pbItem, Object value, ConnectionPropertyType type) {
            //noinspection DataFlowIssue : if value is non-null, then type.asString is also non-null
            pb.addArgument(pbItem, type.asString(value));
        }
    },
    /**
     * Parameter value is an integer.
     */
    INT {
        @Override
        public void addValue(ParameterBuffer pb, int pbItem, Object value, ConnectionPropertyType type) {
            //noinspection DataFlowIssue : if value is non-null, then type.asInteger is also non-null
            pb.addArgument(pbItem, type.asInteger(value));
        }
    },
    /**
     * Parameter value as a byte.
     */
    BYTE {
        @Override
        public void addValue(ParameterBuffer pb, int pbItem, Object value, ConnectionPropertyType type) {
            //noinspection DataFlowIssue : if value is non-null, then type.asInteger is also non-null
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
            if (Objects.equals(Boolean.TRUE, type.asBoolean(value))) {
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
