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

import org.firebirdsql.jaybird.props.internal.ConnectionPropertyDefinition;
import org.firebirdsql.util.InternalApi;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;
import static org.firebirdsql.util.StringUtils.trimToNull;

/**
 * A Jaybird connection property.
 * <p>
 * Holds information like name, aliases, default value etc for a Jaybird connection property.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public final class ConnectionProperty {

    private static final Set<ConnectionPropertyApplicability> DEFAULT_APPLICABILITY =
            unmodifiableSet(EnumSet.allOf(ConnectionPropertyApplicability.class));
    private static final Set<ConnectionPropertyApplicability> DATABASE_ONLY_APPLICABILITY =
            unmodifiableSet(EnumSet.of(ConnectionPropertyApplicability.DATABASE));
    private static final Set<ConnectionPropertyApplicability> SERVICE_ONLY_APPLICABILITY =
            unmodifiableSet(EnumSet.of(ConnectionPropertyApplicability.SERVICE));

    public static final int NO_DPB_ITEM = -1;
    public static final int NO_SPB_ITEM = -1;

    private final String name;
    private final List<String> aliases;
    private final ConnectionPropertyType type;
    private final List<String> choices;
    private final Object defaultValue;
    private final Set<ConnectionPropertyApplicability> applicability;
    private final String description;
    private final int dpbItem;
    private final int spbItem;

    /**
     * Creates a connection property instance.
     *
     * @param name
     *         Primary name; cannot be {@code null)
     * @param aliases
     *         List of aliases (secondary names); can be {@code null}
     * @param type
     *         Datatype of values of this property; cannot be {@code null}. For DPB/SPB items the type must match the
     *         type expected by Firebird
     * @param choices
     *         list of possible values for this property (case-insensitive); can be {@code null}. When non-empty the
     *         implementation may reject other non-{@code null} values
     * @param defaultValue
     *         default value (must be convertible to {@code type}); {@code null} means no default
     * @param applicability
     *         applicability of this property; {@code null} implies {@code DATABASE} and {@code SERVICE}, while empty
     *         implies no applicability
     * @param description
     *         Description of the property (can be displayed to users of Jaybird)
     * @param dpbItem
     *         The DPB item for this property; set to {@link #NO_DPB_ITEM} if not associated with a DPB item
     * @param spbItem
     *         The SPB item for this property; set to {@link #NO_SPB_ITEM} if not associated with a SPB item
     */
    private ConnectionProperty(String name, List<String> aliases, ConnectionPropertyType type,
            List<String> choices, Object defaultValue, List<ConnectionPropertyApplicability> applicability,
            String description, int dpbItem, int spbItem) {
        this.name = requireNonNull(trimToNull(name), "name");
        this.aliases = aliases == null || aliases.isEmpty() ? emptyList() : unmodifiableList(new ArrayList<>(aliases));
        this.type = requireNonNull(type, "type");
        this.choices = choices == null || choices.isEmpty() ? emptyList() : unmodifiableList(new ArrayList<>(choices));
        this.defaultValue = defaultValue;
        this.applicability = toConnectionPropertyApplicabilitySet(applicability);
        this.description = description == null || description.isEmpty() ? null : description;
        this.dpbItem = dpbItem;
        this.spbItem = spbItem;
    }

    /**
     * Creates a connection property instance from an {@link ConnectionPropertyDefinition} annotation.
     * <p>
     * This method is for internal use in Jaybird only. TODO: Move elsewhere?
     * </p>
     *
     * @param propDef
     *         property definition annotation
     * @return Connection property instance
     */
    @InternalApi
    public static ConnectionProperty fromAnnotation(ConnectionPropertyDefinition propDef) {
        ConnectionPropertyType type = propDef.type();
        String defaultValueFromAnnotation = propDef.defaultValue();
        String defaultValue = !"".equals(defaultValueFromAnnotation) ? defaultValueFromAnnotation : null;
        return builder()
                .name(propDef.name())
                .aliases(asList(propDef.aliases()))
                .type(type)
                .choices(asList(propDef.choices()))
                .defaultValue(defaultValue)
                .applicability(asList(propDef.applicability()))
                .description(propDef.description())
                .dpbItem(propDef.dpbItem())
                .spbItem(propDef.spbItem())
                .build();
    }

    /**
     * Creates a connection property builder.
     *
     * @return builder
     */
    public static ConnectionPropertyBuilder builder() {
        return new ConnectionPropertyBuilder();
    }

    /**
     * Creates an <em>unknown</em> connection property.
     * <p>
     * An <em>unknown</em> connection property was either not defined but used, or can be used for lookups (given the
     * definition of equals and hashcode). An unknown connection property always applies type
     * {@link ConnectionPropertyType#STRING} and applies to database and service.
     * </p>
     *
     * @param name
     *         Property name
     * @return And unknown property with the specified name
     */
    public static ConnectionProperty unknown(String name) {
        return builder()
                .name(name)
                .type(ConnectionPropertyType.STRING)
                .build();
    }

    /**
     * @return name of the property
     */
    public String getName() {
        return name;
    }

    /**
     * Optional aliases for this property.
     * <p>
     * Aliases are alternative names for the property, either for backwards compatibility or for ease of use.
     * </p>
     *
     * @return aliases for this property, empty means no aliases
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Type of the property.
     *
     * @return type of the property
     */
    public ConnectionPropertyType getType() {
        return type;
    }

    /**
     * Possible values for this property.
     * <p>
     * This is for documentation purposes only (eg for {@link java.sql.DriverPropertyInfo}).
     * </p>
     *
     * @return possible values for this property, or empty when unrestricted
     */
    public List<String> getChoices() {
        return choices;
    }

    /**
     * Default value of this property.
     *
     * @return default value, null means no default
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Applicability of this property.
     * <p>
     * NOTE: This is considered informational; setting {@code SERVICE}-only property on a database connection is
     * allowed (it will just - likely - not do anything).
     * </p>
     *
     * @return applicability of this property
     */
    public Set<ConnectionPropertyApplicability> getApplicability() {
        return applicability;
    }

    /**
     * @return {@code true} if this is a database connection property
     */
    public boolean isDatabaseConnectionProperty() {
        return applicability.contains(ConnectionPropertyApplicability.DATABASE);
    }

    /**
     * @return {@code true} if this is a service connection property
     */
    public boolean isServiceConnectionProperty() {
        return applicability.contains(ConnectionPropertyApplicability.SERVICE);
    }

    /**
     * Description of this property.
     *
     * @return description of this property, can be {@code null}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Database parameter buffer (DPB) item associated with this property.
     *
     * @return database parameter buffer item, or {@link #NO_DPB_ITEM} if no item is associated
     */
    public int getDpbItem() {
        return dpbItem;
    }

    /**
     * Service parameter buffer (SPB) item associated with this property.
     *
     * @return service parameter buffer item, or {@link #NO_SPB_ITEM} if no item is associated
     */
    public int getSpbItem() {
        return spbItem;
    }

    @Override
    public String toString() {
        return "ConnectionProperty{" +
                "name='" + name + '\'' +
                ", aliases=" + aliases +
                ", type=" + type +
                ", choices=" + choices +
                ", defaultValue=" + defaultValue +
                ", applicability=" + applicability +
                ", description='" + description + '\'' +
                ", dpbItem=" + dpbItem +
                ", spbItem=" + spbItem +
                '}';
    }

    /**
     * {@inheritDoc}
     * <p>
     * Equality (and hash code) only considers the {@code name}.
     * </p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConnectionProperty)) return false;

        ConnectionProperty that = (ConnectionProperty) o;

        return name.equals(that.name);
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
                && Objects.equals(defaultValue, other.defaultValue)
                && Objects.equals(description, other.description)
                && dpbItem == other.dpbItem
                && spbItem == other.spbItem;
    }

    private static Set<ConnectionPropertyApplicability> toConnectionPropertyApplicabilitySet(
            List<ConnectionPropertyApplicability> applicability) {
        if (applicability == null) {
            return DEFAULT_APPLICABILITY;
        }
        boolean hasDatabase = false;
        boolean hasService = false;
        for (ConnectionPropertyApplicability currentApplicability : applicability) {
            if (currentApplicability == ConnectionPropertyApplicability.DATABASE) {
                hasDatabase = true;
            } else if (currentApplicability == ConnectionPropertyApplicability.SERVICE) {
                hasService = true;
            }
        }
        if (hasDatabase) {
            if (hasService) {
                return DEFAULT_APPLICABILITY;
            }
            return DATABASE_ONLY_APPLICABILITY;
        } else if (hasService) {
            return SERVICE_ONLY_APPLICABILITY;
        }
        return emptySet();
    }

    public static final class ConnectionPropertyBuilder {

        private String name;
        private List<String> aliases;
        private ConnectionPropertyType type;
        private List<String> choices;
        private String defaultValue;
        private List<ConnectionPropertyApplicability> applicability;
        private String description;
        private int dpbItem = NO_DPB_ITEM;
        private int spbItem = NO_SPB_ITEM;

        public ConnectionProperty build() {
            return new ConnectionProperty(name, aliases, requireNonNull(type, "type"), choices,
                    type.toType(defaultValue), applicability, description, dpbItem, spbItem);
        }

        /**
         * Primary name of the property; required
         */
        public ConnectionPropertyBuilder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Aliases or secondary names of the property; optional
         */
        public ConnectionPropertyBuilder aliases(Collection<String> aliases) {
            this.aliases = aliases != null ? new ArrayList<>(aliases) : null;
            return this;
        }

        public ConnectionPropertyBuilder addAlias(String alias) {
            addAlias0(alias);
            return this;
        }

        private void addAlias0(String alias) {
            if (aliases == null) {
                aliases = new ArrayList<>();
            }
            aliases.add(alias);
        }

        public ConnectionPropertyBuilder addAliases(String... aliases) {
            if (aliases != null) {
                for (String alias : aliases) {
                    addAlias0(alias);
                }
            }
            return this;
        }

        /**
         * Type of the property; required
         */
        public ConnectionPropertyBuilder type(ConnectionPropertyType type) {
            this.type = type;
            return this;
        }

        /**
         * Possible values of the property (case insensitive); optional
         */
        public ConnectionPropertyBuilder choices(Collection<String> choices) {
            this.choices = choices != null ? new ArrayList<>(choices) : null;
            return this;
        }

        public ConnectionPropertyBuilder addChoice(String choice) {
            addChoice0(choice);
            return this;
        }

        private void addChoice0(String choice) {
            if (choices == null) {
                choices = new ArrayList<>();
            }
            choices.add(choice);
        }

        public ConnectionPropertyBuilder addChoices(String... choices) {
            if (choices != null) {
                for (String choice : choices) {
                    addChoice0(choice);
                }
            }
            return this;
        }

        /**
         * Default value of the property; must be convertible to {@code type} on build. {@code null} means no default.
         */
        public ConnectionPropertyBuilder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Applicability of the property. {@code null} applies default ({@code DATABASE} and {@code SERVICE}), while
         * empty implies no applicability.
         */
        public ConnectionPropertyBuilder applicability(Collection<ConnectionPropertyApplicability> applicability) {
            this.applicability = applicability != null ? new ArrayList<>(applicability) : null;
            return this;
        }

        public ConnectionPropertyBuilder addApplicability(ConnectionPropertyApplicability applicability) {
            addApplicability0(applicability);
            return this;
        }

        private void addApplicability0(ConnectionPropertyApplicability applicability) {
            if (this.applicability == null) {
                this.applicability = new ArrayList<>();
            }
            this.applicability.add(applicability);
        }

        public ConnectionPropertyBuilder addApplicability(ConnectionPropertyApplicability... applicability) {
            if (applicability != null) {
                for (ConnectionPropertyApplicability currentApplicability : applicability) {
                    addApplicability0(currentApplicability);
                }
            }
            return this;
        }

        /**
         * Description (for users of Jaybird) of the property; optional but recommended
         */
        public ConnectionPropertyBuilder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * DPB item associated with the property; don't set or use {@link #NO_DPB_ITEM} if no DPB item.
         */
        public ConnectionPropertyBuilder dpbItem(int dpbItem) {
            this.dpbItem = dpbItem;
            return this;
        }

        /**
         * SPB item associated with the property; don't set or use {@link #NO_SPB_ITEM} if no SPB item.
         */
        public ConnectionPropertyBuilder spbItem(int spbItem) {
            this.spbItem = spbItem;
            return this;
        }

    }

}
