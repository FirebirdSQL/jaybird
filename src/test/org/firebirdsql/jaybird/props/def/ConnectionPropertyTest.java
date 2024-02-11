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
package org.firebirdsql.jaybird.props.def;

import org.firebirdsql.jaybird.props.DpbType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.firebirdsql.jaybird.fb.constants.DpbItems.isc_dpb_user;
import static org.firebirdsql.jaybird.fb.constants.SpbItems.isc_spb_user_name;
import static org.firebirdsql.jaybird.props.def.ConnectionProperty.builder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ConnectionPropertyTest {

    @SuppressWarnings("java:S5778")
    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { " ", "  ", "\t" })
    void testBlankOrNullNameNotAllowed(String blankName) {
        assertThatThrownBy(() -> builder().name(blankName).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("name");
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { " ", "  ", "\t" })
    void testBlankOrNullAlias_excluded(String blankAlias) {
        ConnectionProperty connectionProperty = builder("name")
                .aliases("notBlank", blankAlias, " secondNotBlankWithWhitespace ")
                .build();

        assertThat(connectionProperty.aliases())
                .containsExactly("notBlank", "secondNotBlankWithWhitespace");
    }

    @Test
    void testEqualsDependsOnNameOnly() {
        ConnectionProperty connectionProperty = builder("name")
                .aliases("a", "b")
                .build();
        ConnectionProperty unknownPropertyName = ConnectionProperty.unknown("name");
        ConnectionProperty unknownPropertyOther = ConnectionProperty.unknown("other");

        assertAll(
                () -> assertThat(connectionProperty).isEqualTo(connectionProperty),
                () -> assertThat(connectionProperty).isEqualTo(unknownPropertyName),
                () -> assertThat(connectionProperty).isNotEqualTo(unknownPropertyOther),
                () -> assertThat(unknownPropertyName).isEqualTo(connectionProperty),
                () -> assertThat(unknownPropertyName).isNotEqualTo(unknownPropertyOther),
                () -> assertThat(unknownPropertyOther).isNotEqualTo(connectionProperty),
                () -> assertThat(unknownPropertyOther).isNotEqualTo(unknownPropertyName)
        );
    }

    @Test
    void testIsIdenticalTo() {
        // we are not testing all combinations
        ConnectionProperty connectionProperty = builder("name")
                .aliases("a", "b")
                .build();
        ConnectionProperty connectionPropertyCopy = builder("name")
                .aliases("a", "b")
                .build();
        ConnectionProperty unknownPropertyName = ConnectionProperty.unknown("name");

        assertAll(
                () -> assertThat(connectionProperty).matches(connectionProperty::isIdenticalTo),
                () -> assertThat(connectionProperty).matches(connectionPropertyCopy::isIdenticalTo),
                () -> assertThat(connectionProperty).matches(prop -> !unknownPropertyName.isIdenticalTo(prop))
        );
    }

    @ParameterizedTest
    @EnumSource(ConnectionPropertyType.class)
    void pbTypeIsNONEWhenDpbItemNotSet(ConnectionPropertyType connectionPropertyType) {
        ConnectionProperty property = builder("propName").type(connectionPropertyType).build();

        assertThat(property.pbType()).isEqualTo(DpbType.NONE);
    }

    @ParameterizedTest
    @CsvSource({
            "STRING,   STRING",
            "INT,      INT",
            "BOOLEAN,  SINGLE",
    })
    void defaultDpbTypesForConnectionPropertyType(ConnectionPropertyType connectionPropertyType,
            DpbType expectedPbType) {
        ConnectionProperty property = builder("propName").type(connectionPropertyType).dpbItem(isc_dpb_user).build();

        assertThat(property.pbType()).isEqualTo(expectedPbType);
    }

    @Test
    void canSetPbTypeNONEIfDpbItemNotSet() {
        assertDoesNotThrow(() -> builder().pbType(DpbType.NONE));
    }

    @Test
    void cannotSetPbTypeNONEIfDpbItemSet() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> builder().dpbItem(isc_dpb_user).pbType(DpbType.NONE));
    }

    @Test
    void cannotSetPbTypeNONEIfSpbItemSet() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> builder().spbItem(isc_spb_user_name).pbType(DpbType.NONE));
    }

    @ParameterizedTest
    @EnumSource(value = DpbType.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
    void cannotSetPbTypeOtherThanNONEIfDpbOrSpbItemNotSet(DpbType dpbType) {
        assertThatIllegalArgumentException().isThrownBy(() -> builder().pbType(dpbType));
    }

    @ParameterizedTest
    @EnumSource(value = DpbType.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
    void canSetPbTypeOtherThanNONEIfDpbItemSet(DpbType dpbType) {
        assertDoesNotThrow(() -> builder().dpbItem(isc_dpb_user).pbType(dpbType));
    }

    @ParameterizedTest
    @EnumSource(value = DpbType.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
    void canSetPbTypeOtherThanNONEIfSpbItemSet(DpbType dpbType) {
        assertDoesNotThrow(() -> builder().spbItem(isc_spb_user_name).pbType(dpbType));
    }

    @Test
    void clearingDpbItemResetsPbTypeToNONE() {
        ConnectionProperty propWithDpbItem = builder("propName")
                .dpbItem(isc_dpb_user).pbType(DpbType.SINGLE).build();
        assumeThat(propWithDpbItem.pbType()).isEqualTo(DpbType.SINGLE);

        ConnectionProperty propWithoutDpbItem = builder("propName")
                .dpbItem(isc_dpb_user).pbType(DpbType.SINGLE).dpbItem(ConnectionProperty.NO_DPB_ITEM).build();
        assertThat(propWithoutDpbItem.pbType()).isEqualTo(DpbType.NONE);
    }

    @Test
    void clearingSpbItemResetsPbTypeToNONE() {
        ConnectionProperty propWithDpbItem = builder("propName")
                .spbItem(isc_spb_user_name).pbType(DpbType.SINGLE).build();
        assumeThat(propWithDpbItem.pbType()).isEqualTo(DpbType.SINGLE);

        ConnectionProperty propWithoutSpbItem = builder("propName")
                .spbItem(isc_spb_user_name).pbType(DpbType.SINGLE).spbItem(ConnectionProperty.NO_SPB_ITEM).build();
        assertThat(propWithoutSpbItem.pbType()).isEqualTo(DpbType.NONE);
    }

    @Test
    void clearingDpbOrSpbItemWhenSpbOrPbIsSetDoesNotResetPbTypeToNONE() {
        ConnectionProperty propWithDpbItem = builder("propName")
                .dpbItem(isc_dpb_user).spbItem(isc_spb_user_name).pbType(DpbType.SINGLE).build();
        assumeThat(propWithDpbItem.pbType()).isEqualTo(DpbType.SINGLE);

        ConnectionProperty propWithoutDpbItem = builder("propName")
                .dpbItem(isc_dpb_user).spbItem(isc_spb_user_name).pbType(DpbType.SINGLE)
                .dpbItem(ConnectionProperty.NO_DPB_ITEM).build();
        assertThat(propWithoutDpbItem.pbType()).isEqualTo(DpbType.SINGLE);
        ConnectionProperty propWithoutSpbItem = builder("propName")
                .dpbItem(isc_dpb_user).spbItem(isc_spb_user_name).pbType(DpbType.SINGLE)
                .spbItem(ConnectionProperty.NO_SPB_ITEM).build();
        assertThat(propWithoutSpbItem.pbType()).isEqualTo(DpbType.SINGLE);
        ConnectionProperty propWithoutDpbAndSpbItem = builder("propName")
                .dpbItem(isc_dpb_user).spbItem(isc_spb_user_name).pbType(DpbType.SINGLE)
                .dpbItem(ConnectionProperty.NO_DPB_ITEM).spbItem(ConnectionProperty.NO_SPB_ITEM).build();
        assertThat(propWithoutDpbAndSpbItem.pbType()).isEqualTo(DpbType.NONE);
    }

}