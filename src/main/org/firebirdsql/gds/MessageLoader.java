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

import org.firebirdsql.jaybird.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Loads the messages and SQL states for Firebird/Jaybird error codes to a {@link MessageLookup}
 *
 * @author Mark Rotteveel
 */
final class MessageLoader {

    private static final String FIREBIRD_MESSAGES = "isc_error_msg";
    private static final String JAYBIRD_MESSAGES = "org/firebirdsql/jaybird_error_msg";
    private static final String FIREBIRD_SQLSTATES = "isc_error_sqlstates";
    private static final String JAYBIRD_SQLSTATES = "org/firebirdsql/jaybird_error_sqlstates";

    private final System.Logger log = System.getLogger(MessageLoader.class.getName());
    // Implementation note, using Vector here as they can be sized and then populated by index
    private final List<List<String>> facilityMessages;
    private final List<List<String>> facilityStates;

    private MessageLoader() {
        facilityMessages = createFacilityLists();
        facilityStates = createFacilityLists();
    }

    private static List<List<String>> createFacilityLists() {
        int size = MessageLookup.FACILITY_SIZE;
        final List<List<String>> facilities = new ArrayList<>(size);
        while (size-- > 0) {
            facilities.add(new ArrayList<>());
        }
        return facilities;
    }

    /**
     * Loads the error messages and SQL states.
     *
     * @return Error lookup object for messages and SQL states
     * @throws IOException
     *         For errors reading the resource(s) with error messages and SQL states
     */
    static MessageLookup loadErrorMessages() throws IOException {
        final MessageLoader messageLoader = new MessageLoader();
        messageLoader.loadMessages(FIREBIRD_MESSAGES);
        messageLoader.loadMessages(JAYBIRD_MESSAGES);
        messageLoader.loadSqlStates(FIREBIRD_SQLSTATES);
        messageLoader.loadSqlStates(JAYBIRD_SQLSTATES);

        return messageLoader.createErrorLookup();
    }

    private MessageLookup createErrorLookup() {
        return new MessageLookup(facilityMessages, facilityStates);
    }

    private void loadMessages(String resource) throws IOException {
        loadResource(ResourceType.ERROR_MESSAGE, resource);
    }

    private void loadSqlStates(String resource) throws IOException {
        loadResource(ResourceType.SQL_STATE, resource);
    }

    private void loadResource(ResourceType resourceType, String resource) throws IOException {
        Properties properties = loadProperties(resource);
        mapToErrorCode(resourceType, properties);
    }

    private Properties loadProperties(String resource) throws IOException {
        Properties properties = new Properties();
        // Load from property files
        try (InputStream in = getResourceAsStream("/" + resource + ".properties")) {
            if (in != null) {
                properties.load(in);
            } else {
                log.log(WARNING, "Unable to load resource; resource {0} is not found", resource);
            }
        } catch (IOException ioex) {
            log.log(ERROR, "Unable to load resource " + resource, ioex);
            throw ioex;
        }
        return properties;
    }

    private void mapToErrorCode(ResourceType resourceType, Properties properties) {
        for (Object key : properties.keySet()) {
            if (!(key instanceof String)) continue;
            try {
                String keyString = (String) key;
                int errorCode = Integer.parseInt(keyString);
                String value = properties.getProperty(keyString);

                resourceType.store(errorCode, value, this);
            } catch (NumberFormatException e) {
                log.log(WARNING, "Key {0} is not a number; ignored; see debug level for stacktrace", key);
                if (log.isLoggable(DEBUG)) {
                    log.log(DEBUG, "Key " + key + " is not a number; ignored", e);
                }
            }
        }
    }

    private void storeMessage(int errorCode, String value) {
        storeValue(errorCode, value, facilityMessages.get(MessageLookup.getFacility(errorCode)));
    }

    private void storeSqlState(int errorCode, String value) {
        // Given the large number of duplicate sql states, interning makes sense here
        storeValue(errorCode, value.intern(), facilityStates.get(MessageLookup.getFacility(errorCode)));
    }

    private void storeValue(int errorCode, String value, List<String> facilityList) {
        if (facilityList == null) {
            log.log(WARNING, "Invalid error code {0}, no valid facility; skipping", errorCode);
            return;
        }
        final int code = MessageLookup.getCode(errorCode);
        if (facilityList.size() <= code) {
            CollectionUtils.growToSize(facilityList, code + 1);
        }
        facilityList.set(code, value);
    }

    private static InputStream getResourceAsStream(String res) {
        InputStream in = MessageLoader.class.getResourceAsStream(res);
        if (in == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            in = cl.getResourceAsStream(res);
        }
        return in;
    }

    private enum ResourceType {
        ERROR_MESSAGE {
            @Override
            void store(int errorCode, String value, MessageLoader messageLoader) {
                messageLoader.storeMessage(errorCode, value);
            }
        },
        SQL_STATE {
            @Override
            void store(int errorCode, String value, MessageLoader messageLoader) {
                messageLoader.storeSqlState(errorCode, value);
            }
        };

        abstract void store(int errorCode, String value, MessageLoader messageLoader);
    }

}
