package org.firebirdsql.jdbc;

import java.lang.reflect.Constructor;

import org.firebirdsql.gds.ClassFactory;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.FBObjectListener.BlobListener;
import org.firebirdsql.jdbc.FBObjectListener.StatementListener;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

public class FBStatementFactory {

	private static Logger log = LoggerFactory.getLogger(
			FBStatementFactory.class, false);

	private static Constructor callableStatementConst = null;

	public static AbstractCallableStatement createCallableStatement(
			GDSHelper gdsHelper, String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability,
			StatementListener statementListener, BlobListener blobListener)
			throws FBSQLException {

		try {

			AbstractCallableStatement statement = null;

			if (callableStatementConst == null)
				callableStatementConst = ClassFactory.get(
						ClassFactory.FBCallableStatement).getConstructor(
						new Class[]{GDSHelper.class, String.class, int.class,
								int.class, int.class, StatementListener.class,
								BlobListener.class});

			statement = (AbstractCallableStatement) callableStatementConst
					.newInstance(new Object[]{gdsHelper, sql,
							new Integer(resultSetType),
							new Integer(resultSetConcurrency),
							new Integer(resultSetHoldability),
							statementListener, blobListener});

			return statement;

		} catch (Exception e) {

			throw new FBSQLException(e);

		}
	}

	private static Constructor preparedStatementConst = null;

	public static AbstractPreparedStatement createPreparedStatement(
			GDSHelper gdsHelper, String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability,
			StatementListener statementListener, BlobListener blobListener,
			boolean metadata) throws FBSQLException {

		try {

			AbstractPreparedStatement statement = null;

			if (preparedStatementConst == null)
				preparedStatementConst = ClassFactory.get(
						ClassFactory.FBPreparedStatement).getConstructor(
						new Class[]{GDSHelper.class, String.class, int.class,
								int.class, int.class, StatementListener.class,
								BlobListener.class, boolean.class});

			statement = (AbstractPreparedStatement) preparedStatementConst
					.newInstance(new Object[]{gdsHelper, sql,
							new Integer(resultSetType),
							new Integer(resultSetConcurrency),
							new Integer(resultSetHoldability),
							statementListener, blobListener,
							new Boolean(metadata)});

			return statement;

		} catch (Exception e) {

			throw new FBSQLException(e);

		}

	}

	private static Constructor savepointIntConst = null;

	public static AbstractSavepoint createSavepoint(int counter)
			throws FBSQLException {

		try {

			AbstractSavepoint savepoint = null;

			if (savepointIntConst == null)
				savepointIntConst = ClassFactory.get(ClassFactory.FBSavepoint)
						.getConstructor(new Class[]{int.class});

			savepoint = (AbstractSavepoint) savepointIntConst
					.newInstance(new Object[]{new Integer(counter)});

			return savepoint;

		} catch (Exception e) {

			throw new FBSQLException(e);

		}

	}

	private static Constructor savepointStrConst = null;

	public static AbstractSavepoint createSavepoint(String name)
			throws FBSQLException {

		try {

			AbstractSavepoint savepoint = null;

			if (savepointStrConst == null)
				savepointStrConst = ClassFactory.get(ClassFactory.FBSavepoint)
						.getConstructor(new Class[]{String.class});

			savepoint = (AbstractSavepoint) savepointStrConst
					.newInstance(new Object[]{name});

			return savepoint;

		} catch (Exception e) {

			throw new FBSQLException(e);

		}

	}

	private static Constructor statementConst = null;

	public static AbstractStatement createStatement(GDSHelper gdsHelper,
			int resultSetType, int resultSetConcurrency,
			int resultSetHoldability, StatementListener statementListener)
			throws FBSQLException {

		try {

			AbstractStatement statement = null;

			if (statementConst == null)
				statementConst = ClassFactory.get(ClassFactory.FBStatement)
						.getConstructor(
								new Class[]{GDSHelper.class, int.class,
										int.class, int.class,
										StatementListener.class});

			statement = (AbstractStatement) statementConst
					.newInstance(new Object[]{gdsHelper,
							new Integer(resultSetType),
							new Integer(resultSetConcurrency),
							new Integer(resultSetHoldability),
							statementListener});

			return statement;

		} catch (Exception e) {

			throw new FBSQLException(e);

		}

	}

}
