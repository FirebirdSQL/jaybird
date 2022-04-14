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
