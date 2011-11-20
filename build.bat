@echo off

set ANT_HOME=.

REM check JAVA_HOME
if "%JAVA_HOME%" == "" goto noJavaHome
goto slurpArgs

:noJavaHome
echo.
echo Warning: JAVA_HOME environment must be set.
echo.
goto end

REM Slurp the command line arguments.  This loop allows for an unlimited number of 
REM arguments (up to the command line limit, anyway).
:slurpArgs
set ANT=%ANT_HOME%\bin\ant.bat

set ANT_CMD_LINE_ARGS=

:setupArgs
if %1a==a goto doneArgs
set ANT_CMD_LINE_ARGS=%ANT_CMD_LINE_ARGS% %1
shift
goto setupArgs

:doneArgs
REM The doneArgs label is here just to provide a place for the argument list loop
REM to break out to.

REM add entries to the classpath
SETLOCAL
set CLASSPATH=%CD%\lib\cpptasks.jar

:RunAnt
echo on
%ANT% %ANT_CMD_LINE_ARGS%

:end
