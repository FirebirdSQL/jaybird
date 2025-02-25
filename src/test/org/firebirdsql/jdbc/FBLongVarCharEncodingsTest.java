// SPDX-FileCopyrightText: Copyright 2002-2010 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2011-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

/**
 * This test case tests encodings in text blobs.
 *
 * @author Roman Rokytskyy
 * @version 1.0
 */
class FBLongVarCharEncodingsTest extends FBEncodingsTest {

    //@formatter:off
    private static final String CREATE_TABLE =
            "CREATE TABLE test_encodings (" +
            "  id INTEGER, " +
            "  win1250_field BLOB SUB_TYPE 1 CHARACTER SET WIN1250, " +
            "  win1251_field BLOB SUB_TYPE 1 CHARACTER SET WIN1251, " +
            "  win1252_field BLOB SUB_TYPE 1 CHARACTER SET WIN1252, " +
            "  win1253_field BLOB SUB_TYPE 1 CHARACTER SET WIN1253, " +
            "  win1254_field BLOB SUB_TYPE 1 CHARACTER SET WIN1254, " +
            "  unicode_field BLOB SUB_TYPE 1 CHARACTER SET UNICODE_FSS, " +
            "  ascii_field BLOB SUB_TYPE 1 CHARACTER SET ASCII, " +
            "  none_field BLOB SUB_TYPE 1 CHARACTER SET NONE, " +
            "  char_field BLOB SUB_TYPE 1 CHARACTER SET UNICODE_FSS, " +
            "  octets_field BLOB SUB_TYPE 1 CHARACTER SET OCTETS, " +
            "  var_octets_field BLOB SUB_TYPE 1 CHARACTER SET OCTETS, " +
            "  none_octets_field BLOB SUB_TYPE 1 CHARACTER SET NONE, " +
            "  uuid_char BLOB SUB_TYPE 1 CHARACTER SET UTF8, " +
            "  uuid_varchar BLOB SUB_TYPE 1 CHARACTER SET UTF8 " +
            ")";
    //@formatter:on

    @Override
    protected String getCreateTableStatement() {
        return CREATE_TABLE;
    }

    @Override
    void testPadding() {
        // test is not relevant
    }

    @Override
    protected byte[] getOctetsFullLength() {
        // This is to make sure testOctets() works correctly as 
        // the datatype of the fields is different than defined in TestFBEncodings
        return OCTETS_DATA;
    }

    @Override
    protected byte[] getOctetsFullLengthAsNone() {
        // This is to make sure testOctets() works correctly as
        // the datatype of the fields is different than defined in TestFBEncodings
        return OCTETS_DATA;
    }
}