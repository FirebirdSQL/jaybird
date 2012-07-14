/*
 * $Id$
 * 
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
package org.firebirdsql.jdbc.parser;

/**
 * Interface for accessing the parser. This is intended to shield the rest of Jaybird from
 * problems when the antlr-runtime is not on the classpath.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public interface StatementParser {
    
    /**
     * Parses the provided SQL into a statement model
     * 
     * @param sql SQL query text to parse
     * @return Statementmodel
     * @throws StatementParser.ParseException For errors parsing the query
     */
    JaybirdStatementModel parseInsertStatement(String sql) throws ParseException;
    
    /**
     * Exception to wrap other exceptions when parsing.
     */
    public class ParseException extends Exception {
        
        private static final long serialVersionUID = 2440030356284907181L;

        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
