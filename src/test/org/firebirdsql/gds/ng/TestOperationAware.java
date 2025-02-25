// SPDX-FileCopyrightText: Copyright 2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
