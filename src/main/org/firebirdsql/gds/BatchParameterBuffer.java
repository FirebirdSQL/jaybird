package org.firebirdsql.gds;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.impl.wire.Xdrable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public interface BatchParameterBuffer extends ParameterBuffer {

    /**
     * @return The parameter buffer type identifier
     */
    int getType();

    /**
     * Add argument with no parameters.
     *
     * @param argumentType
     *         type of argument.
     */
    void addArgument(int argumentType);

    /**
     * Add string argument with the default encoding.
     *
     * @param argumentType
     *         type of argument.
     * @param value
     *         string value to add.
     */
    void addArgument(int argumentType, String value);

    /**
     * Add string argument.
     *
     * @param argumentType
     *         type of argument.
     * @param value
     *         string value to add.
     * @param encoding
     *         encoding to use for conversion to bytes
     */
    void addArgument(int argumentType, String value, Encoding encoding);

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
     * Add long argument.
     *
     * @param argumentType
     *         type of argument.
     * @param value
     *         long value to add.
     */
    void addArgument(int argumentType, long value);

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

    /**
     * Writes the arguments in the implementation specific serialization into the {@code OutputStream}.
     *
     * @param outputStream
     *         The {@code OutputStream} to write to
     * @throws IOException
     *         Errors produced by the output stream during writes
     */
    void writeArgumentsTo(OutputStream outputStream) throws IOException;

    /**
     * @return {@code Xdrable} to write (and optionally read) this instance as Xdr.
     */
    Xdrable toXdrable();

    /**
     * Converts this parameter buffer to a byte array.
     * <p>
     * This byte array includes the extra header-bytes (if any), but does not include the type information
     * </p>
     *
     * @return Byte array with serialization of this parameter buffer
     * @see #toBytesWithType()
     */
    byte[] toBytes();

    /**
     * Converts this parameter buffer to a byte array with type information.
     * <p>
     * This byte array includes the type information and the extra header bytes (if any).
     * </p>
     *
     * @return Byte array with serialization of this parameter buffer
     * @see #toBytes()
     */
    byte[] toBytesWithType();

    /**
     * @return the number of parameters stored.
     */
    int size();

}
