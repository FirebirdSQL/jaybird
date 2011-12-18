/*
 * Firebird Open Source J2EE Connector - JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import java.lang.reflect.*;

import org.firebirdsql.gds.ClassFactory;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.FBObjectListener.BlobListener;
import org.firebirdsql.jdbc.FBObjectListener.StatementListener;

/**
 * Creates statement objects.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
 */
public class FBStatementFactory {

    /**
     * @return a new instance of FBCallableStatement.
     */
    public static AbstractCallableStatement createCallableStatement(GDSHelper gdsHelper,
            String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability,
            StoredProcedureMetaData storedProcMetaData, StatementListener statementListener, BlobListener blobListener) throws FBSQLException {

        try {

            Constructor constructor = ClassFactory.get(ClassFactory.FBCallableStatement)
                    .getConstructor(
                            new Class[] { GDSHelper.class, String.class, int.class, int.class,
                                    int.class, StoredProcedureMetaData.class, StatementListener.class, BlobListener.class });

            return (AbstractCallableStatement) constructor.newInstance(new Object[] { gdsHelper,
                    sql, new Integer(resultSetType), new Integer(resultSetConcurrency),
                    new Integer(resultSetHoldability), storedProcMetaData, statementListener, blobListener });

        } catch(InvocationTargetException ex) {
            
            Throwable t = ex.getTargetException();
            if (t instanceof FBSQLException)
                throw (FBSQLException)t;
            else
                throw new UndeclaredThrowableException(t);
            
        } catch (IllegalArgumentException e) {
            throw new UndeclaredThrowableException(e);
        } catch (InstantiationException e) {
            throw new UndeclaredThrowableException(e);
        } catch (IllegalAccessException e) {
            throw new UndeclaredThrowableException(e);
        } catch (NoSuchMethodException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * @return a new instance of FBPreparedStatement
     */
    public static AbstractPreparedStatement createPreparedStatement(GDSHelper gdsHelper,
            String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability,
            StatementListener statementListener, BlobListener blobListener, boolean metadata, 
            boolean standaloneStatement, boolean generatedKeys)
            throws FBSQLException {

        try {
            
            Constructor constructor = ClassFactory.get(ClassFactory.FBPreparedStatement)
                    .getConstructor(
                            new Class[] { GDSHelper.class, String.class, int.class, int.class,
                                    int.class, StatementListener.class, BlobListener.class,
                                    boolean.class, boolean.class, boolean.class });

            return (AbstractPreparedStatement) constructor.newInstance(new Object[] { gdsHelper,
                    sql, new Integer(resultSetType), new Integer(resultSetConcurrency),
                    new Integer(resultSetHoldability), statementListener, blobListener,
                    new Boolean(metadata), new Boolean(standaloneStatement), 
                    new Boolean(generatedKeys)});

        } catch(InvocationTargetException ex) {
            
            Throwable t = ex.getTargetException();
            if (t instanceof FBSQLException)
                throw (FBSQLException)t;
            else
                throw new UndeclaredThrowableException(t);
            
        } catch (IllegalArgumentException e) {
            throw new UndeclaredThrowableException(e);
        } catch (InstantiationException e) {
            throw new UndeclaredThrowableException(e);
        } catch (IllegalAccessException e) {
            throw new UndeclaredThrowableException(e);
        } catch (NoSuchMethodException e) {
            throw new UndeclaredThrowableException(e);
        }

    }

    /**
     * @return a new instance FBStatement.
     */
    public static AbstractStatement createStatement(GDSHelper gdsHelper, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability, StatementListener statementListener)
            throws FBSQLException {

        try {

            Constructor constructor = ClassFactory.get(ClassFactory.FBStatement).getConstructor(
                    new Class[] { GDSHelper.class, int.class, int.class, int.class,
                            StatementListener.class });

            return (AbstractStatement) constructor.newInstance(new Object[] { gdsHelper,
                    new Integer(resultSetType), new Integer(resultSetConcurrency),
                    new Integer(resultSetHoldability), statementListener });

        } catch(InvocationTargetException ex) {
            
            Throwable t = ex.getTargetException();
            if (t instanceof FBSQLException)
                throw (FBSQLException)t;
            else
                throw new UndeclaredThrowableException(t);
            
        } catch (IllegalArgumentException e) {
            throw new UndeclaredThrowableException(e);
        } catch (InstantiationException e) {
            throw new UndeclaredThrowableException(e);
        } catch (IllegalAccessException e) {
            throw new UndeclaredThrowableException(e);
        } catch (NoSuchMethodException e) {
            throw new UndeclaredThrowableException(e);
        }

    }

}
