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
package org.firebirdsql.jaybird.props.internal;

import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.def.ConnectionPropertyType;
import org.firebirdsql.jaybird.props.internal.ConnectionPropertyRegistry.ConnectionPropertiesBuilder;
import org.firebirdsql.jaybird.props.spi.ConnectionPropertyDefinerSpi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.firebirdsql.jaybird.fb.constants.DpbItems.isc_dpb_user_name;
import static org.firebirdsql.jaybird.fb.constants.SpbItems.isc_spb_user_name;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link ConnectionPropertyRegistry}.
 */
@ExtendWith(MockitoExtension.class)
class ConnectionPropertyRegistryTest {

    private static final String PROP_NAME = "knownValue";
    private static ConnectionProperty connectionProperty() {
        return ConnectionProperty.builder().name(PROP_NAME)
                .aliases("known_value")
                .dpbItem(isc_dpb_user_name).spbItem(isc_spb_user_name).build();
    }

    @Test
    void verifySomePropertiesOfStandardInstance() {
        ConnectionPropertyRegistry connectionPropertyRegistry = ConnectionPropertyRegistry.getInstance();

        // Tests a small subset of property names and aliases
        assertThat(connectionPropertyRegistry.getRegisteredNames())
                .contains("user", "userName", "password", "roleName", "sqlRole", "dbCryptConfig");

        // Tests a small subset of properties for a subset of their values
        assertThat(connectionPropertyRegistry.getRegisteredProperties())
                .anySatisfy(prop -> {
                    assertThat(prop.name()).isEqualTo("user");
                    assertThat(prop.aliases())
                            .containsExactlyInAnyOrder("userName", "user_name", "isc_dpb_user_name");
                    assertThat(prop.hasDpbItem()).isFalse();
                    assertThat(prop.hasSpbItem()).isFalse();
                })
                .anySatisfy(prop -> {
                    assertThat(prop.name()).isEqualTo("wireCompression");
                    assertThat(prop.type()).isEqualTo(ConnectionPropertyType.BOOLEAN);
                });
    }

    @Test
    void verifyPropertiesOfJaybirdNative() {
        var connectionPropertyRegistry = ConnectionPropertyRegistry.getInstance();

        // Tests a property registered by NativeConnectionPropertyDefiner in jaybird-native
        assertThat(connectionPropertyRegistry.getRegisteredNames())
                .contains("nativeLibraryPath");
    }

    @Test
    void getByName_returnsKnownProperty() {
        Map<String, ConnectionProperty> props = new HashMap<>();
        ConnectionProperty connectionProperty = connectionProperty();
        props.put(PROP_NAME, connectionProperty);
        ConnectionPropertyRegistry connectionPropertyRegistry = new ConnectionPropertyRegistry(props);

        assertThat(connectionPropertyRegistry.getByName(PROP_NAME))
                .matches(connectionProperty::isIdenticalTo);
    }

    @Test
    void getByName_returnsNullForUnknownProperty() {
        ConnectionPropertyRegistry connectionPropertyRegistry = new ConnectionPropertyRegistry(emptyMap());

        assertThat(connectionPropertyRegistry.getByName("unknownValue"))
                .isNull();
    }

    @Test
    void builderPropertyRegistration(@Mock ConnectionPropertyDefinerSpi definer) {
        ConnectionPropertiesBuilder builder = new ConnectionPropertiesBuilder();

        ConnectionProperty connectionProperty = connectionProperty();
        builder.tryRegisterProperty(connectionProperty, definer);

        ConnectionPropertyRegistry connectionPropertyRegistry = builder.build();
        assertThat(connectionPropertyRegistry.getRegisteredProperties())
                .hasSize(1)
                .first().matches(connectionProperty::isIdenticalTo);
    }

    @Test
    void builderPropertyRegistration_exactDuplicate(@Mock ConnectionPropertyDefinerSpi definer) {
        ConnectionProperty connectionProperty = connectionProperty();
        ConnectionProperty connectionPropertyCopy = connectionProperty();
        ConnectionPropertiesBuilder builder = new ConnectionPropertiesBuilder();
        builder.tryRegisterProperty(connectionProperty, definer);

        builder.tryRegisterProperty(connectionPropertyCopy, definer);

        ConnectionPropertyRegistry connectionPropertyRegistry = builder.build();
        // Although the actual object *is* connectionProperty, we only care that it is identicalTo
        assertThat(connectionPropertyRegistry.getRegisteredProperties())
                .hasSize(1)
                .first().matches(connectionProperty::isIdenticalTo);
    }

    @Test
    void builderPropertyRegistrationFailure_sameName_name(@Mock ConnectionPropertyDefinerSpi definer) {
        ConnectionProperty connectionPropertySameName =
                ConnectionProperty.builder().name(PROP_NAME).aliases("xyz").build();
        ConnectionPropertiesBuilder builder = new ConnectionPropertiesBuilder();
        ConnectionProperty connectionProperty = connectionProperty();
        builder.tryRegisterProperty(connectionProperty, definer);

        builder.tryRegisterProperty(connectionPropertySameName, definer);

        verify(definer).notRegistered(same(connectionPropertySameName));
        ConnectionPropertyRegistry connectionPropertyRegistry = builder.build();
        // Although the actual object *is* connectionProperty, we only care that it is identicalTo
        assertThat(connectionPropertyRegistry.getRegisteredProperties())
                .hasSize(1)
                .first().matches(connectionProperty::isIdenticalTo);
    }

    @Test
    void builderPropertyRegistrationFailure_sameName_alias(@Mock ConnectionPropertyDefinerSpi definer) {
        ConnectionProperty connectionPropertyAliasMatchesName =
                ConnectionProperty.builder().name("xyz").aliases(PROP_NAME).build();
        ConnectionPropertiesBuilder builder = new ConnectionPropertiesBuilder();
        ConnectionProperty connectionProperty = connectionProperty();
        builder.tryRegisterProperty(connectionProperty, definer);

        builder.tryRegisterProperty(connectionPropertyAliasMatchesName, definer);

        verify(definer).notRegistered(same(connectionPropertyAliasMatchesName));
        ConnectionPropertyRegistry connectionPropertyRegistry = builder.build();
        // Although the actual object *is* connectionProperty, we only care that it is identicalTo
        assertThat(connectionPropertyRegistry.getRegisteredProperties())
                .hasSize(1)
                .first().matches(connectionProperty::isIdenticalTo);
    }

    @Test
    void builderPropertyRegistrationFailure_sameDpbItem(@Mock ConnectionPropertyDefinerSpi definer) {
        ConnectionProperty connectionPropertySameDpb =
                ConnectionProperty.builder().name("userTest").dpbItem(isc_dpb_user_name).build();
        ConnectionPropertiesBuilder builder = new ConnectionPropertiesBuilder();
        ConnectionProperty connectionProperty = connectionProperty();
        builder.tryRegisterProperty(connectionProperty, definer);

        builder.tryRegisterProperty(connectionPropertySameDpb, definer);

        verify(definer).notRegistered(same(connectionPropertySameDpb));
        ConnectionPropertyRegistry connectionPropertyRegistry = builder.build();
        // Although the actual object *is* connectionProperty, we only care that it is identicalTo
        assertThat(connectionPropertyRegistry.getRegisteredProperties())
                .hasSize(1)
                .first().matches(connectionProperty::isIdenticalTo);
    }

    @Test
    void builderPropertyRegistrationFailure_sameSpbItem(@Mock ConnectionPropertyDefinerSpi definer) {
        ConnectionProperty connectionPropertySameSpb =
                ConnectionProperty.builder().name("userTest").spbItem(isc_spb_user_name).build();
        ConnectionPropertiesBuilder builder = new ConnectionPropertiesBuilder();
        ConnectionProperty connectionProperty = connectionProperty();
        builder.tryRegisterProperty(connectionProperty, definer);

        builder.tryRegisterProperty(connectionPropertySameSpb, definer);

        verify(definer).notRegistered(same(connectionPropertySameSpb));
        ConnectionPropertyRegistry connectionPropertyRegistry = builder.build();
        // Although the actual object *is* connectionProperty, we only care that it is identicalTo
        assertThat(connectionPropertyRegistry.getRegisteredProperties())
                .hasSize(1)
                .first().matches(connectionProperty::isIdenticalTo);
    }

    @Test
    void builderIgnoresExceptionFrom_definer_notRegistered_duplicateName(@Mock ConnectionPropertyDefinerSpi definer) {
        ConnectionProperty connectionPropertyDuplicateName = ConnectionProperty.unknown(PROP_NAME);
        ConnectionPropertiesBuilder builder = new ConnectionPropertiesBuilder();
        ConnectionProperty connectionProperty = connectionProperty();
        builder.tryRegisterProperty(connectionProperty, definer);
        doThrow(RuntimeException.class).when(definer).notRegistered(connectionProperty);

        assertThatCode(() -> builder.tryRegisterProperty(connectionPropertyDuplicateName, definer))
                .doesNotThrowAnyException();
    }

    @Test
    void builderIgnoresExceptionFrom_definer_notRegistered_duplicateDpb(@Mock ConnectionPropertyDefinerSpi definer) {
        ConnectionProperty connectionPropertySameDpb =
                ConnectionProperty.builder().name("userTest").dpbItem(isc_dpb_user_name).build();
        ConnectionPropertiesBuilder builder = new ConnectionPropertiesBuilder();
        builder.tryRegisterProperty(connectionProperty(), definer);
        doThrow(RuntimeException.class).when(definer).notRegistered(connectionPropertySameDpb);

        assertThatCode(() -> builder.tryRegisterProperty(connectionPropertySameDpb, definer))
                .doesNotThrowAnyException();
    }

    @Test
    void builderIgnoresExceptionFrom_definer_notRegistered_duplicateSpb(@Mock ConnectionPropertyDefinerSpi definer) {
        ConnectionProperty connectionPropertySameSpb =
                ConnectionProperty.builder().name("userTest").spbItem(isc_spb_user_name).build();
        ConnectionPropertiesBuilder builder = new ConnectionPropertiesBuilder();
        builder.tryRegisterProperty(connectionProperty(), definer);
        doThrow(RuntimeException.class).when(definer).notRegistered(connectionPropertySameSpb);

        assertThatCode(() -> builder.tryRegisterProperty(connectionPropertySameSpb, definer))
                .doesNotThrowAnyException();
    }

}