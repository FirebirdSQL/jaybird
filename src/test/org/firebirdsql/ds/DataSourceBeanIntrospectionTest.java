// SPDX-FileCopyrightText: Copyright 2021 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.ds;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.firebirdsql.jaybird.props.PropertyNames.*;

/**
 * Test if properties of data sources with connection properties can be introspected.
 */
class DataSourceBeanIntrospectionTest {

    @ParameterizedTest
    @ValueSource(classes = { FBSimpleDataSource.class, FBConnectionPoolDataSource.class, FBXADataSource.class })
    void testIntrospection(Class<?> dataSourceClass) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(dataSourceClass);
        Set<String> propertyNames = Arrays.stream(beanInfo.getPropertyDescriptors())
                .map(PropertyDescriptor::getName)
                .collect(toSet());

        // Check a subset of properties
        assertThat(propertyNames).describedAs("DataSource property names")
                .contains(type, user, password, defaultIsolation, columnLabelForName, pageCacheSize);
    }
}
