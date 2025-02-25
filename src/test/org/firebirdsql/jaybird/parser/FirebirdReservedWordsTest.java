// SPDX-FileCopyrightText: Copyright 2021-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FirebirdReservedWordsTest {

    @Test
    void latest_returnFIREBIRD_5_0() {
        assertThat(FirebirdReservedWords.latest()).isEqualTo(FirebirdReservedWords.FIREBIRD_5_0);
    }

}