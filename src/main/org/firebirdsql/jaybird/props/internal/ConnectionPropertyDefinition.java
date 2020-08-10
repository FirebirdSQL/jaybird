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
import org.firebirdsql.jaybird.props.def.ConnectionPropertyApplicability;
import org.firebirdsql.jaybird.props.def.ConnectionPropertyType;
import org.firebirdsql.util.InternalApi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Properties;

import static org.firebirdsql.jaybird.props.def.ConnectionProperty.NO_DPB_ITEM;
import static org.firebirdsql.jaybird.props.def.ConnectionProperty.NO_SPB_ITEM;

/**
 * Annotation defining a connection property.
 * <p>
 * This annotation is for internal use in Jaybird only.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
@InternalApi
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConnectionPropertyDefinition {

    /**
     * Name of this property.
     * <p>
     * It is recommend to follow the Java naming convention for fields (that is: use camelCase).
     * </p>
     *
     * @return name of the property
     */
    String name();

    /**
     * Optional aliases for this property.
     * <p>
     * Aliases are alternative names for the property, either for backwards compatibility or for ease of use.
     * </p>
     *
     * @return aliases for this property, empty means no aliases
     */
    String[] aliases() default {};

    /**
     * Type of the property.
     *
     * @return type of the property
     */
    ConnectionPropertyType type() default ConnectionPropertyType.STRING;

    /**
     * Possible values for this property.
     * <p>
     * This is for documentation purposes only (eg for {@link java.sql.DriverPropertyInfo}).
     * </p>
     *
     * @return possible values for this property, or empty when unrestricted
     */
    String[] choices() default {};

    /**
     * Default value of this property.
     *
     * @return default value, empty string means no default
     */
    String defaultValue() default "";

    /**
     * Applicability of the property (where it can be used).
     * <p>
     * The applicability is informational, so Jaybird or other users can infer things about properties, for example if
     * it should be returned from {@link java.sql.Driver#getPropertyInfo(String, Properties)}. A property with
     * applicability SERVICE (or no applicability) can still be used on a database connection.
     * </p>
     *
     * @return Applicability of property, default value is {@link ConnectionPropertyApplicability#DATABASE}
     */
    ConnectionPropertyApplicability[] applicability() default { ConnectionPropertyApplicability.DATABASE };

    /**
     * Description of this property
     *
     * @return description of this property
     */
    String description() default "";

    /**
     * Database parameter buffer (DPB) item associated with this property.
     * <p>
     * Set to {@link ConnectionProperty#NO_DPB_ITEM} ({@code -1}, the default) to not
     * associate this with a DPB item
     * </p>
     *
     * @return database parameter buffer item
     */
    int dpbItem() default NO_DPB_ITEM;

    /**
     * Service parameter buffer (SPB) item associated with this property.
     * <p>
     * Set to {@link ConnectionProperty#NO_SPB_ITEM} ({@code -1}, the default) to not
     * associate this with an SPB item
     * </p>
     *
     * @return service parameter buffer item
     */
    int spbItem() default NO_SPB_ITEM;

}
