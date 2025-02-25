// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.function;

/**
 * Specialization of {@link AutoCloseable} which does not throw checked exceptions.
 *
 * @author Mark Rotteveel
 */
public interface UncheckedCloseable extends AutoCloseable {

    @Override
    void close();
}
