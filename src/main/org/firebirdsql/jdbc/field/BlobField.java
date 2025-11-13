// SPDX-FileCopyrightText: Copyright 2022-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import org.firebirdsql.jdbc.FBObjectListener;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.IntSupplier;

/**
 * Interface for field implementations that are backed by a blob or cached blob.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@NullMarked
public interface BlobField {

    /**
     * Sets the blob listener of the field.
     *
     * @param blobListener
     *         blob listener
     */
    void setBlobListener(FBObjectListener.BlobListener blobListener);

    /**
     * Configures the field to restrict the length of values returned by {@link FBField#getBytes()} to the maximum
     * length returned by {@code maxLengthSupplier}. This will also affect methods depending on {@code getBytes}, like
     * string, numeric, Boolean and datetime getters, and {@code getObject} returning any of those types.
     * <p>
     * The {@link IntSupplier} {@code maxLengthSupplier} returns the maximum number of bytes, or {@code 0} for no limit;
     * negative values are treated as {@code 0}. The value returned may change dynamically.
     * </p>
     * <p>
     * For non-cached blobs/clobs, methods returning {@code Blob}, {@code Clob}, {@code InputStream} or {@code Reader}
     * are not affected (not truncated).
     * </p>
     * <p>
     * For cached blobs/clobs, the limit may also be applied to methods returning {@code Blob}, {@code Clob},
     * {@code InputStream} or {@code Reader}, and the cached value may be permanently truncated. That is, if the value
     * returned by {@code maxLengthSupplier} changes to a larger value, or the length supplier is replaced by one that
     * returns a larger value, the value may remain truncated to the initial smaller length.
     * </p>
     *
     * @param maxLengthSupplier
     *         supplier that returns the maximum number of bytes; a value of {@code null} will revert to the default
     *         (unrestricted)
     * @see java.sql.Statement#setMaxFieldSize(int)
     * @since 7
     */
    void restrictLength(@Nullable IntSupplier maxLengthSupplier);

}
