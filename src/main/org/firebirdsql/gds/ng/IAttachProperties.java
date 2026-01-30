// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-FileCopyrightText: Copyright 2015 Hakime Nakagami
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.firebirdsql.jaybird.props.AttachmentProperties;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Common properties for database and service attach.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@NullMarked
public interface IAttachProperties<T extends IAttachProperties<T>> extends AttachmentProperties {

    int DEFAULT_SOCKET_BUFFER_SIZE = PropertyConstants.BUFFER_SIZE_NOT_SET;
    int DEFAULT_SO_TIMEOUT = PropertyConstants.TIMEOUT_NOT_SET;
    int DEFAULT_CONNECT_TIMEOUT = PropertyConstants.TIMEOUT_NOT_SET;

    /**
     * @return The name of the object to attach to (either a database or service name).
     * @see #setAttachObjectName(String) 
     */
    default @Nullable String getAttachObjectName() {
        return getProperty(PropertyNames.attachObjectName);
    }

    /**
     * Sets the attach object name.
     * <p>
     * For more information, see
     * {@link org.firebirdsql.jaybird.props.DatabaseConnectionProperties#setDatabaseName(String)}
     * and {@link org.firebirdsql.jaybird.props.ServiceConnectionProperties#setServiceName(String)}.
     * </p>
     *
     * @param attachObjectName Database attach object name
     */
    default void setAttachObjectName(@Nullable String attachObjectName) {
        setProperty(PropertyNames.attachObjectName, attachObjectName);
    }

    /**
     * @return The value of {@link #getWireCrypt()} as an instance of {@link WireCrypt}.
     * @since 5
     * @see #getWireCrypt()
     */
    default WireCrypt getWireCryptAsEnum() {
        return WireCrypt.fromString(getWireCrypt());
    }

    /**
     * Set the wire encryption level.
     *
     * @param wireCrypt
     *         Wire encryption level ({@code null} not allowed)
     * @since 5
     * @see #setWireCrypt(String)
     */
    default void setWireCryptAsEnum(WireCrypt wireCrypt) {
        setWireCrypt(requireNonNull(wireCrypt, "wireCrypt").name());
    }

    /**
     * @return An immutable version of this instance as an implementation of {@link IAttachProperties}
     */
    T asImmutable();

    /**
     * @return A new, mutable, instance as an implementation of {@link IAttachProperties} with all properties copied.
     */
    T asNewMutable();

    /**
     * @return {@code true} if this is an immutable implementation, {@code false} if mutable
     * @since 5
     */
    boolean isImmutable();
}
