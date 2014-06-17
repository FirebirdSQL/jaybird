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

import java.util.Iterator;

/**
 * Instance of this interface represents a Parameter Buffer it is extended
 * by various parameter buffer interfaces.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @see org.firebirdsql.gds.ParameterBuffer
 * @see org.firebirdsql.gds.ServiceRequestBuffer
 * @see org.firebirdsql.gds.TransactionParameterBuffer
 * @see org.firebirdsql.gds.ServiceParameterBuffer
 * @since 3.0
 */
public interface ParameterBuffer extends Iterable<Parameter> {
    /**
     * Add argument with no parameters.
     *
     * @param argumentType
     *         type of argument.
     */
    void addArgument(int argumentType);

    /**
     * Add string argument.
     *
     * @param argumentType
     *         type of argument.
     * @param value
     *         string value to add.
     */
    void addArgument(int argumentType, String value);

    /**
     * Add integer argument.
     *
     * @param argumentType
     *         type of argument.
     * @param value
     *         integer value to add.
     */
    void addArgument(int argumentType, int value);

    /**
     * Add array of bytes.
     *
     * @param argumentType
     *         type of argument.
     * @param content
     *         content of argument.
     */
    void addArgument(int argumentType, byte[] content);

    /**
     * Remove specified argument.
     *
     * @param argumentType
     *         type of argument to remove.
     */
    void removeArgument(int argumentType);

    /**
     * Get argument as string.
     *
     * @param argumentType
     *         type of argument to find.
     * @return argument as string or <code>null</code> if nothing found.
     */
    String getArgumentAsString(int argumentType);

    /**
     * Get argument as int.
     *
     * @param argumentType
     *         type of argument to find.
     * @return argument as string or <code>0</code> if nothing found.
     */
    int getArgumentAsInt(int argumentType);

    /**
     * Check if this parameter buffer has specified argument.
     *
     * @param argumentType
     *         type of argument to find.
     * @return <code>true</code> if this buffer contains specified argument.
     */
    boolean hasArgument(int argumentType);

    /**
     * Returns an iterator over a copy of the parameters in this parameter buffer.
     * <p>
     * It is safe to iterate over this iterator while modifying the parameter buffer. Changes will not be reflected in
     * the iterator.
     * </p>
     *
     * @return Iterator over the parameters in this parameter buffer.
     */
    @Override
    Iterator<Parameter> iterator();
}
