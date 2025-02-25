// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.assertions;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Custom assertions that don't fall into a specific category.
 *
 * @author Mark Rotteveel
 */
public final class CustomAssertions {

    /**
     * Variant of {@link org.junit.jupiter.api.Assertions#assertThrows(Class, Executable)} involving
     * an {@link AutoCloseable}, ensuring it is closed if the exception is not thrown.
     */
    public static <T extends Throwable> T assertThrowsForAutoCloseable(Class<T> expectedType, ThrowingSupplier<AutoCloseable> throwingSupplier) {
        return assertThrows(expectedType, closeShield(throwingSupplier));
    }

    /**
     * Variant of {@link org.junit.jupiter.api.Assertions#assertThrows(Class, Executable, String)} involving
     * an {@link AutoCloseable}, ensuring it is closed if the exception is not thrown.
     */
    public static <T extends Throwable> T assertThrowsForAutoCloseable(Class<T> expectedType, ThrowingSupplier<AutoCloseable> throwingSupplier, String message) {
        return assertThrows(expectedType, closeShield(throwingSupplier), message);
    }

    /**
     * Variant of {@link org.junit.jupiter.api.Assertions#assertThrows(Class, Executable, Supplier)} involving
     * an {@link AutoCloseable}, ensuring it is closed if the exception is not thrown.
     */
    public static <T extends Throwable> T assertThrowsForAutoCloseable(Class<T> expectedType, ThrowingSupplier<AutoCloseable> throwingSupplier, Supplier<String> messageSupplier) {
        return assertThrows(expectedType, closeShield(throwingSupplier), messageSupplier);
    }

    @SuppressWarnings("EmptyTryBlock")
    public static Executable closeShield(ThrowingSupplier<? extends AutoCloseable> throwingSupplier) {
        return () -> {
            try (var ignored = throwingSupplier.get()) {
                // ignored
            }
        };
    }

}
