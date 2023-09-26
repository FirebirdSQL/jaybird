/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.jaybird.props.def;

import org.firebirdsql.jaybird.props.DpbType;
import org.firebirdsql.jaybird.props.InvalidPropertyValueException;
import org.firebirdsql.jaybird.util.StringUtils;

import java.util.*;

import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;
import static org.firebirdsql.jaybird.util.StringUtils.trimToNull;

/**
 * A Jaybird connection property.
 * <p>
 * Holds information like name, aliases, default value etc for a Jaybird connection property.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class ConnectionProperty {

    public static final int NO_DPB_ITEM = -1;
    public static final int NO_SPB_ITEM = -1;

    private final String name;
    private final List<String> aliases;
    private final ConnectionPropertyType type;
    private final List<String> choices;
    private final DpbType pbType;
    private final int dpbItem;
    private final int spbItem;
    /*
     NOTE: We currently do not provide an option to set a custom validator as there didn't seem to be suitable
     use cases, except maybe encoding and charset, which might result in too much inflexibility, and maybe
     generatedKeysEnabled, but we currently specify behaviour for invalid inputs, i.e. ignore).
     
     If a suitable use case does come up, it can always be added, but for now, we choose simplicity.
    */

    /**
     * Creates a connection property instance.
     *
     * @param builder
     *         Builder with values to populate this instance
     */
    private ConnectionProperty(Builder builder) {
        name = requireNonNull(trimToNull(builder.name), "name");
        aliases = normalizeValues(builder.aliases);
        type = requireNonNull(builder.type, "type");
        choices = normalizeValues(builder.choices);
        pbType = requireNonNull(builder.pbType, "pbType");
        dpbItem = builder.dpbItem;
        spbItem = builder.spbItem;

        if (dpbItem != NO_DPB_ITEM || spbItem != NO_SPB_ITEM) {
            if (pbType == DpbType.NONE) {
                throw new IllegalArgumentException(
                        "dpbType set to NONE while dpbItem is set to " + dpbItem + " and spbItem to " + spbItem);
            }
        } else if (pbType != DpbType.NONE) {
            throw new IllegalArgumentException("dpbType set to " + pbType + " while dpbItem and spbItem not set");
        }
    }

    /**
     * Creates a connection property builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String name) {
        return builder().name(name);
    }

    /**
     * Creates an <em>unknown</em> connection property.
     * <p>
     * An <em>unknown</em> connection property was either not defined but used, or can be used for lookups (given the
     * definition of equals and hashcode). An unknown connection property returned by this method always applies type
     * {@link ConnectionPropertyType#STRING}</p>
     *
     * @param name
     *         Property name
     * @return An unknown property with the specified name
     */
    public static ConnectionProperty unknown(String name) {
        return builder().name(name).build();
    }

    /**
     * @return name of the property
     */
    public String name() {
        return name;
    }

    /**
     * Optional aliases (secondary names) for this property.
     * <p>
     * Aliases are alternative names for the property, either for backwards compatibility or for ease of use.
     * </p>
     *
     * @return aliases for this property, empty means no aliases
     * @see #name()
     */
    public List<String> aliases() {
        return aliases;
    }

    /**
     * Type of the property.
     *
     * @return type of the property
     */
    public ConnectionPropertyType type() {
        return type;
    }

    /**
     * Possible values for this property.
     *
     * @return possible values for this property, or empty when unrestricted
     */
    public List<String> choices() {
        return choices;
    }

    /**
     * Validates {@code value} for this property.
     *
     * @param value
     *         value to validate
     * @return {@code value} when validation passed
     * @throws IllegalArgumentException
     *         When {@code value} is not a valid value for this property
     */
    public <T> T validate(T value) {
        defaultValidate(value);
        return value;
    }

    private void defaultValidate(Object value) {
        if (value == null || choices.isEmpty()) {
            return;
        }

        String valueString = requireNonNull(type.asString(value), "value as string");
        if (choices.stream().noneMatch(valueString::equalsIgnoreCase)) {
            throw InvalidPropertyValueException.invalidProperty(name(), valueString, "valid values are " + choices());
        }
    }

    /**
     * Type of the value when sent in the database (or service attach) parameter buffer.
     *
     * @return type for the parameter buffer
     */
    public DpbType pbType() {
        return pbType;
    }

    /**
     * Database parameter buffer (DPB) item associated with this property.
     *
     * @return database parameter buffer item, or {@link #NO_DPB_ITEM} if no item is associated
     * @see #hasDpbItem()
     */
    public int dpbItem() {
        return dpbItem;
    }

    /**
     * @return {@code true} if this property has a DPB item
     * @see #dpbItem()
     */
    public boolean hasDpbItem() {
        return dpbItem != NO_DPB_ITEM;
    }

    /**
     * Service parameter buffer (SPB) item associated with this property.
     *
     * @return service parameter buffer item, or {@link #NO_SPB_ITEM} if no item is associated
     * @see #hasSpbItem()
     */
    public int spbItem() {
        return spbItem;
    }

    /**
     * @return {@code true} if this property has an SPB item
     * @see #spbItem()
     */
    public boolean hasSpbItem() {
        return spbItem != NO_SPB_ITEM;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Equality (and hash code) only considers the {@code name}.
     * </p>
     * <p>
     * Use {@link #isIdenticalTo(ConnectionProperty)} for checking full equality.
     * </p>
     */
    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof ConnectionProperty that && name.equals(that.name);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The hashcode (and equals) only considers the {@code name}.
     * </p>
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Checks if the provided object is identical to this object.
     * <p>
     * This supplements {@link #equals(Object)} which only checks the {@code name}.
     * </p>
     *
     * @param other
     *         Other connection property object
     * @return {@code true} if this is the same object or all fields have the same value, {@code false} otherwise
     */
    public boolean isIdenticalTo(ConnectionProperty other) {
        if (this == other) return true;
        if (other == null) return false;
        return name.equals(other.name)
                && aliases.equals(other.aliases)
                && type == other.type
                && choices.equals(other.choices)
                && pbType == other.pbType
                && dpbItem == other.dpbItem
                && spbItem == other.spbItem;
    }

    /**
     * Normalizes values by trimming whitespace, removing {@code null}, empty or blank values.
     *
     * @param values
     *         Aliases to normalize (can be {@code null})
     * @return immutable list with zero or more values; never {@code null}
     */
    private static List<String> normalizeValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return emptyList();
        }
        List<String> normalizedValues = values.stream()
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .toList();
        return normalizedValues.isEmpty() ? emptyList() : normalizedValues;
    }

    public static final class Builder {

        private String name;
        private List<String> aliases;
        private ConnectionPropertyType type = ConnectionPropertyType.STRING;
        private List<String> choices;
        private DpbType pbType = DpbType.NONE;
        private int dpbItem = NO_DPB_ITEM;
        private int spbItem = NO_SPB_ITEM;

        public ConnectionProperty build() {
            return new ConnectionProperty(this);
        }

        /**
         * Primary name of the property; required.
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Aliases or secondary names of the property; optional.
         */
        public Builder aliases(Collection<String> aliases) {
            this.aliases = aliases != null ? new ArrayList<>(aliases) : null;
            return this;
        }

        /**
         * Aliases or secondary names of the property; optional.
         */
        public Builder aliases(String... aliases) {
            return aliases(aliases != null ? Arrays.asList(aliases) : null);
        }

        /**
         * Alias or secondary name of the property; optional.
         */
        public Builder aliases(String alias) {
            aliases = new ArrayList<>(1);
            aliases.add(alias);
            return this;
        }

        /**
         * Type of the property; required; defaults to {@link ConnectionPropertyType#STRING}.
         */
        public Builder type(ConnectionPropertyType type) {
            this.type = type;
            // NOTE: Only changing when pbType is not NONE, because that means it was explicitly forced to NONE
            if ((dpbItem != NO_DPB_ITEM || spbItem != NO_SPB_ITEM) && pbType != DpbType.NONE) {
                pbType = type.getDefaultParameterType();
            }
            return this;
        }

        /**
         * Possible values of the property (case-insensitive); optional.
         */
        public Builder choices(Collection<String> choices) {
            this.choices = choices != null ? new ArrayList<>(choices) : null;
            return this;
        }

        /**
         * Possible values of the property (case-insensitive); optional.
         */
        public Builder choices(String... choices) {
            return choices(choices != null ? Arrays.asList(choices) : null);
        }

        /**
         * Type of database (or service attach) parameter buffer; defaults to {@link #NO_DPB_ITEM}
         */
        public Builder pbType(DpbType pbType) {
            if (pbType == DpbType.NONE) {
                if (dpbItem != NO_DPB_ITEM) {
                    throw new IllegalArgumentException(
                            "Not allowed to set pbType NONE when dpbItem is set to " + dpbItem);
                }
                if (spbItem != NO_SPB_ITEM) {
                    throw new IllegalArgumentException(
                            "Not allowed to set pbType NONE when spbItem is set to " + spbItem);
                }
            } else if (dpbItem == NO_DPB_ITEM && spbItem == NO_SPB_ITEM) {
                throw new IllegalArgumentException("Usage error, set pbType after setting dpbItem or spbItem");
            }
            this.pbType = pbType;
            return this;
        }

        /**
         * DPB item associated with the property; optional; defaults to {@link #NO_DPB_ITEM}.
         */
        public Builder dpbItem(int dpbItem) {
            this.dpbItem = dpbItem;
            if (dpbItem == NO_DPB_ITEM) {
                if (spbItem == NO_SPB_ITEM) {
                    pbType = DpbType.NONE;
                }
            } else if (pbType == DpbType.NONE) {
                pbType = type.getDefaultParameterType();
            }
            return this;
        }

        /**
         * SPB item associated with the property; optional; defaults to {@link #NO_SPB_ITEM}.
         */
        public Builder spbItem(int spbItem) {
            this.spbItem = spbItem;
            if (spbItem == NO_SPB_ITEM) {
                if (dpbItem == NO_DPB_ITEM) {
                    pbType = DpbType.NONE;
                }
            } else if (pbType == DpbType.NONE) {
                pbType = type.getDefaultParameterType();
            }
            return this;
        }

    }

}
