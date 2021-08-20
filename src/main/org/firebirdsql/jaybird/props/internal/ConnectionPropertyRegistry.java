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
package org.firebirdsql.jaybird.props.internal;

import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.spi.ConnectionPropertyDefinerSpi;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.InternalApi;

import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * Registry of connection properties available to Jaybird.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
@InternalApi
public final class ConnectionPropertyRegistry {

    /**
     * Initialize on demand holder
     */
    private static final class Holder {
        private static final ConnectionPropertyRegistry INSTANCE = loadProperties();
    }

    private final Map<String, ConnectionProperty> connectionPropertiesMap;

    // default access for test purposes
    ConnectionPropertyRegistry(Map<String, ConnectionProperty> connectionPropertiesMap) {
        this.connectionPropertiesMap = unmodifiableMap(new HashMap<>(connectionPropertiesMap));
    }

    /**
     * Get an existing connection property by its name or alias.
     *
     * @param name
     *         Name or alias of the property
     * @return The property, or {@code null} if there is no property with this name.
     */
    public ConnectionProperty getByName(String name) {
        return connectionPropertiesMap.get(name);
    }

    /**
     * Get a connection property by its name or alias, returning an unknown property if it doesn't exist.
     *
     * @param name
     *         Name or alias of the property
     * @return The property (either registered or unknown)
     */
    public ConnectionProperty getOrUnknown(String name) {
        ConnectionProperty property = getByName(name);
        return property != null ? property : ConnectionProperty.unknown(name);
    }

    /**
     * @return collection of all registered properties in arbitrary order
     */
    // default access for test purposes
    Collection<ConnectionProperty> getRegisteredProperties() {
        return new HashSet<>(connectionPropertiesMap.values());
    }

    /**
     * @return collection of all registered names and aliases in arbitrary order
     */
    // default access for test purposes
    Collection<String> getRegisteredNames() {
        return new HashSet<>(connectionPropertiesMap.keySet());
    }

    /**
     * @return the standard instance of {@code ConnectionProperties}; for normal usages this is a singleton
     */
    public static ConnectionPropertyRegistry getInstance() {
        return Holder.INSTANCE;
    }

    private static ConnectionPropertyRegistry loadProperties() {
        ConnectionPropertiesBuilder builder = new ConnectionPropertiesBuilder();
        getDefiners().forEach(definer -> definer.defineProperties()
                .forEach(property -> builder.tryRegisterProperty(property, definer)));
        UnregisteredDpbDefiner unregisteredDpbDefiner =
                new UnregisteredDpbDefiner(unmodifiableSet(builder.connectionPropertiesMap.keySet()));
        unregisteredDpbDefiner.defineProperties()
                .forEach(property -> builder.tryRegisterProperty(property, unregisteredDpbDefiner));
        return builder.build();
    }

    private static Stream<ConnectionPropertyDefinerSpi> getDefiners() {
        return Stream.concat(getStandardDefiners(), getCustomDefiners());
    }

    private static Stream<ConnectionPropertyDefinerSpi> getStandardDefiners() {
        return Stream.of(new StandardConnectionPropertyDefiner());
    }

    private static Stream<ConnectionPropertyDefinerSpi> getCustomDefiners() {
        try {
            ServiceLoader<ConnectionPropertyDefinerSpi> serviceLoader = ServiceLoader.load(
                    ConnectionPropertyDefinerSpi.class, ConnectionPropertyRegistry.class.getClassLoader());
            List<ConnectionPropertyDefinerSpi> customDefiners = new ArrayList<>();
            Iterator<ConnectionPropertyDefinerSpi> iterator = serviceLoader.iterator();
            while (iterator.hasNext()) {
                try {
                    customDefiners.add(iterator.next());
                } catch (RuntimeException | ServiceConfigurationError e) {
                    LoggerFactory.getLogger(ConnectionPropertyRegistry.class)
                            .warn("Could not load a custom ConnectionPropertyDefinerSpi", e);
                }
            }
            return customDefiners.stream();
        } catch (RuntimeException | ServiceConfigurationError e) {
            LoggerFactory.getLogger(ConnectionPropertyRegistry.class)
                    .warn("Could not load any custom ConnectionPropertyDefinerSpi", e);
            return Stream.empty();
        }
    }

    // default access for test purposes
    static final class ConnectionPropertiesBuilder {

        private final Logger log = LoggerFactory.getLogger(ConnectionPropertiesBuilder.class);

        private final Map<String, ConnectionProperty> connectionPropertiesMap = new HashMap<>();
        private final Set<Integer> dpbItems = new HashSet<>();
        private final Set<Integer> spbItems = new HashSet<>();

        void tryRegisterProperty(ConnectionProperty property, ConnectionPropertyDefinerSpi definer) {
            if (shouldRegisterProperty(property, definer)) {
                Stream.concat(Stream.of(property.name()), property.aliases().stream())
                        .forEach(propertyNameOrAlias -> connectionPropertiesMap.put(propertyNameOrAlias, property));
                dpbItems.add(property.dpbItem());
                spbItems.add(property.spbItem());
            }
        }

        ConnectionPropertyRegistry build() {
            return new ConnectionPropertyRegistry(connectionPropertiesMap);
        }

        /**
         * Checks if the connection property should be registered, and otherwise notifies the definer.
         * <p>
         * The property will not be added if an identical property is already registered, or the name or one of its
         * aliases, or the DPB or SPB item was already claimed by another property.
         * </p>
         * <p>
         * If an identical property is already registered, it is silently ignored; if a property should not be
         * registered for a different reason, its
         * {@link ConnectionPropertyDefinerSpi#notRegistered(ConnectionProperty)} will be called.
         * </p>
         *
         * @param property
         *         Connection property to add
         * @param definer
         *         Definer of the property
         * @return {@code true} if the property should be registered, {@code false} if not
         */
        private boolean shouldRegisterProperty(ConnectionProperty property, ConnectionPropertyDefinerSpi definer) {
            ConnectionProperty existingProperty = connectionPropertiesMap.get(property.name());
            if (existingProperty != null && property.isIdenticalTo(existingProperty)) {
                // equivalent property already registered; ignore
                return false;
            } else if (existingProperty != null
                    || property.aliases().stream().anyMatch(connectionPropertiesMap::containsKey)) {
                handleDuplicateNameOrAlias(property, definer);
                return false;
            } else if (property.hasDpbItem() && dpbItems.contains(property.dpbItem())) {
                handleDuplicateDpbItem(property, definer);
                return false;
            } else if (property.hasSpbItem() && spbItems.contains(property.spbItem())) {
                handleDuplicateSpbItem(property, definer);
                return false;
            }
            return true;
        }

        private void handleDuplicateNameOrAlias(ConnectionProperty property, ConnectionPropertyDefinerSpi definer) {
            assert (!(definer instanceof StandardConnectionPropertyDefiner))
                    : "standard properties should not have duplicate aliases";
            HashSet<String> duplicateAliases = new HashSet<>(property.aliases());
            duplicateAliases.add(property.name());
            duplicateAliases.retainAll(connectionPropertiesMap.keySet());
            log.warn(format("Failed to register connection property, one or more of its aliases were already "
                    + "defined; duplicate alias(es): %s, connection property: %s", duplicateAliases, property));
            notifyNotRegistered(definer, property);
        }

        private void handleDuplicateDpbItem(ConnectionProperty property, ConnectionPropertyDefinerSpi definer) {
            handleDuplicateParameterBufferItem(property, definer, "DPB");
        }

        private void handleDuplicateSpbItem(ConnectionProperty property, ConnectionPropertyDefinerSpi definer) {
            handleDuplicateParameterBufferItem(property, definer, "SPB");
        }

        private void handleDuplicateParameterBufferItem(ConnectionProperty property,
                ConnectionPropertyDefinerSpi definer, String type) {
            assert (!(definer instanceof StandardConnectionPropertyDefiner))
                    : "standard properties should not have duplicate " + type + " items: " + property.name();
            log.warn(format("Failed to register connection property, its %s item was already defined; "
                    + "connection property: %s", type, property));
            notifyNotRegistered(definer, property);
        }

        private void notifyNotRegistered(ConnectionPropertyDefinerSpi definer, ConnectionProperty connectionProperty) {
            try {
                definer.notRegistered(connectionProperty);
            } catch (Exception e) {
                // Intentionally not catching Throwable here to allow errors to escape
                log.warn("Exception received notifying definer.notRegistered(" + connectionProperty + ") at "
                        + definer, e);
            }
        }
    }

}
