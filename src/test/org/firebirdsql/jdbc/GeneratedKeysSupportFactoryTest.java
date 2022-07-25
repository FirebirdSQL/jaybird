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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.Set;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.firebirdsql.jdbc.GeneratedKeysSupportFactory.REASON_EXPLICITLY_DISABLED;
import static org.firebirdsql.jdbc.GeneratedKeysSupportFactory.REASON_NO_RETURNING_SUPPORT;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link GeneratedKeysSupportFactory}.
 * <p>
 * The normal 'default' generated keys behavior is tested in more detail in {@link GeneratedKeysQueryTest}.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class GeneratedKeysSupportFactoryTest {

    private static final String TEST_INSERT_QUERY = "INSERT INTO GENERATED_KEYS_TBL(NAME, TEXT_VALUE) VALUES (?, ?)";
    private static final String TEST_UPDATE_QUERY = "update generated_keys_tbl set text_value = ?";

    @Mock
    private FirebirdDatabaseMetaData dbMetadata;

    @Test
    void testCreateFor_disabled_noGeneratedKeys_asIs() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("DISABLED", dbMetadata);

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.NO_GENERATED_KEYS);

        assertFalse(query.generatesKeys(), "Should not generated keys");
        assertEquals(TEST_INSERT_QUERY, query.getQueryString(), "Should return unmodified query");
    }

    @Test
    void testCreateFor_disabled_returnGeneratedKeys_featureNotSupported() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("DISABLED", dbMetadata);

        assertExceptionExplicitlyDisabledReason(
                () -> generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS));
    }

    @Test
    void testCreateFor_disabled_invalidValue_invalidOptionValue() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("DISABLED", dbMetadata);

        SQLException exception = assertThrows(SQLNonTransientException.class,
                () ->generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, 132));
        assertThat(exception, allOf(
                fbMessageStartsWith(JaybirdErrorCodes.jb_invalidGeneratedKeysOption),
                sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_OPTION_IDENTIFIER)));
    }

    @Test
    void testCreateFor_disabled_columnIndexArray_featureNotSupported() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("DISABLED", dbMetadata);

        assertExceptionExplicitlyDisabledReason(
                () -> generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, new int[] { 1, 2 }));
    }

    @Test
    void testCreateFor_disabled_columnNameArray_featureNotSupported() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("disabled", dbMetadata);

        assertExceptionExplicitlyDisabledReason(
                () -> generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, new String[] { "ABC", "ID" }));
    }

    @Test
    void testCreateFor_ignored_noGeneratedKeys_asIs() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("IGNORED", dbMetadata);

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.NO_GENERATED_KEYS);

        assertFalse(query.generatesKeys(), "Should not generate keys");
        assertEquals(TEST_INSERT_QUERY, query.getQueryString(), "Should return unmodified query");
    }

    @Test
    void testCreateFor_ignored_returnGeneratedKeys_asIs() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("IGNORED", dbMetadata);

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertFalse(query.generatesKeys(), "Should not generate keys");
        assertEquals(TEST_INSERT_QUERY, query.getQueryString(), "Should return unmodified query");
    }

    @Test
    void testCreateFor_ignored_invalidValue_asIs() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("IGNORED", dbMetadata);

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertFalse(query.generatesKeys(), "Should not generate keys");
        assertEquals(TEST_INSERT_QUERY, query.getQueryString(), "Should return unmodified query");
    }

    @Test
    void testCreateFor_ignored_columnIndexArray_asIs() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("ignored", dbMetadata);

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, new int[] { 1, 2 });

        assertFalse(query.generatesKeys(), "Should not generate keys");
        assertEquals(TEST_INSERT_QUERY, query.getQueryString(), "Should return unmodified query");
    }

    @Test
    void testCreateFor_ignored_columnNameArray_asIs() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("IGNORED", dbMetadata);

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, new String[] { "ABC", "ID" });

        assertFalse(query.generatesKeys(), "Should not generate keys");
        assertEquals(TEST_INSERT_QUERY, query.getQueryString(), "Should return unmodified query");
    }

    @Test
    void testCreateFor_default_3_0_all() throws SQLException {
        // supports all query types
        prepareDatabaseVersionCheck(3, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("default", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(),
                equalTo(EnumSet.complementOf(EnumSet.of(GeneratedKeysSupport.QueryType.UNSUPPORTED))));
    }

    @Test
    void testCreateFor_default_2_0_insert_only() throws SQLException {
        // supports only insert
        prepareDatabaseVersionCheck(2, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("default", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(),
                equalTo(EnumSet.of(GeneratedKeysSupport.QueryType.INSERT)));
    }

    @Test
    void testCreateFor_default_2_5_allExceptMerge() throws SQLException {
        // supports all except merge
        prepareDatabaseVersionCheck(2, 5);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("default", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(),
                equalTo(EnumSet.complementOf(EnumSet.of(
                        GeneratedKeysSupport.QueryType.MERGE,
                        GeneratedKeysSupport.QueryType.UNSUPPORTED))));
    }

    @Test
    void testCreateFor_insert_update_3_0_both() throws SQLException {
        prepareDatabaseVersionCheck(3, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory
                .createFor("insert,update", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(),
                equalTo(EnumSet.of(GeneratedKeysSupport.QueryType.INSERT, GeneratedKeysSupport.QueryType.UPDATE)));
    }

    @Test
    void testCreateFor_insert_update_extra_whitespace_3_0_both() throws SQLException {
        prepareDatabaseVersionCheck(3, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory
                .createFor(" insert , update ", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(),
                equalTo(EnumSet.of(GeneratedKeysSupport.QueryType.INSERT, GeneratedKeysSupport.QueryType.UPDATE)));
    }

    @Test
    void testCreateFor_INSERT_UPDATE_3_0_both() throws SQLException {
        prepareDatabaseVersionCheck(3, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory
                .createFor("INSERT,UPDATE", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(),
                equalTo(EnumSet.of(GeneratedKeysSupport.QueryType.INSERT, GeneratedKeysSupport.QueryType.UPDATE)));
    }

    @Test
    void testCreateFor_insert_update_2_0_insert_only() throws SQLException {
        prepareDatabaseVersionCheck(2, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory
                .createFor("insert,update", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(),
                equalTo(EnumSet.of(GeneratedKeysSupport.QueryType.INSERT)));
    }

    @Test
    void testCreateFor_insert_gibberish_3_0_insert_only() throws SQLException {
        prepareDatabaseVersionCheck(3, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory
                .createFor("insert,gibberish", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(),
                equalTo(EnumSet.of(GeneratedKeysSupport.QueryType.INSERT)));
    }

    @Test
    void testCreateFor_allGibberish_3_0_none() throws SQLException {
        prepareDatabaseVersionCheck(3, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory
                .createFor("gibberish1,gibberish2", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(), is(empty()));

        // Verifies that not disabled, but ignored
        GeneratedKeysSupport.Query insertQuery =
                generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertFalse(insertQuery.generatesKeys(), "Should not generated keys");
        assertEquals(TEST_INSERT_QUERY, insertQuery.getQueryString(), "Should return unmodified query");
    }

    /**
     * Default for 1.5 and earlier is as if disabled because absence of RETURNING support
     */
    @Test
    void testCreateFor_default_1_5_featureNotSupported() throws Exception {
        prepareDatabaseVersionCheck(1, 5);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("default", dbMetadata);

        assertExceptionDisabledReason(REASON_NO_RETURNING_SUPPORT,
                () -> generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS));
    }

    /**
     * Default for 1.5 and earlier is as if disabled because absence of RETURNING support
     */
    @Test
    void testCreateFor_update_1_5_featureNotSupported() throws Exception {
        prepareDatabaseVersionCheck(1, 5);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("insert", dbMetadata);

        assertExceptionDisabledReason(REASON_NO_RETURNING_SUPPORT,
                () -> generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS));
    }

    /**
     * Contrast behavior with {@link #testCreateFor_update_1_5_featureNotSupported()}
     */
    @Test
    void testCreateFor_update_2_0_ignored() throws Exception {
        prepareDatabaseVersionCheck(2, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("update", dbMetadata);

        GeneratedKeysSupport.Query insertQuery =
                generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertFalse(insertQuery.generatesKeys(), "Should not generated keys");
        assertEquals(TEST_INSERT_QUERY, insertQuery.getQueryString(), "Should return unmodified query");

        GeneratedKeysSupport.Query updateQuery =
                generatedKeysSupport.buildQuery(TEST_UPDATE_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertFalse(updateQuery.generatesKeys(), "Should not generated keys");
        assertEquals(TEST_UPDATE_QUERY, updateQuery.getQueryString(), "Should return unmodified query");
    }

    private void assertExceptionExplicitlyDisabledReason(Executable executable) {
        assertExceptionDisabledReason(REASON_EXPLICITLY_DISABLED, executable);
    }

    private void assertExceptionDisabledReason(String reason, Executable executable) {
        SQLException exception = assertThrows(SQLFeatureNotSupportedException.class, executable);
        assertThat(exception, fbMessageStartsWith(JaybirdErrorCodes.jb_generatedKeysSupportNotAvailable, reason));
    }

    private void prepareDatabaseVersionCheck(final int major, final int minor) throws SQLException {
        when(dbMetadata.getDatabaseMajorVersion()).thenReturn(major);
        when(dbMetadata.getDatabaseMinorVersion()).thenReturn(minor);
    }

    private static Matcher<Set<GeneratedKeysSupport.QueryType>> equalTo(Set<GeneratedKeysSupport.QueryType> types) {
        return CoreMatchers.equalTo(types);
    }
}