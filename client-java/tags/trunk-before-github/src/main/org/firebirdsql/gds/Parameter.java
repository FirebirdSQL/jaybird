/*
 * $Id$
 *
 * Public Firebird Java API.
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
package org.firebirdsql.gds;

import org.firebirdsql.encodings.Encoding;

/**
 * Interface for parameters stored in a {@link org.firebirdsql.gds.ParameterBuffer}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface Parameter {

    /**
     * The type identifier of the parameter (usually one of the constant values in
     * {@link org.firebirdsql.gds.ISCConstants}).
     *
     * @return The type of the parameter
     */
    int getType();

    /**
     * The value of the parameter as String.
     * <p>
     * The implementation may throw a RuntimeException if the parameter isn't a string (or shouldn't be used as a
     * string).
     * </p>
     *
     * @return The value as string
     */
    String getValueAsString();

    /**
     * The value of the parameter as int.
     * <p>
     * The implementation may throw a RuntimeException if the parameter isn't an int (or shouldn't be used as an
     * int).
     * </p>
     *
     * @return The value as int
     */
    int getValueAsInt();

    /**
     * Copies this argument into the supplied buffer, uses the supplied {@code Encoding} for string arguments.
     * <p>
     * An instance of <code>Parameter</code> should know how to copy itself into another buffer (eg an instance
     * of {@link org.firebirdsql.gds.impl.argument.StringArgument} would know to call
     * {@link org.firebirdsql.gds.ParameterBuffer#addArgument(int, String, Encoding)}).
     * </p>
     * <p>
     * The parameter does not need to check if it is the right type of destination buffer (if someone tries to
     * add a TPB argument to a DPB he is free to try that).
     * </p>
     *
     * @param buffer
     *         ParameterBuffer instance
     * @param stringEncoding
     *         Encoding to use for string properties. A value of {@code null} can be used to signal that the
     *         original encoding should be used.
     */
    void copyTo(ParameterBuffer buffer, Encoding stringEncoding);
}
