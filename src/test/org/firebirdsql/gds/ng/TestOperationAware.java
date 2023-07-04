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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ng.monitor.Operation;
import org.firebirdsql.gds.ng.monitor.OperationAware;

import java.util.ArrayList;
import java.util.List;

public class TestOperationAware implements OperationAware {

    private final List<OperationReport> reportedOperations = new ArrayList<>();

    @Override
    public void startOperation(Operation operation) {
        reportedOperations.add(OperationReport.start(operation));
    }

    @Override
    public void endOperation(Operation operation) {
        reportedOperations.add(OperationReport.end(operation));
    }

    List<OperationReport> getReportedOperations() {
        return reportedOperations;
    }

    public static class OperationReport {

        private final Type type;
        private final Operation operation;

        private OperationReport(Type type, Operation operation) {
            this.type = type;
            this.operation = operation;
        }

        public Type getType() {
            return type;
        }

        public Operation getOperation() {
            return operation;
        }

        static OperationReport start(Operation operation) {
            return new OperationReport( Type.START, operation);
        }

        static OperationReport end(Operation operation) {
            return new OperationReport(Type.END, operation);
        }

        enum Type {
            START,
            END
        }
    }
}
