/*
 * Firebird Open Source JavaEE connector - jdbc driver, public Firebird-specific
 * JDBC extensions.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.jdbc;

import org.firebirdsql.util.Volatile;

/**
 * Type codes specific for Jaybird.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@Volatile(reason = "Defined type codes may receive a different value when standardized in JDBC")
public final class JaybirdTypeCodes {

    /**
     * Type code for {@code DECFLOAT}.
     * <p>
     * When using Java 26 or higher, use {@code java.sql.Types.DECFLOAT}. This constant might be deprecated and
     * removed once Jaybird only supports versions after Java 26.
     * </p>
     */
    @Volatile(reason = "Type code changed in 5.0.12/6.0.5/7.0.0 to match JDBC 4.5 value; prefer java.sql.Types.DECFLOAT when using Java 26 or higher")
    public static final int DECFLOAT = 2015;

    private JaybirdTypeCodes() {
        // no instances
    }
}
