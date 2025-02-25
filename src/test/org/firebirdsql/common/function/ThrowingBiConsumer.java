// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.function;

public interface ThrowingBiConsumer<T, U> {

    void accept(T t, U u) throws Throwable;

}
