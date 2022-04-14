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
package org.firebirdsql.common.rules;

import org.firebirdsql.common.extension.RunEnvironmentExtension;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assume.assumeThat;

/**
 * Simple (and naive) rule to check for certain environment requirements.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class RunEnvironmentRule implements TestRule {

    private final List<RunEnvironmentExtension.EnvironmentRequirement> unmetRequirements;

    public RunEnvironmentRule(RunEnvironmentExtension runEnvironmentExtension) {
        unmetRequirements = runEnvironmentExtension.getUnmetRequirements();
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                assumeThat("Unmet requirements", unmetRequirements, empty());
                base.evaluate();
            }
        };
    }
}
