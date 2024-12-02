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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.jspecify.annotations.NullMarked;

import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

/**
 * Decorator for {@link FBFetcher} that will block methods not defined for {@link java.sql.ResultSet#TYPE_FORWARD_ONLY}.
 *
 * @author Mark Rotteveel
 * @since 6
 */
@NullMarked
final class ForwardOnlyFetcherDecorator implements FBFetcher {

    private final FBFetcher fetcher;

    ForwardOnlyFetcherDecorator(FBFetcher fetcher) {
        this.fetcher = requireNonNull(fetcher, "fetcher");
        assert !(fetcher instanceof FBStatementFetcher) :
                "Decorating an instance of FBStatementFetcher is not appropriate as it is already forward-only";
    }

    @Override
    public FetchConfig getFetchConfig() {
        return fetcher.getFetchConfig();
    }

    @Override
    public void setReadOnly() throws SQLException {
        fetcher.setReadOnly();
    }

    @Override
    public boolean first() throws SQLException {
        throw notScrollable();
    }

    @Override
    public boolean last() throws SQLException {
        throw notScrollable();
    }

    @Override
    public boolean previous() throws SQLException {
        throw notScrollable();
    }

    @Override
    public boolean next() throws SQLException {
        return fetcher.next();
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        throw notScrollable();
    }

    @Override
    public boolean relative(int row) throws SQLException {
        throw notScrollable();
    }

    @Override
    public void beforeFirst() throws SQLException {
        throw notScrollable();
    }

    @Override
    public void afterLast() throws SQLException {
        throw notScrollable();
    }

    @Override
    public void close() throws SQLException {
        fetcher.close();
    }

    @Override
    public void close(CompletionReason completionReason) throws SQLException {
        fetcher.close(completionReason);
    }

    @Override
    public boolean isClosed() {
        return fetcher.isClosed();
    }

    @Override
    public int getRowNum() throws SQLException {
        return fetcher.getRowNum();
    }

    @Override
    public boolean isEmpty() throws SQLException {
        return fetcher.isEmpty();
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return fetcher.isBeforeFirst();
    }

    @Override
    public boolean isFirst() throws SQLException {
        return fetcher.isFirst();
    }

    @Override
    public boolean isLast() throws SQLException {
        return fetcher.isLast();
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return fetcher.isAfterLast();
    }

    @Override
    public void insertRow(RowValue data) throws SQLException {
        fetcher.insertRow(data);
    }

    @Override
    public void deleteRow() throws SQLException {
        fetcher.deleteRow();
    }

    @Override
    public void updateRow(RowValue data) throws SQLException {
        fetcher.updateRow(data);
    }

    @Override
    public void renotifyCurrentRow() throws SQLException {
        fetcher.renotifyCurrentRow();
    }

    @Override
    public int getFetchSize() throws SQLException {
        return fetcher.getFetchSize();
    }

    @Override
    public void setFetchSize(int fetchSize) throws SQLException {
        fetcher.setFetchSize(fetchSize);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return fetcher.getFetchDirection();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        fetcher.setFetchDirection(direction);
    }

    @Override
    public int currentPosition() throws SQLException {
        return fetcher.currentPosition();
    }

    @Override
    public int size() throws SQLException {
        return fetcher.size();
    }

    @Override
    public void setFetcherListener(FBObjectListener.FetcherListener fetcherListener) {
        fetcher.setFetcherListener(fetcherListener);
    }

    private static SQLException notScrollable() {
        return FbExceptionBuilder.toNonTransientException(JaybirdErrorCodes.jb_operationNotAllowedOnForwardOnly);
    }

}
