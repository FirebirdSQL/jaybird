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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ClumpletReader;
import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.ByteArrayHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.gds.ISCConstants.isc_info_end;

/**
 * Cached info response.
 * <p>
 * This class holds an info response array, and can produce a filtered response holding only those items asked for.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5.0.8
 */
public class CachedInfoResponse {

    private static final Logger logger = LoggerFactory.getLogger(CachedInfoResponse.class);
    private static final CachedInfoResponse EMPTY = new CachedInfoResponse(ByteArrayHelper.emptyByteArray());

    private final byte[] infoResponse;

    /**
     * Constructs a cached info response.
     *
     * @param infoResponse
     *         byte array with info response; the caller of the constructor is responsible for cloning/copying the array
     *         if needed
     */
    public CachedInfoResponse(byte[] infoResponse) {
        this.infoResponse = requireNonNull(infoResponse, "infoResponse");
    }

    /**
     * @return a &mdash; possibly cached &mdash; empty cached info response
     */
    public static CachedInfoResponse empty() {
        return EMPTY;
    }

    /**
     * Produces a response with only the items in {@code requestItems}, allowing items to be missing.
     * <p>
     * If there are no matching items, or the cached response is empty, then a byte array with only
     * {@link org.firebirdsql.gds.ISCConstants#isc_info_end} is returned.
     * </p>
     *
     * @param requestItems
     *         requested info items
     * @return an info response with only the requested items, ending in
     * {@link org.firebirdsql.gds.ISCConstants#isc_info_end}
     * @see #filteredComplete(byte[])
     */
    public byte[] filtered(byte[] requestItems) {
        return filtered(requestItems, true).orElseGet(() -> new byte[] { isc_info_end });
    }

    /**
     * Produces a response with only the items in {@code requestItems}, requiring all items to be present.
     * <p>
     * If at least one item in {@code requestItems} (excluding {@link org.firebirdsql.gds.ISCConstants#isc_info_end}) is
     * not found in this cached info response, empty is returned.
     * </p>
     *
     * @param requestItems
     *         requested info items
     * @return an optional with an info response with only the requested items, and ending in
     * {@link org.firebirdsql.gds.ISCConstants#isc_info_end}, or empty if at least one of the requested items where not
     * found in the cached response, or if the cached response is empty
     * @see #filtered(byte[])
     */
    public Optional<byte[]> filteredComplete(byte[] requestItems) {
        return filtered(requestItems, false);
    }

    private Optional<byte[]> filtered(byte[] requestItems, boolean returnPartial) {
        if (infoResponse.length == 0) {
            // Nothing cached
            return Optional.empty();
        }
        try {
            ClumpletReader requested = new ClumpletReader(ClumpletReader.Kind.InfoItems, requestItems);
            ClumpletReader cached = new ClumpletReader(ClumpletReader.Kind.InfoResponse, infoResponse);
            ByteArrayOutputStream response = new ByteArrayOutputStream();
            for (requested.rewind(); !requested.isEof(); requested.moveNext()) {
                int requestItem = requested.getClumpTag();
                if (requestItem == isc_info_end) {
                    break;
                } else if (cached.find(requestItem)) {
                    byte[] data = cached.getBytes();
                    response.write(requestItem);
                    VaxEncoding.encodeVaxInteger2WithoutLength(response, data.length);
                    response.write(data);
                } else if (!returnPartial) {
                    logger.debugf("Requested info item %d not in cache, returning empty", requestItem);
                    return Optional.empty();
                }
            }
            response.write(isc_info_end);
            return Optional.of(response.toByteArray());
        } catch (IOException | SQLException e) {
            logger.warn("Error in filteredResponse, returning empty", e);
            return Optional.empty();
        }
    }

    /**
     * @return a copy of the full info response array
     */
    public byte[] infoResponse() {
        return infoResponse.clone();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(infoResponse);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CachedInfoResponse
                && Arrays.equals(this.infoResponse, ((CachedInfoResponse) obj).infoResponse);
    }

}
