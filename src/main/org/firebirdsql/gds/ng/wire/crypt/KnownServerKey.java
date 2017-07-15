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
package org.firebirdsql.gds.ng.wire.crypt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Class to hold server keys known to the client.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class KnownServerKey {

    private static final Pattern CRYPT_PLUGIN_LIST_SPLIT = Pattern.compile("[ \t,;]+");

    private final String keyType;
    private final List<String> plugins;

    public KnownServerKey(String keyType, List<String> plugins) {
        this.keyType = requireNonNull(keyType, "keyType");
        this.plugins = Collections.unmodifiableList(new ArrayList<>(requireNonNull(plugins, "plugins")));
    }

    public KnownServerKey(String keyType, String plugins) {
        this(keyType, Arrays.asList(CRYPT_PLUGIN_LIST_SPLIT.split(plugins)));
    }

    public List<EncryptionIdentifier> getIdentifiers() {
        List<EncryptionIdentifier> identifiers = new ArrayList<>(plugins.size());
        for (String plugin : plugins) {
            identifiers.add(new EncryptionIdentifier(keyType, plugin));
        }
        return identifiers;
    }
}
