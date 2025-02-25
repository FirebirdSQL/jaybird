// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.function;

public interface ThrowingTriConsumer<T, U, V> {

    void accept(T t, U u, V v) throws Throwable;

}
