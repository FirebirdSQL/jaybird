# jdp-2019-06: Ability to control statements execution

## Status

- Draft

## Type

- Feature-Specification

## Context

At any given time, a different number of statements `org.firebirdsql.gds.ng.FbStatement` 
can be executed in an application. The two main states in which a statement 
can be exist are the execution `org.firebirdsql.gds.ngStatementState.EXECUTING` 
and fetching `org.firebirdsql.gds.ngStatementState.CURSOR_OPEN`.

The first problem is that these statuses allow us to track the status of the 
statement, but do not allow it centrally for all statements. Partly, this problem 
can be solved at the application level. But for this, each statement will have 
to be wrapped to control executing. At the driver level, tracking is much easier.
The second problem is the control of fetching rows. Executing or fetching data 
can take considerable time and at the moment we do not know whether operations 
are performed or not.

The main idea of ​​this proposal is to add a mechanism that allows at an application
level to know what operations are performed at a given moment in time.

## Decision

This proposal should be taken as a suggestion and may be revised.

Implementation involves the addition of several interfaces and classes to control 
the execution of statements.

### Operation interface

At the `org.firebirdsql.gds` package it is proposed to add the `Operation` interface, 
which contains methods for managing operations. At time of writing, we are only 
interested in canceling the operation, so the interface contains only the method for 
canceling a operation `void cancelOperation()`. Other methods may be added later.

### Interface implementation

The interface implementation can be placed at `org.firebirdsql.gds.impl` package.
A separate class can be implemented for each type of operation. Since we only 
need to cancel the operation, then object of `GDSHelper` class is suitable for this.
This class already contains method for cancelling current operation `cancelOperation()`.

### Control the statement executions

The main idea is to inform the client application about the starting and finishing 
of the statement operation, such as execution and data fetching. At the same time, 
we should **not overload** the driver, but only **provide way for control**. At the 
driver level, it makes no sense to control. To let know the client about the 
starting and finishing of the statement's operations, it is proposed to add 
an abstract class `org.firebirdsql.gds.ng.StatementOperationAware`. This abstract
class will contain only an static instance of its implementation in the client 
application, abstract methods for reporting the start and finish of the operation
`public abstract void startOperation(final Operation op)` and
`public abstract void finishOperation(final Operation op)`, 
static method for initialization of instance 
`public static void initStatementOperationAware(StatementOperationAware s)` and 
static methods to call instance `public static void startStatementOperation(Operation op)`
and `public static void finishStatementOperation(Operation op)`. 
Based on the above features, before starting the operation, we call the static 
method:
```
    StatementOperationAware.startStatementOperation(op);
```
After the operation is completed, it is necessary to inform about its finishing:
```
    StatementOperationAware.finishStatementOperation(op);
```
**It is important to note** that for any result of the operation (successful or not), 
**we must let know the finishing**.
With this implementation, if the instance of the `StatementOperationAware` class 
is not initialized, the driver will work as usual.

### Possible errors and shortcomings

Frequent invocation of statement execution causes the creation of new 
`GDSHelperOperation` class objects. Perhaps there is a more correct way 
to process the current operation. Although this code should not cause memory leaks.

Calling of `cancelOperation` method from `GDSHelperOperation` may be problematic,
as it could also cancel something other than this operation. Perhaps, that all 
operations with one database handler will be canceled.

Calling `cancelOperation` may not cause the operation to complete immediately,
it all depends on a server. It may be worth cancelling the operation at the 
driver, and the server will finish it later.

## Consequences

This implementation will allow centralized control any statements, at 
that synchronously.

In this implementation, we tried to minimize changes in the driver so 
as not to affect its operation.

