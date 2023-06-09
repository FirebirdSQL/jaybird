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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.firebirdsql.internal.tools.Messages.unescapeSource;

/**
 * Utility class for generating the property files containing the error codes and error messages from
 * {@code include/firebird/impl/msg}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public class MessageExtractor {

    private final Path messageRoot;
    private final MessageStore messageStore;

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

    public static void main(String[] args) {
        String messageRootString = null;
        for (int idx = 0; idx < args.length; idx++) {
            if (args[idx].equals("--message-root")) {
                if (++idx < args.length) {
                    messageRootString = args[idx];
                } else {
                    System.err.println("Option --message-root misses requires path");
                }
            }
        }
        if (messageRootString == null || messageRootString.isEmpty()) {
            System.err.println("Option --message-root <path> with path the Firebird error messages files required");
            System.exit(1);
        }
        // Contrary to MessageDump, we're not implementing "--format" (to split errors per facility), as that was
        // an experiment which will likely never surface in actual Jaybird use

        MessageExtractor extractor = new MessageExtractor(Path.of(messageRootString), OutputFormat.SINGLE);
        extractor.run();
    }

    private enum MessageFormat {
        FB_IMPL_MSG("^FB_IMPL_MSG\\(([^,]+), (\\d+), ([^,]+), -?\\d+, \"([^\"]{2})\", \"([^\"]{3})\", \"(.*?)\"\\)$",
                1, 2, 3, 4, 5, 6),
        FB_IMPL_MSG_SYMBOL("^FB_IMPL_MSG_SYMBOL\\(([^,]+), (\\d+), ([^,]+), \"(.*?)\"\\)$", 1, 2, 3, -1, -1, 4),
        FB_IMPL_MSG_NO_SYMBOL("^FB_IMPL_MSG_NO_SYMBOL\\(([^,]+), (\\d+), \"(.*?)\"\\)$", 1, 2, -1, -1, -1, 3);

        private final String prefix = name() + "(";
        private final Pattern messagePattern;
        private final int facilityGroup;
        private final int numberGroup;
        private final int symbolGroup;
        private final int sqlStateClassGroup;
        private final int sqlStateSubclassGroup;
        private final int messageGroup;

        MessageFormat(String pattern, int facilityGroup, int numberGroup, int symbolGroup, int sqlStateClassGroup,
                int sqlStateSubclassGroup, int messageGroup) {
            messagePattern = Pattern.compile(pattern);
            this.facilityGroup = facilityGroup;
            this.numberGroup = numberGroup;
            this.symbolGroup = symbolGroup;
            this.sqlStateClassGroup = sqlStateClassGroup;
            this.sqlStateSubclassGroup = sqlStateSubclassGroup;
            this.messageGroup = messageGroup;
        }

        static void parseLine(String line, MessageStore messageStore) {
            for (MessageFormat messageFormat : values()) {
                if (line.startsWith(messageFormat.prefix)) {
                    messageFormat.parseLine0(line, messageStore);
                    return;
                }
            }
        }

        private void parseLine0(String line, MessageStore messageStore) {
            Matcher matcher = messagePattern.matcher(line);
            if (!matcher.matches()) return;

            Facility facility = Facility.valueOf(matcher.group(facilityGroup));
            int number = Integer.parseInt(matcher.group(numberGroup));
            String message = unescapeSource(matcher.group(messageGroup));
            messageStore.addMessage(facility, number, message);
            if (sqlStateClassGroup != -1 && sqlStateSubclassGroup != -1) {
                String sqlState = Messages.toSqlState(
                        matcher.group(sqlStateClassGroup), matcher.group(sqlStateSubclassGroup));
                messageStore.addSqlState(facility, number, sqlState);
            }
            if (symbolGroup != -1) {
                String symbolName = matcher.group(symbolGroup);
                messageStore.addSymbol(facility, number, symbolName);
            }
        }
    }
}
