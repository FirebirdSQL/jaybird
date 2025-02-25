// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.function;

@FunctionalInterface
public interface ThrowingBiFunction<T, U, R> {

    R apply(T t, U u) throws Exception;

}
