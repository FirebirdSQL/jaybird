/**
 * Defines the supported Firebird character sets and their mapping to Java character sets.
 * <p>
 * It is possible &mdash; but considered internal API &mdash; to define and override encodings by providing
 * an implementation of the {@link org.firebirdsql.encodings.EncodingSet} SPI. See that class for details.
 * </p>
 *
 * @since 3
 */
@InternalApi
package org.firebirdsql.encodings;

import org.firebirdsql.util.InternalApi;