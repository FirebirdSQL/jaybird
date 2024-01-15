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
package org.firebirdsql.internal.tools;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.firebirdsql.internal.tools.MessageConverter.unescapeSource;

/**
 * Utility class for generating the property files containing the error codes and error messages from
 * {@code include/firebird/impl/msg}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@SuppressWarnings("java:S106")
public class MessageExtractor {

    private final Path messageRoot;
    private final FirebirdErrorStore messageStore;

    public MessageExtractor(Path messageRoot, OutputFormat outputFormat) {
        this.messageRoot = messageRoot;
        messageStore = outputFormat.createMessageStore();
    }

    public void run() {
        System.out.printf("Processing files in: %s%n", messageRoot);
        messageStore.reset();
        PathMatcher headerMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.h");
        try (Stream<Path> pathStream = Files.list(messageRoot).filter(headerMatcher::matches)) {
            pathStream.forEach(this::parseFile);
        } catch (IOException e) {
            System.err.printf("Error listing files in %s: %s%n", messageRoot, e);
        }

        try {
            messageStore.save();
        } catch (IOException e) {
            System.err.printf("Unable to store messages and SQLSTATE: %s%n", e);
        }
    }

    private void parseFile(Path messageFile) {
        try (Stream<String> lines = Files.lines(messageFile)) {
            System.out.printf("Processing file: %s%n", messageFile.getFileName());
            // We're assuming each error is a line of its own (this seems to hold in the current code)
            lines.map(String::trim).forEach(line -> MessageFormat.parseLine(line, messageStore));
        } catch (IOException e) {
            System.err.printf("Could not read file %s: %s%n", messageFile, e);
        }
    }

    @SuppressWarnings("java:S127")
    public static void main(String[] args) {
        String messageRootString = null;
        OutputFormat outputFormat = OutputFormat.SINGLE;
        for (int idx = 0; idx < args.length; idx++) {
            if (args[idx].equals("--message-root")) {
                if (++idx < args.length) {
                    messageRootString = args[idx];
                } else {
                    System.err.println("Option --message-root misses requires path");
                }
            } else if (args[idx].equals("--format")) {
                if (++idx < args.length) {
                    try {
                        outputFormat = OutputFormat.valueOf(args[idx]);
                    } catch (IllegalArgumentException e) {
                        System.err.printf("Option --format: unexpected value '%s', expected one of %s; using default%n",
                                args[idx], Arrays.toString(OutputFormat.values()));
                    }
                } else {
                    System.err.println("Option --format expects a value; using default");
                }
            }
        }
        if (messageRootString == null || messageRootString.isEmpty()) {
            System.err.println("Option --message-root <path> with path the Firebird error messages files required");
            System.exit(1);
        }

        var extractor = new MessageExtractor(Path.of(messageRootString), outputFormat);
        extractor.run();
    }

    private enum MessageFormat {
        FB_IMPL_MSG("^FB_IMPL_MSG\\(([^,]+), (\\d+), ([^,]+), (-?\\d+), \"([^\"]{2})\", \"([^\"]{3})\", \"(.*?)\"\\)$",
                1, 2, 3, 4, 5, 6, 7),
        FB_IMPL_MSG_SYMBOL("^FB_IMPL_MSG_SYMBOL\\(([^,]+), (\\d+), ([^,]+), \"(.*?)\"\\)$", 1, 2, 3, -1, -1, -1, 4),
        FB_IMPL_MSG_NO_SYMBOL("^FB_IMPL_MSG_NO_SYMBOL\\(([^,]+), (\\d+), \"(.*?)\"\\)$", 1, 2, -1, -1, -1, -1, 3);

        private final String prefix = name() + "(";
        private final Pattern messagePattern;
        private final int facilityGroup;
        private final int numberGroup;
        private final int symbolGroup;
        private final int sqlCodeGroup;
        private final int sqlStateClassGroup;
        private final int sqlStateSubclassGroup;
        private final int messageGroup;

        MessageFormat(String pattern, int facilityGroup, int numberGroup, int symbolGroup, int sqlCodeGroup,
                int sqlStateClassGroup, int sqlStateSubclassGroup, int messageGroup) {
            messagePattern = Pattern.compile(pattern);
            this.facilityGroup = facilityGroup;
            this.numberGroup = numberGroup;
            this.symbolGroup = symbolGroup;
            this.sqlCodeGroup = sqlCodeGroup;
            this.sqlStateClassGroup = sqlStateClassGroup;
            this.sqlStateSubclassGroup = sqlStateSubclassGroup;
            this.messageGroup = messageGroup;
        }

        static void parseLine(String line, FirebirdErrorStore messageStore) {
            for (MessageFormat messageFormat : values()) {
                if (line.startsWith(messageFormat.prefix)) {
                    messageFormat.parseLine0(line, messageStore);
                    return;
                }
            }
        }

        private void parseLine0(String line, FirebirdErrorStore messageStore) {
            Matcher matcher = messagePattern.matcher(line);
            if (!matcher.matches()) return;
            messageStore.addFirebirdError(firebirdError(matcher));
        }

        private FirebirdError firebirdError(Matcher matcher) {
            return new FirebirdError(facility(matcher), number(matcher), symbol(matcher), sqlCode(matcher),
                    sqlState(matcher), message(matcher));
        }

        private Facility facility(Matcher matcher) {
            return Facility.valueOf(matcher.group(facilityGroup));
        }

        private int number(Matcher matcher) {
            return Integer.parseInt(matcher.group(numberGroup));
        }

        private String symbol(Matcher matcher) {
            if (symbolGroup == -1) return null;
            return matcher.group(symbolGroup).trim();
        }

        private Integer sqlCode(Matcher matcher) {
            if (sqlCodeGroup == -1) return null;
            return Integer.valueOf(matcher.group(sqlCodeGroup));
        }

        private String sqlState(Matcher matcher) {
            if (sqlStateClassGroup == -1 || sqlStateSubclassGroup == -1) return null;
            return MessageConverter.toSqlState(matcher.group(sqlStateClassGroup), matcher.group(sqlStateSubclassGroup));
        }

        private String message(Matcher matcher) {
            return unescapeSource(matcher.group(messageGroup));
        }
    }
}
