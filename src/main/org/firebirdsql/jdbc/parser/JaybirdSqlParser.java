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

// Generated from D:/Development/project/Jaybird/jaybird/src/main/org/firebirdsql/jdbc/parser\JaybirdSql.g4 by ANTLR 4.7.2
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
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, AS=4, DELETE=5, FROM=6, INSERT=7, INTO=8, MERGE=9, 
		OR=10, RETURNING=11, SET=12, UPDATE=13, LEFT_PAREN=14, RIGHT_PAREN=15, 
		COMMA=16, STRING=17, BINARY_STRING=18, Q_STRING=19, GENERIC_ID=20, QUOTED_ID=21, 
		SL_COMMENT=22, COMMENT=23, WS=24, OTHER=25;
	public static final int
		RULE_statement = 0, RULE_deleteStatement = 1, RULE_updateStatement = 2, 
		RULE_updateOrInsertStatement = 3, RULE_insertStatement = 4, RULE_mergeStatement = 5, 
		RULE_returningClause = 6, RULE_simpleIdentifier = 7, RULE_fullIdentifier = 8, 
		RULE_tableName = 9, RULE_returningColumnList = 10, RULE_columnName = 11, 
		RULE_alias = 12, RULE_simpleValue = 13;
	private static String[] makeRuleNames() {
		return new String[] {
			"statement", "deleteStatement", "updateStatement", "updateOrInsertStatement", 
			"insertStatement", "mergeStatement", "returningClause", "simpleIdentifier", 
			"fullIdentifier", "tableName", "returningColumnList", "columnName", "alias", 
			"simpleValue"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", "'.'", "'*'", null, null, null, null, null, null, null, 
			null, null, null, "'('", "')'", "','"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, "AS", "DELETE", "FROM", "INSERT", "INTO", "MERGE", 
			"OR", "RETURNING", "SET", "UPDATE", "LEFT_PAREN", "RIGHT_PAREN", "COMMA", 
			"STRING", "BINARY_STRING", "Q_STRING", "GENERIC_ID", "QUOTED_ID", "SL_COMMENT", 
			"COMMENT", "WS", "OTHER"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
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
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
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
			setState(55);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AS) | (1L << GENERIC_ID) | (1L << QUOTED_ID))) != 0)) {
				{
				setState(54);
				alias();
				}
			}

			setState(57);
			match(SET);
			setState(61);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					{
					setState(58);
					matchWildcard();
					}
					} 
				}
				setState(63);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			}
			setState(65);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNING) {
				{
				setState(64);
				returningClause();
				}
			}

			setState(68);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(67);
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
			setState(72);
			match(UPDATE);
			setState(73);
			match(OR);
			setState(74);
			match(INSERT);
			setState(75);
			match(INTO);
			setState(76);
			tableName();
			setState(80);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					{
					setState(77);
					matchWildcard();
					}
					} 
				}
				setState(82);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			}
			setState(84);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNING) {
				{
				setState(83);
				returningClause();
				}
			}

			setState(87);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(86);
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
			setState(91);
			match(INSERT);
			setState(92);
			match(INTO);
			setState(93);
			tableName();
			setState(97);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					{
					setState(94);
					matchWildcard();
					}
					} 
				}
				setState(99);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			}
			setState(101);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNING) {
				{
				setState(100);
				returningClause();
				}
			}

			setState(104);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(103);
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
			setState(108);
			match(MERGE);
			setState(109);
			match(INTO);
			setState(110);
			tableName();
			setState(114);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					{
					setState(111);
					matchWildcard();
					}
					} 
				}
				setState(116);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			}
			setState(118);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNING) {
				{
				setState(117);
				returningClause();
				}
			}

			setState(121);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(120);
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
		public SimpleIdentifierContext simpleIdentifier() {
			return getRuleContext(SimpleIdentifierContext.class,0);
		}
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
		int _la;
		try {
			setState(137);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(125);
				match(RETURNING);
				setState(129);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GENERIC_ID || _la==QUOTED_ID) {
					{
					setState(126);
					simpleIdentifier();
					setState(127);
					match(T__1);
					}
				}

				setState(131);
				match(T__2);

				               statementModel.setHasReturning();
				           
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(133);
				match(RETURNING);
				setState(134);
				returningColumnList();

				               statementModel.setHasReturning();
				           
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
			setState(139);
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
			setState(141);
			simpleIdentifier();
			setState(142);
			match(T__1);
			setState(143);
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
			setState(145);
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
		public List<AliasContext> alias() {
			return getRuleContexts(AliasContext.class);
		}
		public AliasContext alias(int i) {
			return getRuleContext(AliasContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JaybirdSqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JaybirdSqlParser.COMMA, i);
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
			setState(148);
			columnName();
			setState(150);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AS) | (1L << GENERIC_ID) | (1L << QUOTED_ID))) != 0)) {
				{
				setState(149);
				alias();
				}
			}

			setState(159);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(152);
				match(COMMA);
				setState(153);
				columnName();
				setState(155);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AS) | (1L << GENERIC_ID) | (1L << QUOTED_ID))) != 0)) {
					{
					setState(154);
					alias();
					}
				}

				}
				}
				setState(161);
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
			setState(164);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(162);
				simpleIdentifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(163);
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

	public static class AliasContext extends ParserRuleContext {
		public SimpleIdentifierContext simpleIdentifier() {
			return getRuleContext(SimpleIdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(JaybirdSqlParser.AS, 0); }
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitAlias(this);
		}
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_alias);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(167);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(166);
				match(AS);
				}
			}

			setState(169);
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
			setState(171);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\33\u00b0\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\3\2\3\2\3\2\3\2\3\2\5\2$\n\2"+
		"\3\3\3\3\3\3\3\3\7\3*\n\3\f\3\16\3-\13\3\3\3\5\3\60\n\3\3\3\5\3\63\n\3"+
		"\3\3\3\3\3\4\3\4\3\4\5\4:\n\4\3\4\3\4\7\4>\n\4\f\4\16\4A\13\4\3\4\5\4"+
		"D\n\4\3\4\5\4G\n\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\7\5Q\n\5\f\5\16\5T"+
		"\13\5\3\5\5\5W\n\5\3\5\5\5Z\n\5\3\5\3\5\3\6\3\6\3\6\3\6\7\6b\n\6\f\6\16"+
		"\6e\13\6\3\6\5\6h\n\6\3\6\5\6k\n\6\3\6\3\6\3\7\3\7\3\7\3\7\7\7s\n\7\f"+
		"\7\16\7v\13\7\3\7\5\7y\n\7\3\7\5\7|\n\7\3\7\3\7\3\b\3\b\3\b\3\b\5\b\u0084"+
		"\n\b\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u008c\n\b\3\t\3\t\3\n\3\n\3\n\3\n\3\13"+
		"\3\13\3\13\3\f\3\f\5\f\u0099\n\f\3\f\3\f\3\f\5\f\u009e\n\f\7\f\u00a0\n"+
		"\f\f\f\16\f\u00a3\13\f\3\r\3\r\5\r\u00a7\n\r\3\16\5\16\u00aa\n\16\3\16"+
		"\3\16\3\17\3\17\3\17\7+?Rct\2\20\2\4\6\b\n\f\16\20\22\24\26\30\32\34\2"+
		"\4\3\2\26\27\3\2\23\25\2\u00bc\2#\3\2\2\2\4%\3\2\2\2\6\66\3\2\2\2\bJ\3"+
		"\2\2\2\n]\3\2\2\2\fn\3\2\2\2\16\u008b\3\2\2\2\20\u008d\3\2\2\2\22\u008f"+
		"\3\2\2\2\24\u0093\3\2\2\2\26\u0096\3\2\2\2\30\u00a6\3\2\2\2\32\u00a9\3"+
		"\2\2\2\34\u00ad\3\2\2\2\36$\5\n\6\2\37$\5\4\3\2 $\5\6\4\2!$\5\b\5\2\""+
		"$\5\f\7\2#\36\3\2\2\2#\37\3\2\2\2# \3\2\2\2#!\3\2\2\2#\"\3\2\2\2$\3\3"+
		"\2\2\2%&\7\7\2\2&\'\7\b\2\2\'+\5\24\13\2(*\13\2\2\2)(\3\2\2\2*-\3\2\2"+
		"\2+,\3\2\2\2+)\3\2\2\2,/\3\2\2\2-+\3\2\2\2.\60\5\16\b\2/.\3\2\2\2/\60"+
		"\3\2\2\2\60\62\3\2\2\2\61\63\7\3\2\2\62\61\3\2\2\2\62\63\3\2\2\2\63\64"+
		"\3\2\2\2\64\65\b\3\1\2\65\5\3\2\2\2\66\67\7\17\2\2\679\5\24\13\28:\5\32"+
		"\16\298\3\2\2\29:\3\2\2\2:;\3\2\2\2;?\7\16\2\2<>\13\2\2\2=<\3\2\2\2>A"+
		"\3\2\2\2?@\3\2\2\2?=\3\2\2\2@C\3\2\2\2A?\3\2\2\2BD\5\16\b\2CB\3\2\2\2"+
		"CD\3\2\2\2DF\3\2\2\2EG\7\3\2\2FE\3\2\2\2FG\3\2\2\2GH\3\2\2\2HI\b\4\1\2"+
		"I\7\3\2\2\2JK\7\17\2\2KL\7\f\2\2LM\7\t\2\2MN\7\n\2\2NR\5\24\13\2OQ\13"+
		"\2\2\2PO\3\2\2\2QT\3\2\2\2RS\3\2\2\2RP\3\2\2\2SV\3\2\2\2TR\3\2\2\2UW\5"+
		"\16\b\2VU\3\2\2\2VW\3\2\2\2WY\3\2\2\2XZ\7\3\2\2YX\3\2\2\2YZ\3\2\2\2Z["+
		"\3\2\2\2[\\\b\5\1\2\\\t\3\2\2\2]^\7\t\2\2^_\7\n\2\2_c\5\24\13\2`b\13\2"+
		"\2\2a`\3\2\2\2be\3\2\2\2cd\3\2\2\2ca\3\2\2\2dg\3\2\2\2ec\3\2\2\2fh\5\16"+
		"\b\2gf\3\2\2\2gh\3\2\2\2hj\3\2\2\2ik\7\3\2\2ji\3\2\2\2jk\3\2\2\2kl\3\2"+
		"\2\2lm\b\6\1\2m\13\3\2\2\2no\7\13\2\2op\7\n\2\2pt\5\24\13\2qs\13\2\2\2"+
		"rq\3\2\2\2sv\3\2\2\2tu\3\2\2\2tr\3\2\2\2ux\3\2\2\2vt\3\2\2\2wy\5\16\b"+
		"\2xw\3\2\2\2xy\3\2\2\2y{\3\2\2\2z|\7\3\2\2{z\3\2\2\2{|\3\2\2\2|}\3\2\2"+
		"\2}~\b\7\1\2~\r\3\2\2\2\177\u0083\7\r\2\2\u0080\u0081\5\20\t\2\u0081\u0082"+
		"\7\4\2\2\u0082\u0084\3\2\2\2\u0083\u0080\3\2\2\2\u0083\u0084\3\2\2\2\u0084"+
		"\u0085\3\2\2\2\u0085\u0086\7\5\2\2\u0086\u008c\b\b\1\2\u0087\u0088\7\r"+
		"\2\2\u0088\u0089\5\26\f\2\u0089\u008a\b\b\1\2\u008a\u008c\3\2\2\2\u008b"+
		"\177\3\2\2\2\u008b\u0087\3\2\2\2\u008c\17\3\2\2\2\u008d\u008e\t\2\2\2"+
		"\u008e\21\3\2\2\2\u008f\u0090\5\20\t\2\u0090\u0091\7\4\2\2\u0091\u0092"+
		"\5\20\t\2\u0092\23\3\2\2\2\u0093\u0094\5\20\t\2\u0094\u0095\b\13\1\2\u0095"+
		"\25\3\2\2\2\u0096\u0098\5\30\r\2\u0097\u0099\5\32\16\2\u0098\u0097\3\2"+
		"\2\2\u0098\u0099\3\2\2\2\u0099\u00a1\3\2\2\2\u009a\u009b\7\22\2\2\u009b"+
		"\u009d\5\30\r\2\u009c\u009e\5\32\16\2\u009d\u009c\3\2\2\2\u009d\u009e"+
		"\3\2\2\2\u009e\u00a0\3\2\2\2\u009f\u009a\3\2\2\2\u00a0\u00a3\3\2\2\2\u00a1"+
		"\u009f\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2\27\3\2\2\2\u00a3\u00a1\3\2\2"+
		"\2\u00a4\u00a7\5\20\t\2\u00a5\u00a7\5\22\n\2\u00a6\u00a4\3\2\2\2\u00a6"+
		"\u00a5\3\2\2\2\u00a7\31\3\2\2\2\u00a8\u00aa\7\6\2\2\u00a9\u00a8\3\2\2"+
		"\2\u00a9\u00aa\3\2\2\2\u00aa\u00ab\3\2\2\2\u00ab\u00ac\5\20\t\2\u00ac"+
		"\33\3\2\2\2\u00ad\u00ae\t\3\2\2\u00ae\35\3\2\2\2\32#+/\629?CFRVYcgjtx"+
		"{\u0083\u008b\u0098\u009d\u00a1\u00a6\u00a9";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}