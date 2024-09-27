/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.jdbc.escape;

import org.firebirdsql.jdbc.FBProcedureCall;
import org.firebirdsql.jdbc.FBProcedureParam;
import org.firebirdsql.util.InternalApi;

import java.sql.SQLException;

import static org.firebirdsql.jdbc.escape.FBEscapedCallParser.ParserState.*;

/**
 * Parser for escaped procedure call.
 * <p>
 * This class is not thread-safe.
 * </p>
 */
@InternalApi
public final class FBEscapedCallParser {

    private static final int INITIAL_CAPACITY = 32;

    private ParserState state = NORMAL_STATE;

    private boolean isNameProcessed;
    private boolean isExecuteWordProcessed;
    private boolean isProcedureWordProcessed;
    private boolean isCallWordProcessed;

    private int openBraceCount;

    private FBProcedureCall procedureCall = new FBProcedureCall();

    /**
     * Test the character to be the state switching character and switches the state if necessary.
     *
     * @param testChar
     *         character to test
     */
    void switchState(char testChar) {
        if (Character.isWhitespace(testChar) && state != LITERAL_STATE) {
            state = SPACE_STATE;
            return;
        }

        switch (testChar) {
        case '\'' -> {
            if (state == NORMAL_STATE) {
                state = LITERAL_STATE;
            } else if (state == LITERAL_STATE) {
                state = NORMAL_STATE;
            }
        }
        case ',' -> {
            if (state != LITERAL_STATE && state != PARENS_STATE) {
                state = COMMA_STATE;
            }
        }
        case '(', ')' -> {
            if (state != LITERAL_STATE) {
                state = PARENS_STATE;
            }
        }
        case '{', '}' -> {
            if (state != LITERAL_STATE) {
                state = CURLY_BRACE_STATE;
            }
        }
        default -> {
            if (state != LITERAL_STATE && state != PARENS_STATE) {
                state = NORMAL_STATE;
            }
        }
        }
    }

    /**
     * Clean the SQL statement. This method removes leading and trailing spaces and removes leading and trailing curly
     * braces if any.
     *
     * @param sql
     *         SQL statement to clean up
     * @return cleaned up statement
     * @throws FBSQLParseException
     *         if cleanup resulted in empty statement
     */
    private String cleanUpCall(String sql) throws FBSQLParseException {
        int startIndex = 0;
        int endIndex = sql.length();
        // Find first non-whitespace character
        while (startIndex < endIndex && Character.isWhitespace(sql.charAt(startIndex))) {
            startIndex++;
        }

        // Find last non-whitespace
        while (endIndex > startIndex && Character.isWhitespace(sql.charAt(endIndex - 1))) {
            endIndex--;
        }

        // Exclude open and close curly brace (we are assuming they match)
        if (startIndex < endIndex && sql.charAt(startIndex) == '{' && sql.charAt(endIndex - 1) == '}') {
            startIndex++;
            endIndex--;
        }

        // No string left
        if (startIndex >= endIndex) {
            throw new FBSQLParseException("Escaped call statement was empty.");
        }

        return sql.substring(startIndex, endIndex);
    }

    /**
     * Check if either "call" keyword or "EXECUTE PROCEDURE" keyword processed.
     *
     * @return {@code true} if either one or another keyword were processed.
     */
    private boolean isCallKeywordProcessed() {
        return isCallWordProcessed ||
                (isExecuteWordProcessed && isProcedureWordProcessed);
    }

    private void reset() {
        procedureCall = new FBProcedureCall();

        isExecuteWordProcessed = false;
        isProcedureWordProcessed = false;
        isCallWordProcessed = false;
        isNameProcessed = false;

        state = NORMAL_STATE;
    }

    /**
     * Converts escaped parts in {@code sql} to native representation.
     *
     * @param sql
     *         to parse
     * @return native form of the {@code sql}
     */
    @SuppressWarnings("java:S127")
    public FBProcedureCall parseCall(String sql) throws SQLException {
        sql = cleanUpCall(sql);

        reset();

        boolean isFirstOutParam = false;
        int paramCount = 0;
        int paramPosition = 0;

        final var buffer = new StringBuilder(INITIAL_CAPACITY);

        for (int i = 0, length = sql.length(); i < length; i++) {
            char currentChar = sql.charAt(i);
            switchState(currentChar);

            switch (state) {
            case NORMAL_STATE -> {
                // If we have an equals sign, most likely {? = call ...} syntax is used (there's hardly any place for
                // this symbol in procedure parameters).
                // To be sure, we check if no brace is open and if buffer contains only '?'.
                if (currentChar == '=' && openBraceCount <= 0 && !buffer.isEmpty() && buffer.charAt(0) == '?'
                        && !isFirstOutParam && !isNameProcessed) {
                    FBProcedureParam param = procedureCall.addParam(paramPosition, "?");
                    paramCount++;
                    param.setIndex(paramCount);
                    isFirstOutParam = true;
                    paramPosition++;
                    buffer.setLength(0);
                } else {
                    buffer.append(currentChar);
                }
            }
            case SPACE_STATE -> {
                if (buffer.isEmpty()) {
                    state = NORMAL_STATE;
                } else if (openBraceCount > 0 || isNameProcessed) {
                    buffer.append(currentChar);
                    state = NORMAL_STATE;
                } else {
                    // If procedure name was not yet processed, process the token.
                    // We look for the sequence EXECUTE PROCEDURE <name>.
                    boolean tokenProcessed = processToken(buffer.toString().trim());
                    if (tokenProcessed) {
                        buffer.setLength(0);
                        state = NORMAL_STATE;
                        if (isNameProcessed) {
                            // If we just found a name, fast-forward to the opening parenthesis, if there is one.
                            int j = i;
                            while (j < length - 1 && Character.isWhitespace(sql.charAt(j))) {
                                j++;
                            }
                            if (sql.charAt(j) == '(') {
                                i = j;
                            }
                        }
                    }
                }
            }
            case PARENS_STATE -> {
                // if we have an opening brace, and we already processed EXECUTE PROCEDURE words, but still do not have
                // procedure name set, we can be sure that buffer contains procedure name.
                boolean isProcedureName = currentChar == '(' && isCallKeywordProcessed() && !isNameProcessed;
                if (isProcedureName) {
                    if (buffer.isEmpty()) {
                        throw new FBSQLParseException("Procedure name is empty.");
                    }

                    procedureCall.setName(buffer.toString().trim());
                    isNameProcessed = true;
                    buffer.setLength(0);
                } else {
                    buffer.append(currentChar);
                    if (currentChar == '(') {
                        openBraceCount++;
                    } else {
                        openBraceCount--;
                    }
                }
                state = NORMAL_STATE;
            }
            case CURLY_BRACE_STATE -> {
                buffer.append(currentChar);
                state = NORMAL_STATE;
            }
            case COMMA_STATE -> {
                if (openBraceCount > 0) {
                    buffer.append(currentChar);
                    continue;
                }
                String param = processParam(buffer.toString());
                buffer.setLength(0);
                FBProcedureParam callParam = procedureCall.addParam(paramPosition, param);

                if (callParam.isParam()) {
                    paramCount++;
                    callParam.setIndex(paramCount);
                }

                paramPosition++;

                state = NORMAL_STATE;
            }
            case LITERAL_STATE -> buffer.append(currentChar);
            }
        }

        if (buffer.isEmpty()) {
            return procedureCall;
        }

        // remove spaces at the beginning and the end
        int startIndex = 0;
        int endIndex = buffer.length();
        while (startIndex < endIndex && Character.isSpaceChar(buffer.charAt(startIndex))) {
            startIndex++;
        }

        while (endIndex > startIndex && Character.isSpaceChar(buffer.charAt(endIndex - 1))) {
            endIndex--;
        }

        // If buffer starts with '(', remove it, we do not want this thing to bother us
        if (startIndex < endIndex && buffer.charAt(startIndex) == '(') {
            startIndex++;
        }

        // If buffer ends with ')', remove it,  it should match an opening brace right after the procedure name, and
        // we assume that all syntax checks were already done.
        if (startIndex < endIndex && buffer.charAt(endIndex - 1) == ')') {
            endIndex--;
        }

        final String value = startIndex < endIndex ? buffer.substring(startIndex, endIndex).trim() : "";

        if (value.isEmpty()) {
            return procedureCall;
        }

        // if there's something in the buffer, treat it as last param
        if (null == procedureCall.getName() && !isNameProcessed) {
            procedureCall.setName(value);
        } else {
            FBProcedureParam callParam = procedureCall.addParam(paramPosition, value);

            if (callParam.isParam()) {
                paramCount++;
                callParam.setIndex(paramCount);
            }
        }

        return procedureCall;
    }

    /**
     * Process token. This method detects procedure call keywords and sets appropriate flags. Also, it detects
     * procedure name and sets appropriate field in the procedure call object.
     *
     * @param token
     *         token to process.
     * @return {@code true} if token was understood and processed.
     */
    boolean processToken(String token) {
        if ("EXECUTE".equalsIgnoreCase(token) &&
                !isExecuteWordProcessed && !isProcedureWordProcessed && !isNameProcessed) {
            isExecuteWordProcessed = true;
            return true;
        }

        if ("PROCEDURE".equalsIgnoreCase(token) &&
                isExecuteWordProcessed && !isProcedureWordProcessed && !isNameProcessed) {
            isProcedureWordProcessed = true;
            return true;
        }

        if ("call".equalsIgnoreCase(token) && !isCallWordProcessed && !isNameProcessed) {
            isCallWordProcessed = true;
            return true;
        }

        if ((isCallWordProcessed || (isExecuteWordProcessed && isProcedureWordProcessed)) && !isNameProcessed) {
            procedureCall.setName(token);
            isNameProcessed = true;
            return true;
        }

        return false;
    }

    /**
     * Pre-process parameter. This method checks if there is escaped call inside and converts it to the native one.
     *
     * @param param
     *         parameter to process
     * @return processed parameter
     * @throws FBSQLParseException
     *         if parameter cannot be correctly parsed
     */
    String processParam(String param) throws SQLException {
        return FBEscapedParser.toNativeSql(param);
    }

    /**
     * State for the escaped call parser.
     *
     * @since 6
     */
    enum ParserState {
        NORMAL_STATE,
        LITERAL_STATE,
        PARENS_STATE,
        CURLY_BRACE_STATE,
        SPACE_STATE,
        COMMA_STATE,
    }

}
