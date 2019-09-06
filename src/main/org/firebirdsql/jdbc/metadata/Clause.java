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
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.util.InternalApi;

import java.util.Objects;

/**
 * Condition clause for constructing metadata query conditions.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@InternalApi
public final class Clause {
    
    private final String condition;
    private final String value;

    public Clause(String columnName, String pattern) {
        MetadataPattern metadataPattern = MetadataPattern.compile(pattern);
        condition = metadataPattern.renderCondition(columnName);
        value = metadataPattern.getConditionValue();
    }

    /**
     * @return Result of {@code getCondition(true)}
     */
    public String getCondition() {
        return getCondition(true);
    }

    public String getCondition(boolean includeAnd) {
        if (includeAnd) {
            return getCondition("", " and ");
        }
        return getPlainCondition();
    }

    public String getCondition(String prefix, String suffix) {
        if (emptyCondition()) {
            return "";
        }
        if (Objects.equals("", prefix) && Objects.equals("", suffix)) {
            return condition;
        }
        return prefix + condition + suffix;
    }

    private String getPlainCondition() {
        if (emptyCondition()) {
            return "";
        }
        return condition;
    }

    public String getValue() {
        return value;
    }

    public boolean hasCondition() {
        return !emptyCondition();
    }

    private boolean emptyCondition() {
        return condition.isEmpty();
    }
}
