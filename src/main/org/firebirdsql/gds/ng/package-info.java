/**
 * The Jaybird GDS-ng API provides the abstraction for connecting to Firebird either through a wire protocol
 * implementation, a native firebird client or other means. These classes are used by the JDBC implementation of
 * Jaybird.
 * </p>
 * The classes in this package (and its subpackages) can be used directly but the stability of the interface is
 * <strong>not guaranteed</strong> (not even between point releases). We strongly advise to use the JDBC implementation.
 * </p>
 * <p>
 * The classes and interfaces provided by this package can be used to add additional protocol implementations, but given
 * the expected volatility of the interface (see above) we urge you to use caution.
 * </p>
 * <p>
 * The <em>ng</em> stands for <em>Next Generation</em>, as this is the second incarnation, replacing the original GDS
 * API introduced in Jaybird 1.0 (removed in Jaybird 3.0).
 * </p>
 *
 * @since 3
 */
@InternalApi
package org.firebirdsql.gds.ng;

import org.firebirdsql.util.InternalApi;