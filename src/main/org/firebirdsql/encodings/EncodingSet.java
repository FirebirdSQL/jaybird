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
package org.firebirdsql.encodings;

import java.util.List;

/**
 * {@code EncodingSet} is an interface for the purpose of loading {@link EncodingDefinition} mappings into Jaybird.
 * <p>
 * Jaybird uses the {@link java.util.ServiceLoader} mechanism to load the EncodingSet. An implementation must
 * provide a no-arg constructor, and it must be listed in {@code META-INF/services/org.firebirdsql.encodings.EncodingSet}
 * </p>
 * <p>
 * Providing an EncodingSet in general is not necessary, as Jaybird provides a default. However if Jaybird does not
 * include an encoding, or the current mapping from Firebird to Java (or reverse) doesn't match your requirements, then
 * you can use this mechanism to override it.
 * </p>
 * <p>
 * The encoding information is only loaded once, so the definition will need to be available in the classpath of the
 * class loader that initially loads and initializes Jaybird.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface EncodingSet {

    /**
     * Preference weight defines the loading order.
     * <p>
     * An {@code EncodingSet} with a higher number is processed before a lower number. The default preference weight
     * used by {@link DefaultEncodingSet} in Jaybird is {@code 0}. This means that if you want to override any default
     * definition, you need a higher number, if you just want to specify additional mappings, you need to specify a
     * lower number.
     * </p>
     *
     * @return Preference weight
     */
    int getPreferenceWeight();

    /**
     * @return List of {@link EncodingDefinition} implementations.
     */
    List<EncodingDefinition> getEncodings();
}
