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

// Generated from D:/Development/project/Jaybird/jaybird/src/main/org/firebirdsql/jdbc/parser\JaybirdSql.g4 by ANTLR 4.7
package org.firebirdsql.jdbc.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JaybirdSqlParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, AS=3, DELETE=4, FROM=5, INSERT=6, INTO=7, MERGE=8, OR=9, 
		RETURNING=10, SET=11, UPDATE=12, LEFT_PAREN=13, RIGHT_PAREN=14, COMMA=15, 
		STRING=16, BINARY_STRING=17, Q_STRING=18, GENERIC_ID=19, QUOTED_ID=20, 
		SL_COMMENT=21, COMMENT=22, WS=23, OTHER=24;
	public static final int
		RULE_statement = 0, RULE_deleteStatement = 1, RULE_updateStatement = 2, 
		RULE_updateOrInsertStatement = 3, RULE_insertStatement = 4, RULE_mergeStatement = 5, 
		RULE_returningClause = 6, RULE_simpleIdentifier = 7, RULE_fullIdentifier = 8, 
		RULE_tableName = 9, RULE_returningColumnList = 10, RULE_columnName = 11, 
		RULE_columnAlias = 12, RULE_simpleValue = 13;
	public static final String[] ruleNames = {
		"statement", "deleteStatement", "updateStatement", "updateOrInsertStatement", 
		"insertStatement", "mergeStatement", "returningClause", "simpleIdentifier", 
		"fullIdentifier", "tableName", "returningColumnList", "columnName", "columnAlias", 
		"simpleValue"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "';'", "'.'", null, null, null, null, null, null, null, null, null, 
		null, "'('", "')'", "','"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, "AS", "DELETE", "FROM", "INSERT", "INTO", "MERGE", "OR", 
		"RETURNING", "SET", "UPDATE", "LEFT_PAREN", "RIGHT_PAREN", "COMMA", "STRING", 
		"BINARY_STRING", "Q_STRING", "GENERIC_ID", "QUOTED_ID", "SL_COMMENT", 
		"COMMENT", "WS", "OTHER"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "JaybirdSql.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }


	protected JaybirdStatementModel statementModel = new JaybirdStatementModel();

	public JaybirdStatementModel getStatementModel() {
	    return statementModel;
	}

	public JaybirdSqlParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class StatementContext extends ParserRuleContext {
		public InsertStatementContext insertStatement() {
			return getRuleContext(InsertStatementContext.class,0);
		}
		public DeleteStatementContext deleteStatement() {
			return getRuleContext(DeleteStatementContext.class,0);
		}
		public UpdateStatementContext updateStatement() {
			return getRuleContext(UpdateStatementContext.class,0);
		}
		public UpdateOrInsertStatementContext updateOrInsertStatement() {
			return getRuleContext(UpdateOrInsertStatementContext.class,0);
		}
		public MergeStatementContext mergeStatement() {
			return getRuleContext(MergeStatementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitStatement(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_statement);
		try {
			setState(33);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(28);
				insertStatement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(29);
				deleteStatement();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(30);
				updateStatement();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(31);
				updateOrInsertStatement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(32);
				mergeStatement();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DeleteStatementContext extends ParserRuleContext {
		public TerminalNode DELETE() { return getToken(JaybirdSqlParser.DELETE, 0); }
		public TerminalNode FROM() { return getToken(JaybirdSqlParser.FROM, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public ReturningClauseContext returningClause() {
			return getRuleContext(ReturningClauseContext.class,0);
		}
		public DeleteStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_deleteStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterDeleteStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitDeleteStatement(this);
		}
	}

	public final DeleteStatementContext deleteStatement() throws RecognitionException {
		DeleteStatementContext _localctx = new DeleteStatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_deleteStatement);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(35);
			match(DELETE);
			setState(36);
			match(FROM);
			setState(37);
			tableName();
			setState(41);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					{
					setState(38);
					matchWildcard();
					}
					} 
				}
				setState(43);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			}
			setState(45);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNING) {
				{
				setState(44);
				returningClause();
				}
			}

			setState(48);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(47);
				match(T__0);
				}
			}


			                statementModel.setStatementType(JaybirdStatementModel.DELETE_TYPE);
			            
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UpdateStatementContext extends ParserRuleContext {
		public TerminalNode UPDATE() { return getToken(JaybirdSqlParser.UPDATE, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public TerminalNode SET() { return getToken(JaybirdSqlParser.SET, 0); }
		public ReturningClauseContext returningClause() {
			return getRuleContext(ReturningClauseContext.class,0);
		}
		public UpdateStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_updateStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterUpdateStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitUpdateStatement(this);
		}
	}

	public final UpdateStatementContext updateStatement() throws RecognitionException {
		UpdateStatementContext _localctx = new UpdateStatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_updateStatement);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(52);
			match(UPDATE);
			setState(53);
			tableName();
			setState(54);
			match(SET);
			setState(58);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					{
					setState(55);
					matchWildcard();
					}
					} 
				}
				setState(60);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			}
			setState(62);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNING) {
				{
				setState(61);
				returningClause();
				}
			}

			setState(65);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(64);
				match(T__0);
				}
			}


			                statementModel.setStatementType(JaybirdStatementModel.UPDATE_TYPE);
			            
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UpdateOrInsertStatementContext extends ParserRuleContext {
		public TerminalNode UPDATE() { return getToken(JaybirdSqlParser.UPDATE, 0); }
		public TerminalNode OR() { return getToken(JaybirdSqlParser.OR, 0); }
		public TerminalNode INSERT() { return getToken(JaybirdSqlParser.INSERT, 0); }
		public TerminalNode INTO() { return getToken(JaybirdSqlParser.INTO, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public ReturningClauseContext returningClause() {
			return getRuleContext(ReturningClauseContext.class,0);
		}
		public UpdateOrInsertStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_updateOrInsertStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterUpdateOrInsertStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitUpdateOrInsertStatement(this);
		}
	}

	public final UpdateOrInsertStatementContext updateOrInsertStatement() throws RecognitionException {
		UpdateOrInsertStatementContext _localctx = new UpdateOrInsertStatementContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_updateOrInsertStatement);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			match(UPDATE);
			setState(70);
			match(OR);
			setState(71);
			match(INSERT);
			setState(72);
			match(INTO);
			setState(73);
			tableName();
			setState(77);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					{
					setState(74);
					matchWildcard();
					}
					} 
				}
				setState(79);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			}
			setState(81);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNING) {
				{
				setState(80);
				returningClause();
				}
			}

			setState(84);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(83);
				match(T__0);
				}
			}


			                statementModel.setStatementType(JaybirdStatementModel.UPDATE_OR_INSERT_TYPE);
			            
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InsertStatementContext extends ParserRuleContext {
		public TerminalNode INSERT() { return getToken(JaybirdSqlParser.INSERT, 0); }
		public TerminalNode INTO() { return getToken(JaybirdSqlParser.INTO, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public ReturningClauseContext returningClause() {
			return getRuleContext(ReturningClauseContext.class,0);
		}
		public InsertStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insertStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterInsertStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitInsertStatement(this);
		}
	}

	public final InsertStatementContext insertStatement() throws RecognitionException {
		InsertStatementContext _localctx = new InsertStatementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_insertStatement);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			match(INSERT);
			setState(89);
			match(INTO);
			setState(90);
			tableName();
			setState(94);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					{
					setState(91);
					matchWildcard();
					}
					} 
				}
				setState(96);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			}
			setState(98);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNING) {
				{
				setState(97);
				returningClause();
				}
			}

			setState(101);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(100);
				match(T__0);
				}
			}


			                statementModel.setStatementType(JaybirdStatementModel.INSERT_TYPE);
			            
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MergeStatementContext extends ParserRuleContext {
		public TerminalNode MERGE() { return getToken(JaybirdSqlParser.MERGE, 0); }
		public TerminalNode INTO() { return getToken(JaybirdSqlParser.INTO, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public ReturningClauseContext returningClause() {
			return getRuleContext(ReturningClauseContext.class,0);
		}
		public MergeStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mergeStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterMergeStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitMergeStatement(this);
		}
	}

	public final MergeStatementContext mergeStatement() throws RecognitionException {
		MergeStatementContext _localctx = new MergeStatementContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_mergeStatement);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			match(MERGE);
			setState(106);
			match(INTO);
			setState(107);
			tableName();
			setState(111);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					{
					setState(108);
					matchWildcard();
					}
					} 
				}
				setState(113);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			}
			setState(115);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNING) {
				{
				setState(114);
				returningClause();
				}
			}

			setState(118);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(117);
				match(T__0);
				}
			}


			                statementModel.setStatementType(JaybirdStatementModel.MERGE_TYPE);
			            
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReturningClauseContext extends ParserRuleContext {
		public TerminalNode RETURNING() { return getToken(JaybirdSqlParser.RETURNING, 0); }
		public ReturningColumnListContext returningColumnList() {
			return getRuleContext(ReturningColumnListContext.class,0);
		}
		public ReturningClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_returningClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterReturningClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitReturningClause(this);
		}
	}

	public final ReturningClauseContext returningClause() throws RecognitionException {
		ReturningClauseContext _localctx = new ReturningClauseContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_returningClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(122);
			match(RETURNING);
			setState(123);
			returningColumnList();

			               statementModel.setHasReturning();
			           
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SimpleIdentifierContext extends ParserRuleContext {
		public TerminalNode GENERIC_ID() { return getToken(JaybirdSqlParser.GENERIC_ID, 0); }
		public TerminalNode QUOTED_ID() { return getToken(JaybirdSqlParser.QUOTED_ID, 0); }
		public SimpleIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterSimpleIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitSimpleIdentifier(this);
		}
	}

	public final SimpleIdentifierContext simpleIdentifier() throws RecognitionException {
		SimpleIdentifierContext _localctx = new SimpleIdentifierContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_simpleIdentifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126);
			_la = _input.LA(1);
			if ( !(_la==GENERIC_ID || _la==QUOTED_ID) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FullIdentifierContext extends ParserRuleContext {
		public List<SimpleIdentifierContext> simpleIdentifier() {
			return getRuleContexts(SimpleIdentifierContext.class);
		}
		public SimpleIdentifierContext simpleIdentifier(int i) {
			return getRuleContext(SimpleIdentifierContext.class,i);
		}
		public FullIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fullIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterFullIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitFullIdentifier(this);
		}
	}

	public final FullIdentifierContext fullIdentifier() throws RecognitionException {
		FullIdentifierContext _localctx = new FullIdentifierContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_fullIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(128);
			simpleIdentifier();
			setState(129);
			match(T__1);
			setState(130);
			simpleIdentifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TableNameContext extends ParserRuleContext {
		public SimpleIdentifierContext t;
		public SimpleIdentifierContext simpleIdentifier() {
			return getRuleContext(SimpleIdentifierContext.class,0);
		}
		public TableNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterTableName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitTableName(this);
		}
	}

	public final TableNameContext tableName() throws RecognitionException {
		TableNameContext _localctx = new TableNameContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_tableName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(132);
			((TableNameContext)_localctx).t = simpleIdentifier();

			                statementModel.setTableName((((TableNameContext)_localctx).t!=null?_input.getText(((TableNameContext)_localctx).t.start,((TableNameContext)_localctx).t.stop):null));
			            
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReturningColumnListContext extends ParserRuleContext {
		public List<ColumnNameContext> columnName() {
			return getRuleContexts(ColumnNameContext.class);
		}
		public ColumnNameContext columnName(int i) {
			return getRuleContext(ColumnNameContext.class,i);
		}
		public List<ColumnAliasContext> columnAlias() {
			return getRuleContexts(ColumnAliasContext.class);
		}
		public ColumnAliasContext columnAlias(int i) {
			return getRuleContext(ColumnAliasContext.class,i);
		}
		public ReturningColumnListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_returningColumnList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterReturningColumnList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitReturningColumnList(this);
		}
	}

	public final ReturningColumnListContext returningColumnList() throws RecognitionException {
		ReturningColumnListContext _localctx = new ReturningColumnListContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_returningColumnList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(135);
			columnName();
			setState(137);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AS) | (1L << GENERIC_ID) | (1L << QUOTED_ID))) != 0)) {
				{
				setState(136);
				columnAlias();
				}
			}

			setState(146);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(139);
				match(COMMA);
				setState(140);
				columnName();
				setState(142);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AS) | (1L << GENERIC_ID) | (1L << QUOTED_ID))) != 0)) {
					{
					setState(141);
					columnAlias();
					}
				}

				}
				}
				setState(148);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColumnNameContext extends ParserRuleContext {
		public SimpleIdentifierContext simpleIdentifier() {
			return getRuleContext(SimpleIdentifierContext.class,0);
		}
		public FullIdentifierContext fullIdentifier() {
			return getRuleContext(FullIdentifierContext.class,0);
		}
		public ColumnNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterColumnName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitColumnName(this);
		}
	}

	public final ColumnNameContext columnName() throws RecognitionException {
		ColumnNameContext _localctx = new ColumnNameContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_columnName);
		try {
			setState(151);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(149);
				simpleIdentifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(150);
				fullIdentifier();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColumnAliasContext extends ParserRuleContext {
		public SimpleIdentifierContext simpleIdentifier() {
			return getRuleContext(SimpleIdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(JaybirdSqlParser.AS, 0); }
		public ColumnAliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnAlias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterColumnAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitColumnAlias(this);
		}
	}

	public final ColumnAliasContext columnAlias() throws RecognitionException {
		ColumnAliasContext _localctx = new ColumnAliasContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_columnAlias);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(153);
				match(AS);
				}
			}

			setState(156);
			simpleIdentifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SimpleValueContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(JaybirdSqlParser.STRING, 0); }
		public TerminalNode BINARY_STRING() { return getToken(JaybirdSqlParser.BINARY_STRING, 0); }
		public TerminalNode Q_STRING() { return getToken(JaybirdSqlParser.Q_STRING, 0); }
		public SimpleValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterSimpleValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitSimpleValue(this);
		}
	}

	public final SimpleValueContext simpleValue() throws RecognitionException {
		SimpleValueContext _localctx = new SimpleValueContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_simpleValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(158);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STRING) | (1L << BINARY_STRING) | (1L << Q_STRING))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\32\u00a3\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\3\2\3\2\3\2\3\2\3\2\5\2$\n\2"+
		"\3\3\3\3\3\3\3\3\7\3*\n\3\f\3\16\3-\13\3\3\3\5\3\60\n\3\3\3\5\3\63\n\3"+
		"\3\3\3\3\3\4\3\4\3\4\3\4\7\4;\n\4\f\4\16\4>\13\4\3\4\5\4A\n\4\3\4\5\4"+
		"D\n\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\7\5N\n\5\f\5\16\5Q\13\5\3\5\5\5"+
		"T\n\5\3\5\5\5W\n\5\3\5\3\5\3\6\3\6\3\6\3\6\7\6_\n\6\f\6\16\6b\13\6\3\6"+
		"\5\6e\n\6\3\6\5\6h\n\6\3\6\3\6\3\7\3\7\3\7\3\7\7\7p\n\7\f\7\16\7s\13\7"+
		"\3\7\5\7v\n\7\3\7\5\7y\n\7\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\3\n\3\n\3\n"+
		"\3\n\3\13\3\13\3\13\3\f\3\f\5\f\u008c\n\f\3\f\3\f\3\f\5\f\u0091\n\f\7"+
		"\f\u0093\n\f\f\f\16\f\u0096\13\f\3\r\3\r\5\r\u009a\n\r\3\16\5\16\u009d"+
		"\n\16\3\16\3\16\3\17\3\17\3\17\7+<O`q\2\20\2\4\6\b\n\f\16\20\22\24\26"+
		"\30\32\34\2\4\3\2\25\26\3\2\22\24\2\u00ac\2#\3\2\2\2\4%\3\2\2\2\6\66\3"+
		"\2\2\2\bG\3\2\2\2\nZ\3\2\2\2\fk\3\2\2\2\16|\3\2\2\2\20\u0080\3\2\2\2\22"+
		"\u0082\3\2\2\2\24\u0086\3\2\2\2\26\u0089\3\2\2\2\30\u0099\3\2\2\2\32\u009c"+
		"\3\2\2\2\34\u00a0\3\2\2\2\36$\5\n\6\2\37$\5\4\3\2 $\5\6\4\2!$\5\b\5\2"+
		"\"$\5\f\7\2#\36\3\2\2\2#\37\3\2\2\2# \3\2\2\2#!\3\2\2\2#\"\3\2\2\2$\3"+
		"\3\2\2\2%&\7\6\2\2&\'\7\7\2\2\'+\5\24\13\2(*\13\2\2\2)(\3\2\2\2*-\3\2"+
		"\2\2+,\3\2\2\2+)\3\2\2\2,/\3\2\2\2-+\3\2\2\2.\60\5\16\b\2/.\3\2\2\2/\60"+
		"\3\2\2\2\60\62\3\2\2\2\61\63\7\3\2\2\62\61\3\2\2\2\62\63\3\2\2\2\63\64"+
		"\3\2\2\2\64\65\b\3\1\2\65\5\3\2\2\2\66\67\7\16\2\2\678\5\24\13\28<\7\r"+
		"\2\29;\13\2\2\2:9\3\2\2\2;>\3\2\2\2<=\3\2\2\2<:\3\2\2\2=@\3\2\2\2><\3"+
		"\2\2\2?A\5\16\b\2@?\3\2\2\2@A\3\2\2\2AC\3\2\2\2BD\7\3\2\2CB\3\2\2\2CD"+
		"\3\2\2\2DE\3\2\2\2EF\b\4\1\2F\7\3\2\2\2GH\7\16\2\2HI\7\13\2\2IJ\7\b\2"+
		"\2JK\7\t\2\2KO\5\24\13\2LN\13\2\2\2ML\3\2\2\2NQ\3\2\2\2OP\3\2\2\2OM\3"+
		"\2\2\2PS\3\2\2\2QO\3\2\2\2RT\5\16\b\2SR\3\2\2\2ST\3\2\2\2TV\3\2\2\2UW"+
		"\7\3\2\2VU\3\2\2\2VW\3\2\2\2WX\3\2\2\2XY\b\5\1\2Y\t\3\2\2\2Z[\7\b\2\2"+
		"[\\\7\t\2\2\\`\5\24\13\2]_\13\2\2\2^]\3\2\2\2_b\3\2\2\2`a\3\2\2\2`^\3"+
		"\2\2\2ad\3\2\2\2b`\3\2\2\2ce\5\16\b\2dc\3\2\2\2de\3\2\2\2eg\3\2\2\2fh"+
		"\7\3\2\2gf\3\2\2\2gh\3\2\2\2hi\3\2\2\2ij\b\6\1\2j\13\3\2\2\2kl\7\n\2\2"+
		"lm\7\t\2\2mq\5\24\13\2np\13\2\2\2on\3\2\2\2ps\3\2\2\2qr\3\2\2\2qo\3\2"+
		"\2\2ru\3\2\2\2sq\3\2\2\2tv\5\16\b\2ut\3\2\2\2uv\3\2\2\2vx\3\2\2\2wy\7"+
		"\3\2\2xw\3\2\2\2xy\3\2\2\2yz\3\2\2\2z{\b\7\1\2{\r\3\2\2\2|}\7\f\2\2}~"+
		"\5\26\f\2~\177\b\b\1\2\177\17\3\2\2\2\u0080\u0081\t\2\2\2\u0081\21\3\2"+
		"\2\2\u0082\u0083\5\20\t\2\u0083\u0084\7\4\2\2\u0084\u0085\5\20\t\2\u0085"+
		"\23\3\2\2\2\u0086\u0087\5\20\t\2\u0087\u0088\b\13\1\2\u0088\25\3\2\2\2"+
		"\u0089\u008b\5\30\r\2\u008a\u008c\5\32\16\2\u008b\u008a\3\2\2\2\u008b"+
		"\u008c\3\2\2\2\u008c\u0094\3\2\2\2\u008d\u008e\7\21\2\2\u008e\u0090\5"+
		"\30\r\2\u008f\u0091\5\32\16\2\u0090\u008f\3\2\2\2\u0090\u0091\3\2\2\2"+
		"\u0091\u0093\3\2\2\2\u0092\u008d\3\2\2\2\u0093\u0096\3\2\2\2\u0094\u0092"+
		"\3\2\2\2\u0094\u0095\3\2\2\2\u0095\27\3\2\2\2\u0096\u0094\3\2\2\2\u0097"+
		"\u009a\5\20\t\2\u0098\u009a\5\22\n\2\u0099\u0097\3\2\2\2\u0099\u0098\3"+
		"\2\2\2\u009a\31\3\2\2\2\u009b\u009d\7\5\2\2\u009c\u009b\3\2\2\2\u009c"+
		"\u009d\3\2\2\2\u009d\u009e\3\2\2\2\u009e\u009f\5\20\t\2\u009f\33\3\2\2"+
		"\2\u00a0\u00a1\t\3\2\2\u00a1\35\3\2\2\2\27#+/\62<@COSV`dgqux\u008b\u0090"+
		"\u0094\u0099\u009c";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}