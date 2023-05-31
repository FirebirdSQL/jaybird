/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
