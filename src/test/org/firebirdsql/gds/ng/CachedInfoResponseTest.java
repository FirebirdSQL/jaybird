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

import org.firebirdsql.common.InfoResponseWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.firebirdsql.common.DataGenerator.createRandomBytes;
import static org.firebirdsql.gds.ISCConstants.isc_info_end;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link CachedInfoResponse}.
 *
 * @author Mark Rotteveel
 */
class CachedInfoResponseTest {

    @Test
    void infoResponseNull_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CachedInfoResponse(null));
    }

    @Test
    void empty_hasZeroLengthInfoResponse() {
        CachedInfoResponse empty = CachedInfoResponse.empty();
        assertEquals(0, empty.infoResponse().length, "expected empty info response");
    }

    @Test
    void infoResponse_copyOfOriginal() {
        final byte[] originalInfoResponse = createRandomBytes(20);
        CachedInfoResponse cached = new CachedInfoResponse(originalInfoResponse);

        byte[] infoResponse = cached.infoResponse();
        assertNotSame(originalInfoResponse, infoResponse, "arrays should be different");
        assertArrayEquals(originalInfoResponse, infoResponse, "arrays should have same content");
    }

    @Test
    void filtered_returnsItemsRequested() throws Exception {
        final byte[] originalInfoResponse = new InfoResponseWriter()
                .addInt(10, 5)
                .addInt(11, 10)
                .addInt(12, 15)
                .addInt(13, 20)
                .addInt(14, 25)
                .toArray();
        CachedInfoResponse cached = new CachedInfoResponse(originalInfoResponse);

        final byte[] expectedResponse = new InfoResponseWriter()
                .addInt(13, 20)
                .addInt(11, 10)
                .toArray();

        // We're requesting 2 items not in the cached info response and 2 out of 5 items that do exist out of order
        byte[] infoResponse = cached.filtered(new byte[] { 15, 13, 11, 16, isc_info_end });
        assertArrayEquals(expectedResponse, infoResponse, "filtered info response has unexpected content");
    }

    @Test
    void filtered_doesNotRequire_isc_info_end() throws Exception {
        final byte[] originalInfoResponse = new InfoResponseWriter()
                .addInt(10, 5)
                .addInt(11, 10)
                .addInt(12, 15)
                .addInt(13, 20)
                .addInt(14, 25)
                .toArray();
        CachedInfoResponse cached = new CachedInfoResponse(originalInfoResponse);

        // We're requesting 2 items not in the cached info response and 2 out of 5 items that do exist out of order
        byte[] infoResponseRequestedWithEnd = cached.filtered(new byte[] { 15, 13, 11, 16, isc_info_end });
        byte[] infoResponseRequestedWithoutEnd = cached.filtered(new byte[] { 15, 13, 11, 16 });
        assertArrayEquals(infoResponseRequestedWithEnd, infoResponseRequestedWithoutEnd,
                "requesting with or without isc_info_end should not make a difference");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void filtered_noMatchingItems_returnsOnly_isc_info_end(boolean infoEmpty) throws Exception {
        final byte[] originalInfoResponse = infoEmpty
                ? new byte[0]
                : new InfoResponseWriter()
                        .addInt(10, 5)
                        .addInt(11, 10)
                        .toArray();
        CachedInfoResponse cached = new CachedInfoResponse(originalInfoResponse);

        byte[] expected = { isc_info_end };
        assertArrayEquals(expected, cached.filtered(new byte[] { 12 }));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void filteredComplete_returnsEmptyIfItemMissing(boolean infoEmpty) throws Exception {
        final byte[] originalInfoResponse = infoEmpty
                ? new byte[0]
                : new InfoResponseWriter()
                        .addInt(10, 5)
                        .addInt(11, 10)
                        .toArray();
        CachedInfoResponse cached = new CachedInfoResponse(originalInfoResponse);

        assertThat(cached.filteredComplete(new byte[] { 11, 12, isc_info_end }), is(emptyOptional()));
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    void filteredComplete_returnsItemsRequested() throws Exception {
        final byte[] originalInfoResponse = new InfoResponseWriter()
                .addInt(10, 5)
                .addInt(11, 10)
                .addInt(12, 15)
                .toArray();
        CachedInfoResponse cached = new CachedInfoResponse(originalInfoResponse);

        final byte[] expectedResponse = new InfoResponseWriter()
                .addInt(12, 15)
                .addInt(11, 10)
                .toArray();

        Optional<byte[]> optionalInfoResponse = cached.filteredComplete(new byte[] { 12, 11, isc_info_end });
        assertThat(optionalInfoResponse, is(optionalWithValue()));
        assertArrayEquals(expectedResponse, optionalInfoResponse.get(),
                "filtered info response has unexpected content");
    }

}