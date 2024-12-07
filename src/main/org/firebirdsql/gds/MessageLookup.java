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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Lookup table for error messages and sql states by error code.
 * <p>
 * See also Firebird {@code src\common\msg_encode.h}.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 4
 */
@NullMarked
final class MessageLookup {

    // See definitions in Firebird's msg_encode.h
    private static final int ISC_MASK = 0x14000000; // Defines the code as a valid ISC code
    private static final int FAC_MASK = 0x00FF0000; // Specifies the facility where the code is located
    private static final int CODE_MASK = 0x0000FFFF; // Specifies the code in the message file
    static final int JAYBIRD_FACILITY = 26;
    // This constant will need to be updated if new facilities are added to Firebird
    private static final int MAX_FACILITY = JAYBIRD_FACILITY; // Jaybird = 26
    static final int FACILITY_SIZE = MessageLookup.MAX_FACILITY + 1;
    /**
     * Marker object for unloaded facilities; also returned for "out-of-range" facility codes.
     */
    private static final MessageTemplate[] FACILITY_NOT_LOADED = new MessageTemplate[0];

    // Lookup from facility + code to message template
    private final @Nullable MessageTemplate[][] messageTemplates = new @Nullable MessageTemplate[FACILITY_SIZE][];
    // Only governs reads/writes to the first level of messageTemplates
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    MessageLookup() {
        Arrays.fill(messageTemplates, FACILITY_NOT_LOADED);
    }

    /**
     * Retrieves the (error) message template for the specified error code.
     *
     * @param errorCode
     *         error code
     * @return error message, or {@code null} if not found
     * @since 6
     */
    MessageTemplate getMessageTemplate(int errorCode) {
        if (isInvalidErrorCode(errorCode)) {
            return DefaultMessageTemplate.notFound(errorCode);
        }
        try {
            final @Nullable MessageTemplate[] facilityTemplates = getFacilityTemplates(getFacility(errorCode));
            final int code = getCode(errorCode);
            MessageTemplate template = facilityTemplates[code];
            if (template == null) {
                // Store not found message for reuse (if asked for it once, we'll likely ask for it again)
                // NOTE: we're not using a write lock for this; we accept barging and/or visibility issues
                template = facilityTemplates[code] = DefaultMessageTemplate.notFound(errorCode);
            }
            return template;
        } catch (ArrayIndexOutOfBoundsException e) {
            return DefaultMessageTemplate.notFound(errorCode);
        }
    }

    private @Nullable MessageTemplate[] getFacilityTemplates(int facility) {
        if (facility < 0 || facility > MAX_FACILITY) return FACILITY_NOT_LOADED;
        readLock.lock();
        try {
            @Nullable MessageTemplate[] facilityTemplates = messageTemplates[facility];
            if (facilityTemplates != FACILITY_NOT_LOADED) {
                return facilityTemplates;
            }
        } finally {
            readLock.unlock();
        }
        return loadFacilityTemplates(facility);
    }

    private @Nullable MessageTemplate[] loadFacilityTemplates(int facility) {
        writeLock.lock();
        try {
            @Nullable MessageTemplate[] facilityTemplates = messageTemplates[facility];
            if (facilityTemplates != FACILITY_NOT_LOADED) {
                return facilityTemplates;
            }
            var indexedByCode = new ArrayList<@Nullable MessageTemplate>();
            MessageLoader.loadMessageTemplates(facility)
                    .forEach(template -> {
                        int errorCode = template.errorCode();
                        if (isInvalidErrorCode(errorCode) || getFacility(errorCode) != facility) {
                            System.getLogger(MessageLookup.class.getName())
                                    .log(DEBUG, "Invalid error code or error code with out-of-range facility: {}", errorCode);
                            return;
                        }
                        int code = getCode(errorCode);
                        CollectionUtils.growToSize(indexedByCode, code + 1);
                        if (indexedByCode.set(code, template) != null) {
                            System.getLogger(MessageLookup.class.getName())
                                    .log(DEBUG, "Duplicate error code: {}", errorCode);
                        }
                    });
            return messageTemplates[facility] = indexedByCode.toArray(@Nullable MessageTemplate[]::new);
        } finally {
            writeLock.unlock();
        }
    }

    private static boolean isInvalidErrorCode(final int errorCode) {
        return (errorCode & ISC_MASK) != ISC_MASK;
    }

    /**
     * Obtain the facility from an error code.
     *
     * @param errorCode
     *         error code
     * @return Facility
     */
    static int getFacility(int errorCode) {
        return (errorCode & FAC_MASK) >> 16;
    }

    /**
     * Obtain the code within a facility from an error code.
     *
     * @param errorCode
     *         error code
     * @return Facility
     */
    static int getCode(int errorCode) {
        return (errorCode & CODE_MASK);
    }

}
