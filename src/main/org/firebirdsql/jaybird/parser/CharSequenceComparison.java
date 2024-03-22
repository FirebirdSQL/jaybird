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
