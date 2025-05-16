// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ClumpletReader;
import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.jaybird.util.ByteArrayHelper;
import org.jspecify.annotations.NullMarked;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.Objects.requireNonNull;
import static org.firebirdsql.gds.ISCConstants.isc_info_end;

/**
 * Cached info response.
 * <p>
 * This record holds an info response array, and can produce a filtered response holding only those items asked for.
 * </p>
 *
 * @param infoResponse
 *         byte array with info response; the caller of the constructor is responsible for cloning/copying the array if
 *         needed
 * @author Mark Rotteveel
 * @since 7
 */
@NullMarked
public record CachedInfoResponse(byte[] infoResponse) {

    private static final System.Logger logger = System.getLogger(CachedInfoResponse.class.getName());
    private static final CachedInfoResponse EMPTY = new CachedInfoResponse(ByteArrayHelper.emptyByteArray());

    public CachedInfoResponse {
        requireNonNull(infoResponse, "infoResponse");
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
            var requested = new ClumpletReader(ClumpletReader.Kind.InfoItems, requestItems);
            var cached = new ClumpletReader(ClumpletReader.Kind.InfoResponse, infoResponse);
            var response = new ByteArrayOutputStream();
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
                    logger.log(DEBUG, "Requested info item {0} not in cache, returning empty", requestItem);
                    return Optional.empty();
                }
            }
            response.write(isc_info_end);
            return Optional.of(response.toByteArray());
        } catch (IOException | SQLException e) {
            logger.log(WARNING, "Error in filteredResponse, returning empty", e);
            return Optional.empty();
        }
    }

    /**
     * @return a copy of the full info response array
     */
    @Override
    public byte[] infoResponse() {
        return infoResponse.clone();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(infoResponse);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CachedInfoResponse that && Arrays.equals(this.infoResponse, that.infoResponse);
    }

}
