package org.firebirdsql.gds.ng;

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.BatchParameterBuffer;
import org.firebirdsql.gds.impl.BatchParameterBufferImpl;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Generic tests for FbBatch.
 * <p>
 * This abstract class is subclassed by the tests for specific FbBatch implementations.
 * </p>
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 5.0
 */
public abstract class AbstractBatchTest {
    //@formatter:off
    protected String CREATE_TABLE =
            "CREATE TABLE test_p_metadata (" +
            "  id INTEGER, " +
            "  simple_field VARCHAR(60) CHARACTER SET WIN1251 COLLATE PXW_CYRL, " +
            "  two_byte_field VARCHAR(60) CHARACTER SET BIG_5, " +
            "  three_byte_field VARCHAR(60) CHARACTER SET UNICODE_FSS, " +
            "  long_field BIGINT, " +
            "  int_field INTEGER, " +
            "  short_field SMALLINT, " +
            "  float_field FLOAT, " +
            "  double_field DOUBLE PRECISION, " +
            "  smallint_numeric NUMERIC(3,1), " +
            "  integer_decimal_1 DECIMAL(3,1), " +
            "  integer_numeric NUMERIC(5,2), " +
            "  integer_decimal_2 DECIMAL(9,3), " +
            "  bigint_numeric NUMERIC(10,4), " +
            "  bigint_decimal DECIMAL(18,9), " +
            "  date_field DATE, " +
            "  time_field TIME, " +
            "  timestamp_field TIMESTAMP, " +
            "  blob_field BLOB, " +
            "  blob_text_field BLOB SUB_TYPE TEXT, " +
            "  blob_minus_one BLOB SUB_TYPE -1 " +
            "  /* boolean */ " +
            "  /* decfloat */ " +
            "  /* extended numerics */ " +
            ")";

    protected String INSERT_QUERY = "INSERT INTO test_p_metadata (" +
            "  id, " +
            "  simple_field, " +
            "  two_byte_field, " +
            "  three_byte_field, " +
            "  long_field, " +
            "  int_field, " +
            "  short_field, " +
            "  float_field, " +
            "  double_field, " +
            "  smallint_numeric, " +
            "  integer_decimal_1, " +
            "  integer_numeric, " +
            "  integer_decimal_2, " +
            "  bigint_numeric, " +
            "  bigint_decimal, " +
            "  date_field, " +
            "  time_field, " +
            "  timestamp_field, " +
            "  blob_field, " +
            "  blob_text_field, " +
            "  blob_minus_one " +
            "  /* boolean */ " +
            "  /* decfloat */ " +
            "  /* extended numerics */ " +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?/* boolean-param *//* decfloat-param *//* extended numerics-param */)";

    protected String TEST_QUERY =
            "SELECT " +
            "simple_field, two_byte_field, three_byte_field, long_field, int_field, short_field," +
            "float_field, double_field, smallint_numeric, integer_decimal_1, integer_numeric," +
            "integer_decimal_2, bigint_numeric, bigint_decimal, date_field, time_field," +
            "timestamp_field, blob_field, blob_text_field, blob_minus_one " +
            "/* boolean */ " +
            "/* decfloat */ " +
            "/* extended numerics */ " +
            "FROM test_p_metadata";
    //@formatter:on

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase();

    protected FbDatabase db;
    protected Connection connection;
    protected FbTransaction transaction;
    protected PreparedStatement pstmt;
    protected ParameterMetaData parameterMetaData;
    protected FirebirdSupportInfo supportInfo;
    protected FbMetadataBuilder metadataBuilder;

    protected final FbConnectionProperties connectionInfo = FBTestProperties.getDefaultFbConnectionProperties();

    protected abstract Class<? extends FbDatabase> getExpectedDatabaseType();

    @BeforeEach
    void setUp() throws Exception {

        connection = getConnectionViaDriverManager();
        supportInfo = supportInfoFor(connection);

        if (!supportInfo.supportsBigint()) {
            // No BIGINT support, replacing type so number of columns remain the same
            CREATE_TABLE = CREATE_TABLE.replace("long_field BIGINT,", "long field DOUBLE PRECISION,");
        }
        if (supportInfo.supportsBoolean()) {
            CREATE_TABLE = CREATE_TABLE.replace("/* boolean */", ", boolean_field BOOLEAN");
            TEST_QUERY = TEST_QUERY.replace("/* boolean */", ", boolean_field").replace("/* boolean-param */", ", ?");
            INSERT_QUERY = INSERT_QUERY.replace("/* boolean */", ", boolean_field")
                    .replace("/* boolean-param */", ", ?");
        }
        if (supportInfo.supportsDecfloat()) {
            CREATE_TABLE = CREATE_TABLE.replace("/* decfloat */",
                    ", decfloat16_field DECFLOAT(16), decfloat34_field DECFLOAT(34)");
            TEST_QUERY = TEST_QUERY.replace("/* decfloat */", ", decfloat16_field, decfloat34_field")
                    .replace("/* decfloat-param */", ", ?, ?");
            INSERT_QUERY = INSERT_QUERY.replace("/* decfloat */", ", decfloat16_field, decfloat34_field")
                    .replace("/* decfloat-param */", ", ?, ?");
        }
        if (supportInfo.supportsDecimalPrecision(34)) {
            CREATE_TABLE = CREATE_TABLE.replace("/* extended numerics */",
                    ", col_numeric25_20 NUMERIC(25, 20), col_decimal30_5 DECIMAL(30,5)");
            TEST_QUERY = TEST_QUERY.replace("/* extended numerics */", ", col_numeric25_20, col_decimal30_5")
                    .replace("/* extended-num-param*/", ", ?, ?");
            INSERT_QUERY = INSERT_QUERY.replace("/* extended numerics */", ", col_numeric25_20, col_decimal30_5")
                    .replace("/* extended numerics-param */", ", ?, ?");
        }

        DdlHelper.executeCreateTable(connection, CREATE_TABLE);

        pstmt = connection.prepareStatement(TEST_QUERY);
        parameterMetaData = pstmt.getParameterMetaData();

        db = createDatabase();
        assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbDatabase implementation");

        db.attach();
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            transaction.commit();
            closeQuietly(pstmt, connection);
            closeQuietly(db);
        } finally {
            transaction = null;
            parameterMetaData = null;
            pstmt = null;
            connection = null;
            supportInfo = null;
        }
    }

    public static FBManager createFBManager() {
        return new FBManager(getGdsType());
    }

    public static FirebirdConnection getConnectionViaDriverManager() throws SQLException {
        return (FirebirdConnection) DriverManager.getConnection(getUrl(),
                getDefaultPropertiesForConnection());
    }

    protected abstract FbDatabase createDatabase() throws SQLException;

    @Test
    public void testCreateBatchWithoutMetadata() throws SQLException {
        allocateTransaction();
        BatchParameterBuffer buffer = new BatchParameterBufferImpl();
        buffer.addArgument(BatchParameterBuffer.TAG_RECORD_COUNTS, 1);
        FbBatch batch = db.createBatch(transaction, INSERT_QUERY, buffer);
    }

    @Test
    public void testCreateBatchWithMetadata() throws SQLException {
        allocateTransaction();

        metadataBuilder = db.getMetadataBuilder(26);
        metadataBuilder.addInteger(0);
        metadataBuilder.addVarchar(1, 60);
        metadataBuilder.addVarchar(2, 60);
        metadataBuilder.addVarchar(3, 60);
        metadataBuilder.addBigint(4);
        metadataBuilder.addInteger(5);
        metadataBuilder.addSmallint(6);
        metadataBuilder.addFloat(7);
        metadataBuilder.addDouble(8);
        metadataBuilder.addNumeric(9, 3, 1);
        metadataBuilder.addDecimal(10, 3, 1);
        metadataBuilder.addNumeric(11, 5, 2);
        metadataBuilder.addDecimal(12, 9, 3);
        metadataBuilder.addNumeric(13, 10, 4);
        metadataBuilder.addDecimal(14, 18, 9);
        metadataBuilder.addDate(15);
        metadataBuilder.addTime(16);
        metadataBuilder.addTimestamp(17);
        metadataBuilder.addBlob(18);
        metadataBuilder.addBlob(19, 1);
        metadataBuilder.addBlob(20, -1);
        final FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        if (supportInfo.supportsBoolean()) {
            metadataBuilder.addBoolean(21);
        }
        if (supportInfo.supportsDecfloat()) {
            metadataBuilder.addDecfloat16(22);
            metadataBuilder.addDecfloat34(23);
        }
        if (supportInfo.supportsDecimalPrecision(34)) {
            metadataBuilder.addDecNumeric(24, 25, 20);
            metadataBuilder.addDecDecimal(25, 30, 5);
        }

        BatchParameterBuffer buffer = new BatchParameterBufferImpl();
        buffer.addArgument(BatchParameterBuffer.TAG_RECORD_COUNTS, 1);
        FbBatch batch = db.createBatch(transaction, INSERT_QUERY, metadataBuilder.getMessageMetadata(), buffer);
    }

    private FbTransaction getTransaction() throws SQLException {
        return db.startTransaction(getDefaultTpb());
    }

    protected void allocateTransaction() throws SQLException {
        if (transaction == null || transaction.getState() != TransactionState.ACTIVE) {
            transaction = getTransaction();
        }
    }

}
