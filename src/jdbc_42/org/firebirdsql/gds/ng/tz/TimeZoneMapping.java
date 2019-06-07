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
package org.firebirdsql.gds.ng.tz;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.StringUtils;

import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mapping of Firebird time zone ids.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
public final class TimeZoneMapping {

    private final Logger logger = LoggerFactory.getLogger(TimeZoneMapping.class);

    private static final TimeZoneMapping INSTANCE = new TimeZoneMapping();
    private static final int MAX_ZONE_ID = 65535;
    private static final int MAX_OFFSET = 1439; // 23h:59m;
    private static final int MIN_OFFSET = -MAX_OFFSET;
    private static final int MAX_OFFSET_SUPPORTED = 1080; // 18h:00m; derived from ZoneOffset limitations
    private static final int MIN_OFFSET_SUPPORTED = -MAX_OFFSET_SUPPORTED;
    private static final int MAX_OFFSET_ENCODED = 2878;
    private static final int OFFSET_CORRECTION = 1439;
    private static final int OFFSET_UTC = 0;
    private static final int OFFSET_UTC_ENCODED = OFFSET_UTC + OFFSET_CORRECTION;
    private static final int MAX_OFFSET_SUPPORTED_ENCODED = MAX_OFFSET_SUPPORTED + OFFSET_CORRECTION;
    private static final int MIN_OFFSET_SUPPORTED_ENCODED = MIN_OFFSET_SUPPORTED + OFFSET_CORRECTION;

    /**
     * Maximum number of zone offsets cached. If a 25th needs to be cached, we clear the cache and start afresh.
     * <p>
     * Given the general practice of having full hour offsets and the limited number of zones per applications, this
     * should be more than sufficient for most applications.
     * </p>
     *
     * @see #cacheOffset(Integer, ZoneOffset)
     */
    private static final int MAX_CACHED_ZONE_OFFSETS = 24;
    /**
     * Maximum number of named zones cached. If a 10th needs to be cached, we clear the cache and start afresh.
     *
     * @see #cacheOffset(Integer, ZoneOffset)
     */
    private static final int MAX_CACHED_NAMED_ZONES = 10;
    private static final String FALLBACK_ZONE = "UTC";
    private static final ZoneId FALLBACK_ZONE_ID = ZoneOffset.UTC;
    private static final String KEY_MIN_ZONE_ID = "min_zone_id";
    private static final String FIREBIRD_TIME_ZONE_MAPPING_PROPERTIES = "firebird_time_zone_mapping.properties";

    /**
     * List of time zone names so that the index in the list corresponds to {@code 0xFFFF - RDB$TIME_ZONE_ID} (see
     * {@link #internalId(int)}).
     * <p>
     * This list is only used to map from Firebird to Java, in reverse we will always use offset.
     * </p>
     * <p>
     * When updating the list, make sure to update {@code min_zone_id} in the properties file and the
     * {@code TimeZoneByNameMappingTest}. Also verify if all instances are mapped (or otherwise where necessary, update
     * names to equivalent names). See also file {@code firebird_time_zone_mapping.properties}.
     * </p>
     */
    private final List<String> timeZoneNameById = loadTimeZoneNameById();
    private final Map<Integer, ZoneOffset> offsetCache = new ConcurrentHashMap<>(MAX_CACHED_ZONE_OFFSETS);
    private final Map<Integer, ZoneId> namedZoneCache = new ConcurrentHashMap<>(MAX_CACHED_NAMED_ZONES);

    public static TimeZoneMapping getInstance() {
        return INSTANCE;
    }

    /**
     * Maps a Firebird time zone id to a suitable Java {@link java.time.ZoneId}.
     * <p>
     * The returned value is either a named time zone (zone region), or an offset zone ({@link java.time.ZoneOffset}).
     * </p>
     *
     * @param timeZoneId
     *         Firebird time zone id (valid between between 0 and 65535)
     * @return {@code java.time.Zone} equivalent (out of range values or unmapped ids will return {@code UTC}).
     */
    public ZoneId timeZoneById(final int timeZoneId) {
        if (timeZoneId < 0 || timeZoneId > MAX_ZONE_ID) {
            return defaultForOutOfRange(timeZoneId);
        }
        return timeZoneId > MAX_OFFSET_ENCODED
                ? namedTimeZoneById(timeZoneId)
                : offsetTimeZoneFromId(timeZoneId);
    }

    /**
     * Determines if {@code timeZoneId} is an offset time zone or - possibly - a named time zone.
     *
     * @param timeZoneId
     *         Firebird time zone id
     * @return {@code true} if this is an offset time zone, {@code false} otherwise
     */
    public boolean isOffsetTimeZone(final int timeZoneId) {
        return timeZoneId >= 0 && timeZoneId <= MAX_OFFSET_ENCODED;
    }

    /**
     * Determines if {@code timeZoneId} is an offset time zone in the range supported by Jaybird [-18:00, +18:00].
     *
     * @param timeZoneId
     *         Firebird time zone id
     * @return {@code true} if this is an offset time zone in the range [-18:00, +18:00]
     */
    public boolean isSupportedOffsetTimezone(final int timeZoneId) {
        return timeZoneId >= MIN_OFFSET_SUPPORTED_ENCODED && timeZoneId <= MAX_OFFSET_SUPPORTED_ENCODED;
    }

    /**
     * Return the offset in minutes for an offset time zone id
     *
     * @param timeZoneId
     *         Offset time zone id (in range [0, 2878])
     * @return Offset in minutes, out of range values for {@code timeZoneId} will return {@code 0} for UTC/GMT.
     */
    public int toOffsetMinutes(final int timeZoneId) {
        if (isOffsetTimeZone(timeZoneId)) {
            return getOffsetMinutesUnchecked(timeZoneId);
        }
        logInvalidOffsetTimeZoneId(timeZoneId);
        return OFFSET_UTC;
    }

    private void logInvalidOffsetTimeZoneId(int timeZoneId) {
        if (logger.isWarnEnabled()) {
            String message = "Provided timezone id " + timeZoneId + " is not a valid offset time zone. "
                    + "Valid range is [0, " + MAX_OFFSET_ENCODED + "]. Returning offset 0 (UTC) instead.";
            if (logger.isDebugEnabled()) {
                logger.debug(message, new RuntimeException("debugging stacktrace"));
            } else {
                logger.warn(message + " See debug level for location.");
            }
        }
    }

    /**
     * Returns the Firebird time zone id for a {@link java.time.ZoneOffset}.
     *
     * @param zoneOffset
     *         The zone offset
     * @return Firebird time zone id
     */
    public int toTimeZoneId(ZoneOffset zoneOffset) {
        return toTimeZoneId(zoneOffset.getTotalSeconds() / 60);
    }

    /**
     * Return the Firebird time zone id for an offset in minutes.
     *
     * @param offsetMinutes
     *         Offset in minutes valid range [-1439, 1439] (or [-23:59, +23:59])
     * @return Time zone id encoding the specified offset, out of range values will return id for offset {@code 0} (UTC)
     */
    public int toTimeZoneId(final int offsetMinutes) {
        if (offsetMinutes < MIN_OFFSET || offsetMinutes > MAX_OFFSET) {
            logInvalidOffsetMinutes(offsetMinutes);
            return OFFSET_UTC_ENCODED;
        }
        return OFFSET_CORRECTION + offsetMinutes;
    }

    private void logInvalidOffsetMinutes(int offsetMinutes) {
        if (logger.isWarnEnabled()) {
            String message = "Offset value " + offsetMinutes + " out of range [" + MIN_OFFSET + ", " + MAX_OFFSET
                    + "]. Returning id for offset 0 instead.";
            if (logger.isDebugEnabled()) {
                logger.debug(message, new RuntimeException("debugging stacktrace"));
            } else {
                logger.warn(message + " See debug level for location");
            }
        }
    }

    private ZoneId offsetTimeZoneFromId(final int timeZoneId) {
        if (timeZoneId < MIN_OFFSET_SUPPORTED_ENCODED || timeZoneId > MAX_OFFSET_SUPPORTED_ENCODED) {
            return FALLBACK_ZONE_ID;
        }
        final Integer key = timeZoneId;
        ZoneOffset offsetId = offsetCache.get(key);
        if (offsetId != null) {
            return offsetId;
        }

        offsetId = calculateOffsetTimeZone(timeZoneId);
        cacheOffset(key, offsetId);
        return offsetId;
    }

    private void cacheOffset(final Integer key, final ZoneOffset offsetId) {
        if (offsetCache.size() > MAX_CACHED_ZONE_OFFSETS) {
            offsetCache.clear();
        }
        offsetCache.put(key, offsetId);
    }

    private ZoneOffset calculateOffsetTimeZone(final int timeZoneId) {
        int offset = Math.abs(getOffsetMinutesUnchecked(timeZoneId));
        int negative = timeZoneId < OFFSET_CORRECTION ? -1 : 1;
        int hours = negative * (offset / 60);
        int minutes = negative * (offset % 60);
        return ZoneOffset.ofHoursMinutes(hours, minutes);
    }

    private int getOffsetMinutesUnchecked(final int timeZoneId) {
        return timeZoneId - OFFSET_CORRECTION;
    }

    private ZoneId namedTimeZoneById(final int timeZoneId) {
        final Integer key = timeZoneId;
        ZoneId zoneId = namedZoneCache.get(timeZoneId);
        if (zoneId != null) {
            return zoneId;
        }

        final int internalId = internalId(timeZoneId);
        if (internalId < 0 || internalId >= timeZoneNameById.size()) {
            return defaultForOutOfRange(timeZoneId);
        }

        final String zoneIdName = timeZoneNameById.get(internalId);
        zoneId = zoneIdName != null ? ZoneId.of(zoneIdName, ZoneId.SHORT_IDS) : defaultForOutOfRange(timeZoneId);
        cacheNamedZone(key, zoneId);
        return zoneId;
    }

    private void cacheNamedZone(final Integer key, final ZoneId zoneId) {
        if (namedZoneCache.size() > MAX_CACHED_NAMED_ZONES) {
            namedZoneCache.clear();
        }
        namedZoneCache.put(key, zoneId);
    }

    private ZoneId defaultForOutOfRange(final int timeZoneId) {
        logOutOfRange(timeZoneId);
        return FALLBACK_ZONE_ID;
    }

    private void logOutOfRange(int timeZoneId) {
        if (logger.isWarnEnabled()) {
            String message = "Unmapped or out of range timezone id received, defaulting to " + FALLBACK_ZONE
                    + ", was id: " + timeZoneId + ".";
            if (logger.isDebugEnabled()) {
                logger.debug(message, new RuntimeException("debugging stacktrace"));
            } else {
                logger.warn(message + " See debug level for location.");
            }
        }
    }

    /**
     * Maps the Firebird time zone id to index for lookup in {@link #timeZoneNameById}.
     *
     * @param timeZoneId
     *         Firebird time zone id
     * @return Index suitable for lookup in {@link #timeZoneNameById} (value may be out of range for invalid ids)
     */
    private static int internalId(final int timeZoneId) {
        return MAX_ZONE_ID - timeZoneId;
    }

    /**
     * Maps an internal id to the Firebird time zone id.
     * <p>
     * Inverse of {@link #internalId(int)}.
     * </p>
     *
     * @param internalId
     *         Internal id (index for lookup)
     * @return Firebird time zone id.
     */
    private static int timeZoneId(final int internalId) {
        // if internalId = MAX_ZONE_ID - timeZoneId then timeZoneId = MAX_ZONE_ID - internalId
        return MAX_ZONE_ID - internalId;
    }

    private static List<String> loadTimeZoneNameById() {
        // TODO Potential loading issues?
        // TODO Just hard code it instead?
        try (InputStream in = TimeZoneMapping.class.getResourceAsStream(FIREBIRD_TIME_ZONE_MAPPING_PROPERTIES)) {
            final Properties timeZoneMapping = new Properties();
            timeZoneMapping.load(in);
            final int minZoneId = Integer.parseInt(timeZoneMapping.getProperty(KEY_MIN_ZONE_ID));

            final int zoneIdCount = internalId(minZoneId) + 1;
            final String[] zonesById = new String[zoneIdCount];

            for (int internalId = 0; internalId < zoneIdCount; internalId++) {
                final String timeZoneId = String.valueOf(timeZoneId(internalId));
                final String timeZoneName = StringUtils.trimToNull(timeZoneMapping.getProperty(timeZoneId));
                zonesById[internalId] = timeZoneName != null ? timeZoneName : FALLBACK_ZONE;
            }

            return Collections.unmodifiableList(Arrays.asList(zonesById));
        } catch (Exception e) {
            LoggerFactory.getLogger(TimeZoneMapping.class).warn(
                    "Unable to load Firebird time zone id to name mapping, only offset timezone support will be "
                            + "available ", e);
            // Populating with 65535 (internalId 0) == GMT
            return Collections.singletonList("GMT");
        }
    }

}
