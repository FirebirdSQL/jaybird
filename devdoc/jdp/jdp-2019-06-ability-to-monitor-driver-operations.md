# jdp-2019-06: Ability to monitor driver operations

## Status

- Published: 2019-10-12
- Implemented in: Jaybird 4

## Type

- Feature-Specification

## Context

At any given time, a different number of statements `org.firebirdsql.gds.ng.FbStatement` 
can be executed in an application. The two main states in which a statement 
can exist are the execution `org.firebirdsql.gds.ngStatementState.EXECUTING` 
and fetching `org.firebirdsql.gds.ngStatementState.CURSOR_OPEN`.

The first problem is that these statuses allow us to track the status of the 
statement, but do not allow it centrally for all statements. Partly, this problem 
can be solved at the application level. But for this, each statement will have 
to be wrapped to monitor its execution. At the driver level, tracking is much easier.

The second problem is the monitoring of row fetches. Executing or fetching data 
can take considerable time and at the moment we do not know whether operations 
are performed or not.

The main idea of ??this proposal is to add a mechanism that allows at an application
level to know what operations are performed at a given moment in time.

## Decision

This proposal should be taken as a suggestion and may be revised.

Implementation involves the addition of several interfaces and classes to monitor 
the execution of statements.

As it is possible that this monitoring is extended to other database or driver
operations, the naming tries to be agnostic of the actual operation monitored.

### Operation interface

At the `org.firebirdsql.gds.ng.monitor` package it is proposed to add the
`Operation` interface, which contains methods for managing operations. At time
of writing, we are only interested in canceling the operation, so the interface
contains only the method for canceling an operation `void cancel()`. Other
methods may be added later.

The `Operation` interface also exposes the type of operation through the method
`getType()`.

### Interface implementation

The interface implementation can also be placed at `org.firebirdsql.gds.ng.monitor`
package. A separate class can be implemented for each type of operation. Since
we only need to cancel the operation, then object of `FbDatabase` class is
suitable for this. This class already contains a method for cancelling current
operation, `cancelOperation(int)`.

### Monitor the statement executions

The main idea is to inform the client application about the starting and
finishing of the statement operation, such as execution and data fetching. At
the same time, we should not complicate the driver, but only provide a means to
monitor and - in limited fashion - control execution.

At the driver level, it makes no sense to monitor. To let know the client about
the starting and finishing of the statement's operations, it is proposed to add 
a class `org.firebirdsql.gds.ng.OperationMonitor` and an 
interface `org.firebirdsql.gds.ng.monitor.OperationAware`.

The class `OperationMonitor` class will contain a single static instance 
of `OperationAware` in the client application. The class has a static method for
initialization of this instance `initOperationAware(OperationAware s)`. When a
security manager is present, the `SQLPermission` with name 
`org.firebirdsql.jaybird.initOperationAware` is checked when setting this.
 
The monitor also has static methods to call instance `startOperation(Operation op)`
and `endOperation(Operation op)` (subject to later refinement) to be
called by the implementation. 

The interface `OperationAware` has methods for reporting the start and finish of
the operation `void startOperation(Operation op)` and 
`void endOperation(Operation op)`.

Based on the above features, before starting the operation, we call the static 
method:
```
OperationMonitor.startOperation(op);
```
After the operation is completed, it is necessary to inform about its finishing:
```
OperationMonitor.endOperation(op);
```
**It is important to note** that for any result of the operation (successful or not), 
**we must let know the finishing**.

With this implementation, if the instance of the `OperationMonitor` class is not
initialized, the driver will work as usual.

### Possible errors and shortcomings

Initializing the `OperationMonitor` with an instance of `OperationAware` from a
different classloader may lead to memory leaks. The current implementation does
not address or prevent this in any way.

Frequent invocation of statement execution causes the creation of new `Operation`
objects. Perhaps there is a more correct way to process the current operation.
Although this code should not cause memory leaks.

Calling of `cancelOperation` method from `Operation` may be problematic, as it
could also cancel something other than this operation. Perhaps, that all 
operations with one database handler will be canceled. In an attempt to address
this, the implementation prevents cancellation after the operation has finished.

Calling `cancelOperation` may not cause the operation to complete immediately,
it all depends on a server. It may be worth cancelling the operation at the 
driver, and the server will finish it later. The current implementation does not
address this.

## Consequences

This implementation will allow centralized monitoring of any statements.

In this implementation, we tried to minimize changes in the driver so 
as not to affect its operation.
