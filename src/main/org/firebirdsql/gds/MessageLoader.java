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

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.WARNING;
import static org.firebirdsql.jaybird.util.StringUtils.trimToNull;

/**
 * Loads the messages and SQL states for Firebird/Jaybird error codes to a {@link MessageLookup}
 *
 * @author Mark Rotteveel
 * @since 4
 */
@NullMarked
final class MessageLoader {

    private final MessageDefinition messageDefinition;
    private final Properties messages;
    private final Properties sqlStates;

    /**
     * Creates a message loader for a message definition.
     *
     * @param messageDefinition
     *         message definition
     * @since 6
     */
    private MessageLoader(MessageDefinition messageDefinition) {
        this.messageDefinition = messageDefinition;
        messages = messageDefinition.loadMessages();
        sqlStates = messageDefinition.loadSqlStates();
    }

    /**
     * Loads the message templates for the specified facility.
     *
     * @param facility
     *         facility
     * @return stream of message templates
     * @since 6
     */
    static Stream<MessageTemplate> loadMessageTemplates(int facility) {
        try {
            return new MessageLoader(MessageDefinition.of(facility)).createMessageTemplates();
        } catch (IllegalArgumentException e) {
            System.getLogger(MessageLoader.class.getName()).log(DEBUG,
                    "Failed to load message templates for facility: " + facility, e);
            return Stream.empty();
        }
    }

    /**
     * Creates the message templates.
     *
     * @return stream of message templates
     */
    Stream<MessageTemplate> createMessageTemplates() {
        return messages.stringPropertyNames().stream()
                .map(this::createMessageTemplate)
                .filter(Objects::nonNull);
    }

    /**
     * Creates a message template for {@code errorCodeString}.
     *
     * @param errorCodeString
     *         error code as string
     * @return message template, or {@code null} if {@code errorString} cannot be parsed to an integer, or if the
     * message resource has no text for the error code
     */
    @Nullable MessageTemplate createMessageTemplate(String errorCodeString) {
        try {
            int errorCode = Integer.parseInt(errorCodeString);
            String templateText = trimToNull(messages.getProperty(errorCodeString));
            if (templateText == null) {
                System.getLogger(MessageLoader.class.getName()).log(DEBUG,
                        "No template text for error code {0} in resource {1}", errorCodeString, messageDefinition);
                return null;
            }
            String sqlState = trimToNull(sqlStates.getProperty(errorCodeString));
            if (sqlState != null) {
                // Given the large number of duplicate SQL states, interning makes sense here
                sqlState = sqlState.intern();
            }
            try {
                return new DefaultMessageTemplate(errorCode, templateText, sqlState);
            } catch (IllegalArgumentException e) {
                System.getLogger(MessageLoader.class.getName()).log(DEBUG,
                        "Error code {0} has invalid SQL state value ''{1}'' in resource {2}",
                        errorCodeString, sqlState, messageDefinition);
                // Graceful degradation: create without SQL state
                return new DefaultMessageTemplate(errorCode, templateText);
            }
        } catch (NumberFormatException e) {
            System.getLogger(MessageLoader.class.getName()).log(DEBUG,
                    "Non-integer error code value ''{0}'' in resource {1}", errorCodeString, messageDefinition);
            return null;
        }
    }

    /**
     * Definition of the message resource and accompanying SQL state resource.
     *
     * @param messageResourceName
     *         name of the message resource (without {@code /} prefix and {@code .properties} suffix)
     * @param sqlStateResourceName
     *         name of the SQL state resource (without {@code /} prefix and {@code .properties} suffix)
     * @since 6
     */
    private record MessageDefinition(String messageResourceName, String sqlStateResourceName) {

        private static final String FIREBIRD_MESSAGE_FORMAT = "org/firebirdsql/firebird_%d_error_msg";
        private static final String FIREBIRD_SQL_STATES_FORMAT = "org/firebirdsql/firebird_%d_sql_states";
        private static final String JAYBIRD_MESSAGES = "org/firebirdsql/jaybird_error_msg";
        private static final String JAYBIRD_SQLSTATES = "org/firebirdsql/jaybird_error_sqlstates";

        /**
         * Loads the message resource into a properties object.
         *
         * @return properties object with messages, empty if the resource was not found or could not be read
         */
        private Properties loadMessages() {
            return loadProperties(messageResourceName);
        }

        /**
         * Loads the SQL state resource into a properties object.
         *
         * @return properties object with SQL states, empty if the resource was not found or could not be read
         */
        private Properties loadSqlStates() {
            return loadProperties(sqlStateResourceName);
        }

        /**
         * Loads the resource into a properties object.
         *
         * @param resource
         *         name of the resource (without {@code /} prefix and {@code .properties} suffix)
         * @return properties object, empty if the resource was not found or could not be read
         */
        private static Properties loadProperties(String resource) {
            // Load from property files
            var properties = new Properties();
            try (InputStream in = getResourceAsStream("/" + resource + ".properties")) {
                if (in != null) {
                    properties.load(in);
                } else {
                    System.getLogger(MessageLoader.class.getName())
                            .log(WARNING, "Unable to load resource; resource {0} is not found", resource);
                }
            } catch (IOException e) {
                System.getLogger(MessageLoader.class.getName()).log(ERROR, "Unable to load resource " + resource, e);
                // Graceful degradation; Jaybird can load, but missing errors will produce the "not found" message, or
                // not report the right SQLstate.
            }
            return properties;
        }

        /**
         * Opens the resource as a stream using the class loader of this class, or otherwise the context class loader.
         *
         * @param res
         *         resource name (contrary to {@link #loadProperties(String)}, it must include the file extension)
         * @return input stream, or {@code null} if the resource was not found
         */
        private static @Nullable InputStream getResourceAsStream(String res) {
            InputStream in = MessageLoader.class.getResourceAsStream(res);
            if (in == null) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                in = cl.getResourceAsStream(res);
            }
            return in;
        }

        /**
         * Creates a message definition for the specified facility.
         *
         * @param facility
         *         facility [0 ... {@link MessageLookup#FACILITY_SIZE}&gt;
         * @return message definition
         * @throws IllegalArgumentException
         *         if {@code facility} is out of range
         */
        private static MessageDefinition of(int facility) {
            if (facility < 0 || facility >= MessageLookup.FACILITY_SIZE) {
                throw new IllegalArgumentException("Unsupported facility: " + facility);
            } else if (facility == MessageLookup.JAYBIRD_FACILITY) {
                return new MessageDefinition(JAYBIRD_MESSAGES, JAYBIRD_SQLSTATES);
            }
            return new MessageDefinition(FIREBIRD_MESSAGE_FORMAT.formatted(facility),
                    FIREBIRD_SQL_STATES_FORMAT.formatted(facility));
        }

    }

}
