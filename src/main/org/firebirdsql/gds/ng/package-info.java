/**
 * Provides the abstraction for connecting to Firebird either through a wire protocol implementation, a native firebird
 * client or other means. These classes are used by the JDBC implementation of Jaybird.
 * <p>
 * The classes in this package (and its subpackages) can be used directly but the stability of the interface is
 * <strong>not guaranteed</strong> (not even between point releases). We strongly advise to use the JDBC implementation.
 * </p>
 * <p>
 * The classes and interfaces provided by this package can be used to add additional protocol implementations, but given
 * the expected volatility of the interface (see above) we urge you to use caution.
 * </p>
 *
 * @since 3.0
 */
package org.firebirdsql.gds.ng;