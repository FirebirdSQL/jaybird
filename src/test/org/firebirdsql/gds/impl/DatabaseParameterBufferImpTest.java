// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.impl.DatabaseParameterBufferImp.DpbMetaData;
import org.firebirdsql.gds.impl.argument.Argument;
import org.firebirdsql.gds.impl.argument.ArgumentType;
import org.firebirdsql.gds.impl.argument.BigIntArgument;
import org.firebirdsql.gds.impl.argument.ByteArgument;
import org.firebirdsql.gds.impl.argument.ByteArrayArgument;
import org.firebirdsql.gds.impl.argument.NumericArgument;
import org.firebirdsql.gds.impl.argument.SingleItem;
import org.firebirdsql.gds.impl.argument.StringArgument;
import org.firebirdsql.gds.impl.argument.TypedArgument;
import org.firebirdsql.jaybird.fb.constants.DpbItems;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

class DatabaseParameterBufferImpTest {

    private static final IEncodingFactory encodingFactory = EncodingFactory.getPlatformDefault();

    @Test
    void testUpgradeV1ToV2ForTooLongArgument() {
        var dpb = new DatabaseParameterBufferImp(DpbMetaData.DPB_VERSION_1,
                encodingFactory.getEncodingForFirebirdName("UTF8"));
        dpb.addArgument(DpbItems.isc_dpb_user, "username");
        assertArgument(dpb, DpbItems.isc_dpb_user, ArgumentType.TraditionalDpb, StringArgument.class, "username");
        dpb.addArgument(DpbItems.isc_dpb_nolinger);
        dpb.addArgument(DpbItems.isc_dpb_num_buffers, 1000);
        dpb.addArgument(DpbItems.isc_dpb_dummy_packet_interval, 1000L);
        dpb.addArgument(DpbItems.isc_dpb_sql_role_name, "TEST_ROLE".getBytes(StandardCharsets.UTF_8));
        dpb.addArgument(DpbItems.isc_dpb_debug, (byte) 1);

        assertEquals(DpbMetaData.DPB_VERSION_1, dpb.getParameterBufferMetaData(), "metadata before upgrade");

        // forces DPB upgrade as it exceeds V1 maximum length of 255
        String password = "a".repeat(256);
        dpb.addArgument(DpbItems.isc_dpb_password, password);

        assertEquals(DpbMetaData.DPB_VERSION_2, dpb.getParameterBufferMetaData(), "metadata after upgrade");
        assertArgument(dpb, DpbItems.isc_dpb_user, ArgumentType.Wide, StringArgument.class, "username");
        assertArgument(dpb, DpbItems.isc_dpb_nolinger, ArgumentType.Wide, SingleItem.class, null);
        assertArgument(dpb, DpbItems.isc_dpb_num_buffers, ArgumentType.Wide, NumericArgument.class, 1000);
        assertArgument(dpb, DpbItems.isc_dpb_dummy_packet_interval, ArgumentType.Wide, BigIntArgument.class, 1000L);
        assertArgument(dpb, DpbItems.isc_dpb_sql_role_name, ArgumentType.Wide, ByteArrayArgument.class,
                "TEST_ROLE".getBytes(StandardCharsets.UTF_8));
        assertArgument(dpb, DpbItems.isc_dpb_debug, ArgumentType.Wide, ByteArgument.class, 1);
        assertArgument(dpb, DpbItems.isc_dpb_password, ArgumentType.Wide, StringArgument.class, password);
    }

    private static void assertArgument(ParameterBufferBase dpb, int type, ArgumentType expectedArgumentType,
            Class<? extends Argument> expectedClass, @Nullable Object expectedValue) {
        dpb.findFirst(type).ifPresentOrElse(
                argument -> {
                    assertInstanceOf(expectedClass, argument, "argument of wrong class");
                    assertEquals(type, argument.getType(), "argument has wrong type");
                    assertEquals(expectedArgumentType, ((TypedArgument) argument).getArgumentType(),
                            "argument has wrong argument type");
                    if (expectedValue instanceof String expectedString) {
                        assertEquals(expectedString, argument.getValueAsString(), "string value");
                    } else if (expectedValue instanceof Integer expectedInteger) {
                        assertEquals(expectedInteger, argument.getValueAsInt(), "int value");
                    } else if (expectedValue instanceof Long expectedLong) {
                        assertEquals(expectedLong, argument.getValueAsLong(), "long value");
                    } else if (expectedValue instanceof byte[] expectedBytes) {
                        var byteArrayArgument = assertInstanceOf(ByteArrayArgument.class, argument);
                        assertArrayEquals(expectedBytes, byteArrayArgument.getValueAsBytes(), "byte[] value");
                    }
                },
                () -> fail("argument " + type + " was expected but not found")
        );
    }

}
