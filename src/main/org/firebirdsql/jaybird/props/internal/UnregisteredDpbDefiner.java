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

import org.firebirdsql.jaybird.fb.constants.DpbItems;
import org.firebirdsql.jaybird.fb.constants.SpbItems;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.spi.ConnectionPropertyDefinerSpi;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.firebirdsql.jaybird.props.def.ConnectionProperty.builder;

/**
 * Class to define unregistered DPB items (by name) as connection properties.
 * <p>
 * This definer is run last, so if other properties have been defined with the same short or long name before,
 * the property will not get defined. These properties will be registered as type string.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
class UnregisteredDpbDefiner implements ConnectionPropertyDefinerSpi {

    private final System.Logger log = System.getLogger(UnregisteredDpbDefiner.class.getName());

    private final Collection<String> knownPropertyNames;
    private final Set<String> excludedShortNames;

    UnregisteredDpbDefiner(Collection<String> knownPropertyNames) {
        this.knownPropertyNames = knownPropertyNames;
        // short names of properties we do not want to register (if they don't exist yet), reasons are that
        // Jaybird uses these internally, conflicting definitions (same id for two different names), or using
        // the option would probably require additional support in Jaybird
        excludedShortNames = new HashSet<>(Arrays.asList("trusted_auth", "specific_auth_data", "auth_block",
                "auth_plugin_list", "auth_plugin_name", "password_enc", "utf8_filename", "client_version"));
    }

    @Override
    public Stream<ConnectionProperty> defineProperties() {
        Map<String, Integer> dpbItems = findItems(DpbItems.class, "isc_dpb_");
        Map<String, Integer> spbItems = findItems(SpbItems.class, "isc_spb_");

        // We only consider DPB items for addition, but will add matching SPB items
        return dpbItems.keySet().stream()
                .map(shortName -> {
                    log.log(System.Logger.Level.DEBUG, "Defining unregistered DPB/SPB property {0}", shortName);
                    ConnectionProperty.Builder builder = builder(shortName);
                    if (dpbItems.containsKey(shortName)) {
                        builder.aliases(("isc_dpb_" + shortName).intern());
                        builder.dpbItem(dpbItems.get(shortName));
                    }
                    if (spbItems.containsKey(shortName)) {
                        builder.spbItem(spbItems.get(shortName));
                    }
                    return builder.build();
                });
    }

    private Map<String, Integer> findItems(Class<?> clazz, String prefix) {
        final int prefixLength = prefix.length();
        Map<String, Integer> items = new HashMap<>();
        for (Field field : clazz.getFields()) {
            String name = field.getName();
            if (knownPropertyNames.contains(name) || field.getType() != int.class || !name.startsWith(prefix)) {
                continue;
            }

            try {
                int value = field.getInt(null);
                String shortName = name.substring(prefixLength);
                if (!knownPropertyNames.contains(shortName) && !excludedShortNames.contains(shortName)) {
                    items.put(shortName, value);
                }
            } catch (IllegalAccessException ignored) {
                // ignore field
            }
        }
        return items;
    }

    @Override
    public void notRegistered(ConnectionProperty connectionProperty) {
        log.log(System.Logger.Level.DEBUG, "Property not registered: {0}", connectionProperty);
    }
}
