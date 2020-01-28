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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Tests for {@link GeneratedKeysSupportFactory}.
 * <p>
 * The normal 'default' generated keys behavior is tested in more detail in {@link TestGeneratedKeysQuery}.
 * </p>
 */
public class GeneratedKeysSupportFactoryTest {

    private static final String TEST_INSERT_QUERY = "INSERT INTO GENERATED_KEYS_TBL(NAME, TEXT_VALUE) VALUES (?, ?)";
    private static final String TEST_UPDATE_QUERY = "update generated_keys_tbl set text_value = ?";

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    private FirebirdDatabaseMetaData dbMetadata;

    @Test
    public void testCreateFor_disabled_noGeneratedKeys_asIs() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("DISABLED", dbMetadata);

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.NO_GENERATED_KEYS);

        assertFalse("Should not generated keys", query.generatesKeys());
        assertEquals("Should return unmodified query", TEST_INSERT_QUERY, query.getQueryString());
    }

    @Test
    public void testCreateFor_disabled_returnGeneratedKeys_featureNotSupported() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("DISABLED", dbMetadata);

        expectExceptionExplicitlyDisabledReason();

        generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
    }

    @Test
    public void testCreateFor_disabled_invalidValue_invalidOptionValue() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("DISABLED", dbMetadata);

        expectedException.expect(SQLNonTransientException.class);
        expectedException.expect(allOf(
                fbMessageStartsWith(JaybirdErrorCodes.jb_invalidGeneratedKeysOption),
                sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_OPTION_IDENTIFIER)));

        generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, 132);
    }

    @Test
    public void testCreateFor_disabled_columnIndexArray_featureNotSupported() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("DISABLED", dbMetadata);

        expectExceptionExplicitlyDisabledReason();

        generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, new int[] { 1, 2 });
    }

    @Test
    public void testCreateFor_disabled_columnNameArray_featureNotSupported() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("disabled", dbMetadata);

        expectExceptionExplicitlyDisabledReason();

        generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, new String[] { "ABC", "ID" });
    }

    @Test
    public void testCreateFor_ignored_noGeneratedKeys_asIs() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("IGNORED", dbMetadata);

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.NO_GENERATED_KEYS);

        assertFalse("Should not generate keys", query.generatesKeys());
        assertEquals("Should return unmodified query", TEST_INSERT_QUERY, query.getQueryString());
    }

    @Test
    public void testCreateFor_ignored_returnGeneratedKeys_asIs() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("IGNORED", dbMetadata);

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertFalse("Should not generate keys", query.generatesKeys());
        assertEquals("Should return unmodified query", TEST_INSERT_QUERY, query.getQueryString());
    }

    @Test
    public void testCreateFor_ignored_invalidValue_asIs() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("IGNORED", dbMetadata);

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertFalse("Should not generate keys", query.generatesKeys());
        assertEquals("Should return unmodified query", TEST_INSERT_QUERY, query.getQueryString());
    }

    @Test
    public void testCreateFor_ignored_columnIndexArray_asIs() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("ignored", dbMetadata);

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, new int[] { 1, 2 });

        assertFalse("Should not generate keys", query.generatesKeys());
        assertEquals("Should return unmodified query", TEST_INSERT_QUERY, query.getQueryString());
    }

    @Test
    public void testCreateFor_ignored_columnNameArray_asIs() throws Exception {
        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("IGNORED", dbMetadata);

        GeneratedKeysSupport.Query query = generatedKeysSupport
                .buildQuery(TEST_INSERT_QUERY, new String[] { "ABC", "ID" });

        assertFalse("Should not generate keys", query.generatesKeys());
        assertEquals("Should return unmodified query", TEST_INSERT_QUERY, query.getQueryString());
    }

    @Test
    public void testCreateFor_default_3_0_all() throws SQLException {
        // supports all query types
        expectDatabaseVersionCheck(3, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("default", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(), 
                equalTo(EnumSet.complementOf(EnumSet.of(GeneratedKeysSupport.QueryType.UNSUPPORTED))));
    }

    @Test
    public void testCreateFor_default_2_0_insert_only() throws SQLException {
        // supports only insert
        expectDatabaseVersionCheck(2, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("default", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(),
                equalTo(EnumSet.of(GeneratedKeysSupport.QueryType.INSERT)));
    }

    @Test
    public void testCreateFor_default_2_5_allExceptMerge() throws SQLException {
        // supports all except merge
        expectDatabaseVersionCheck(2, 5);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("default", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(),
                equalTo(EnumSet.complementOf(EnumSet.of(
                        GeneratedKeysSupport.QueryType.MERGE,
                        GeneratedKeysSupport.QueryType.UNSUPPORTED))));
    }

    @Test
    public void testCreateFor_insert_update_3_0_both() throws SQLException {
        expectDatabaseVersionCheck(3, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory
                .createFor("insert,update", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(),
                equalTo(EnumSet.of(GeneratedKeysSupport.QueryType.INSERT, GeneratedKeysSupport.QueryType.UPDATE)));
    }

    @Test
    public void testCreateFor_insert_update_extra_whitespace_3_0_both() throws SQLException {
        expectDatabaseVersionCheck(3, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory
                .createFor(" insert , update ", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(),
                equalTo(EnumSet.of(GeneratedKeysSupport.QueryType.INSERT, GeneratedKeysSupport.QueryType.UPDATE)));
    }

    @Test
    public void testCreateFor_INSERT_UPDATE_3_0_both() throws SQLException {
        expectDatabaseVersionCheck(3, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory
                .createFor("INSERT,UPDATE", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(),
                equalTo(EnumSet.of(GeneratedKeysSupport.QueryType.INSERT, GeneratedKeysSupport.QueryType.UPDATE)));
    }

    @Test
    public void testCreateFor_insert_update_2_0_insert_only() throws SQLException {
        expectDatabaseVersionCheck(2, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory
                .createFor("insert,update", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(),
                equalTo(EnumSet.of(GeneratedKeysSupport.QueryType.INSERT)));
    }

    @Test
    public void testCreateFor_insert_gibberish_3_0_insert_only() throws SQLException {
        expectDatabaseVersionCheck(3, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory
                .createFor("insert,gibberish", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(),
                equalTo(EnumSet.of(GeneratedKeysSupport.QueryType.INSERT)));
    }

    @Test
    public void testCreateFor_allGibberish_3_0_none() throws SQLException {
        expectDatabaseVersionCheck(3, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory
                .createFor("gibberish1,gibberish2", dbMetadata);

        assertThat(generatedKeysSupport.supportedQueryTypes(), is(empty()));

        // Verifies that not disabled, but ignored
        GeneratedKeysSupport.Query insertQuery =
                generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertFalse("Should not generated keys", insertQuery.generatesKeys());
        assertEquals("Should return unmodified query", TEST_INSERT_QUERY, insertQuery.getQueryString());
    }

    /**
     * Default for 1.5 and earlier is as if disabled because absence of RETURNING support
     */
    @Test
    public void testCreateFor_default_1_5_featureNotSupported() throws Exception {
        expectDatabaseVersionCheck(1, 5);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("default", dbMetadata);

        expectExceptionDisabledReason(REASON_NO_RETURNING_SUPPORT);

        generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
    }

    /**
     * Default for 1.5 and earlier is as if disabled because absence of RETURNING support
     */
    @Test
    public void testCreateFor_update_1_5_featureNotSupported() throws Exception {
        expectDatabaseVersionCheck(1, 5);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("insert", dbMetadata);

        expectExceptionDisabledReason(REASON_NO_RETURNING_SUPPORT);

        generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
    }

    /**
     * Contrast behavior with {@link #testCreateFor_update_1_5_featureNotSupported()}
     */
    @Test
    public void testCreateFor_update_2_0_ignored() throws Exception {
        expectDatabaseVersionCheck(2, 0);

        GeneratedKeysSupport generatedKeysSupport = GeneratedKeysSupportFactory.createFor("update", dbMetadata);

        GeneratedKeysSupport.Query insertQuery =
                generatedKeysSupport.buildQuery(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertFalse("Should not generated keys", insertQuery.generatesKeys());
        assertEquals("Should return unmodified query", TEST_INSERT_QUERY, insertQuery.getQueryString());

        GeneratedKeysSupport.Query updateQuery =
                generatedKeysSupport.buildQuery(TEST_UPDATE_QUERY, Statement.RETURN_GENERATED_KEYS);

        assertFalse("Should not generated keys", updateQuery.generatesKeys());
        assertEquals("Should return unmodified query", TEST_UPDATE_QUERY, updateQuery.getQueryString());
    }

    private void expectExceptionExplicitlyDisabledReason() {
        expectExceptionDisabledReason(REASON_EXPLICITLY_DISABLED);
    }

    private void expectExceptionDisabledReason(String reason) {
        expectedException.expect(SQLFeatureNotSupportedException.class);
        expectedException.expect(fbMessageStartsWith(JaybirdErrorCodes.jb_generatedKeysSupportNotAvailable,
                reason));
    }

    private void expectDatabaseVersionCheck(final int major, final int minor) throws SQLException {
        context.checking(new Expectations() {{
            oneOf(dbMetadata).getDatabaseMajorVersion(); will(returnValue(major));
            oneOf(dbMetadata).getDatabaseMinorVersion(); will(returnValue(minor));
        }});
    }

    private static Matcher<Set<GeneratedKeysSupport.QueryType>> equalTo(Set<GeneratedKeysSupport.QueryType> types) {
        return CoreMatchers.equalTo(types);
    }
}