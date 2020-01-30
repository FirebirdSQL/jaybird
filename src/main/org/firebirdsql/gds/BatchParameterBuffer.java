package org.firebirdsql.gds;

/**
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public interface BatchParameterBuffer extends ParameterBuffer {
    int VERSION1 = 1;
    int TAG_MULTIERROR = 1;
    int TAG_RECORD_COUNTS = 2;
    int TAG_BUFFER_BYTES_SIZE = 3;
    int TAG_BLOB_POLICY = 4;
    int TAG_DETAILED_ERRORS = 5;
    int BLOB_NONE = 0;
    int BLOB_ID_ENGINE = 1;
    int BLOB_ID_USER = 2;
    int BLOB_STREAM = 3;
    int BLOB_SEGHDR_ALIGN = 2;
}
