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
package org.firebirdsql.jaybird.props;

import org.firebirdsql.jaybird.props.def.ConnectionProperty;

import java.util.Map;

/**
 * Base of the properties hierarchy; provides common API for setting properties by name.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public interface BaseProperties {

    /**
     * Retrieves a string property value by name.
     * <p>
     * For properties with an explicit default, this method should return the string presentation of that default, not
     * {@code null}. For {@code int} or {@code boolean} the string equivalent is returned.
     * </p>
     *
     * @param name
     *         Property name (not {@code null} or empty)
     * @return Value of the property, or {@code null} when not set or not a known property
     */
    String getProperty(String name);

    /**
     * Retrieves a string property value by name, with a default if it's {@code null}.
     *
     * @param name
     *         Property name (not {@code null} or empty)
     * @return Value of the property, or {@code defaultIfNull} when not set or not a known property
     */
    default String getProperty(String name, String defaultIfNull) {
        String value = getProperty(name);
        return value != null ? value : defaultIfNull;
    }

    /**
     * Sets a property by name.
     * <p>
     * This method can be used to set all defined properties, but also properties not known by Jaybird. When setting
     * {@code int} or {@code boolean} properties, the appropriate conversions are applied. Using {@code null} will
     * reset to the default value. For {@code boolean} properties, an empty string is taken to mean {@code true}.
     * </p>
     *
     * @param name
     *         Property name (not {@code null} or empty)
     * @param value
     *         Property value (use {@code null} to apply default)
     * @throws IllegalArgumentException
     *         When the specified property is an {@code int} or {@code boolean} property and the value is not
     *         {@code null} and not a valid {@code int} or {@code boolean}
     */
    void setProperty(String name, String value);

    /**
     * Retrieves an {@code int} property value by name.
     * <p>
     * For properties with an explicit default, this method should return the integer presentation of that default. For
     * implementation simplicity, it is allowed to convert any string property to {@code int} instead of checking if
     * something is actually an {@code int} property
     * </p>
     *
     * @param name
     *         Property name (not {@code null} or empty)
     * @return Integer with value of the property, or {@code null} when not set
     * @throws NumberFormatException
     *         If the property is not an {@code int property} and the value cannot be converted to an integer
     * @throws IllegalArgumentException
     *         (optional) If the specified property is not an {@code int} property
     */
    Integer getIntProperty(String name);

    /**
     * Retrieves an {@code int} property value by name, with a default if it's {@code null}.
     *
     * @param name
     *         Property name (not {@code null} or empty)
     * @param defaultIfNull
     *         Default value when {@code null}
     * @return The value or {@code defaultIfNull} when the value is {@code null}
     * @see #getIntProperty(String)
     */
    default int getIntProperty(String name, int defaultIfNull) {
        Integer value = getIntProperty(name);
        return value != null ? value : defaultIfNull;
    }

    /**
     * Sets an {@code int} property by name.
     * <p>
     * For implementation simplicity, it is allowed to also set string properties. The value set will be the string
     * equivalent.
     * </p>
     *
     * @param name
     *         Property name (not {@code null} or empty)
     * @param value
     *         Property value (use {@code null} to apply default)
     * @throws IllegalArgumentException
     *         If the specified property is a {@code boolean} property
     */
    void setIntProperty(String name, Integer value);

    /**
     * Retrieves a {@code boolean} property value by name.
     * <p>
     * For properties with an explicit default, this method should return the boolean presentation of that default. For
     * implementation simplicity, it is allowed to convert any string property to {@code boolean} instead of checking
     * if something is actually a {@code boolean} property
     * </p>
     *
     * @param name
     *         Property name (not {@code null} or empty)
     * @return Integer with value of the property, or {@code null} when not set
     * @throws IllegalArgumentException
     *         If the property value is not {@code null} and cannot be converted to a boolean ({@code true} or empty
     *         string, {@code false}), (optional) if the specified property is not a {@code boolean} property
     */
    Boolean getBooleanProperty(String name);

    /**
     * Retrieves a {@code boolean} property value by name, with a default if it's {@code null}.
     *
     * @param name
     *         Property name (not {@code null} or empty)
     * @param defaultIfNull
     *         Default value when {@code null}
     * @return The value or {@code defaultIfNull} when the value is {@code null}
     * @see #getBooleanProperty(String)
     */
    default boolean getBooleanProperty(String name, boolean defaultIfNull) {
        Boolean value = getBooleanProperty(name);
        return value != null ? value : defaultIfNull;
    }

    /**
     * Sets a {@code boolean} property by name.
     * <p>
     * For implementation simplicity, it is allowed to also set string properties. The value set will be the string
     * equivalent.
     * </p>
     *
     * @param name
     *         Property name (not {@code null} or empty)
     * @param value
     *         Property value (use {@code null} to apply default)
     * @throws IllegalArgumentException
     *         If the specified property is an {@code int} property
     */
    void setBooleanProperty(String name, Boolean value);

    /**
     * An unmodifiable view on the connection properties held by this BaseProperties implementation.
     * <p>
     * Be aware, implementations can have additional properties that are not mapped from {@code ConnectionProperty}.
     * Such properties will need to be retrieved in an implementation-specific manner.
     * </p>
     *
     * @return An unmodifiable view on the property values held in this properties instance
     */
    Map<ConnectionProperty, Object> connectionPropertyValues();

}
