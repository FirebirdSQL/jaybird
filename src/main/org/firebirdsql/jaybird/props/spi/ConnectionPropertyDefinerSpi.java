// SPDX-FileCopyrightText: Copyright 2021 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jaybird.props.spi;

import org.firebirdsql.jaybird.props.def.ConnectionProperty;

import java.util.stream.Stream;

/**
 * Service provider interface to define connection properties in Jaybird.
 * <p>
 * These properties are loaded through {@link java.util.ServiceLoader}. The library or user code that wants to expose
 * additional connection properties to Jaybird should define the implementation class(es) of this interface in
 * {@code META-INF/services/org.firebirdsql.jaybird.props.spi.ConnectionPropertyDefinerSpi}. This should be
 * located in the same class path or class loader as Jaybird.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
public interface ConnectionPropertyDefinerSpi {

    /**
     * Defines the connection properties to add to Jaybird.
     * <p>
     * If the property name ({@link ConnectionProperty#name()}), one of the aliases
     * ({@link ConnectionProperty#aliases()}), the non-default {@link ConnectionProperty#dpbItem()} or
     * non-default {@link ConnectionProperty#spbItem()} is already defined, the property will be skipped
     * entirely. This will be logged, but will not produce an error, instead the method
     * {@link #notRegistered(ConnectionProperty)} will be called for that property. This does not apply to properties
     * where {@link ConnectionProperty#isIdenticalTo(ConnectionProperty)} returns true for the existing property.
     * </p>
     *
     * @return stream of properties to define
     */
    Stream<ConnectionProperty> defineProperties();

    /**
     * Callback method to inform the definer that a property has not been added to the collection of properties known
     * to Jaybird.
     *
     * @param connectionProperty
     *         connection property that was not registered
     * @see #defineProperties()
     */
    void notRegistered(ConnectionProperty connectionProperty);

}
