/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc.parser;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

/**
 * Concrete implementation for accessing the parser. This is intended to shield the rest of Jaybird from
 * problems when the antlr-runtime is not on the classpath.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class StatementParserImpl implements StatementParser {

    public JaybirdStatementModel parseInsertStatement(String sql) throws ParseException {
        try {
            CharStream stream = new CaseInsensitiveStream(sql);
            JaybirdSqlLexer lexer = new JaybirdSqlLexer(stream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            
            JaybirdSqlParser parser = new JaybirdSqlParser(tokenStream);
            parser.statement();

            JaybirdStatementModel statementModel = parser.getStatementModel();
            if (statementModel.getStatementType() == JaybirdStatementModel.UNDETECTED_TYPE) {
                throw new ParseException("Unable to detect statement type or unsupported statement type");
            }
            if (statementModel.getTableName() == null) {
                throw new ParseException("Unable to parse query: no table name found");
            }
            return statementModel;
        } catch (RecognitionException e) {
            throw new ParseException("Unable to parse query", e);
        }
    }
}
