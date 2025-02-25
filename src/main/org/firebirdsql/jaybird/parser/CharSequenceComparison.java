// SPDX-FileCopyrightText: Copyright 2022-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparison utility methods for {@link CharSequence}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class CharSequenceComparison {

    private CharSequenceComparison() {
        // no instances
    }

    static Comparator<CharSequence> caseInsensitiveComparator() {
        return CaseInsensitiveComparator.INSTANCE;
    }

    static boolean equalsIgnoreCase(CharSequence s1, CharSequence s2) {
        return CaseInsensitiveComparator.INSTANCE.compare(s1, s2) == 0;
    }

    private static final class CaseInsensitiveComparator implements Comparator<CharSequence>, Serializable {

        private static final Comparator<CharSequence> INSTANCE = new CaseInsensitiveComparator();

        @Serial
        private static final long serialVersionUID = -3287107010439420474L;

        public int compare(CharSequence s1, CharSequence s2) {
            int n1 = s1.length();
            int n2 = s2.length();
            int min = Math.min(n1, n2);
            for (int i = 0; i < min; i++) {
                char c1 = s1.charAt(i);
                char c2 = s2.charAt(i);
                if (c1 != c2) {
                    c1 = Character.toUpperCase(c1);
                    c2 = Character.toUpperCase(c2);
                    if (c1 != c2) {
                        c1 = Character.toLowerCase(c1);
                        c2 = Character.toLowerCase(c2);
                        if (c1 != c2) {
                            return c1 - c2;
                        }
                    }
                }
            }
            return n1 - n2;
        }

        /** Replaces the de-serialized object. */
        @Serial
        private Object readResolve() { return INSTANCE; }

    }
}
