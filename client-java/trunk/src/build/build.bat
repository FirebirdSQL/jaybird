@echo off

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
set LOCALCLASSPATH=..\..\build\classes
set LOCALCLASSPATH=%LOCALCLASSPATH%;..
set LOCALCLASSPATH=%LOCALCLASSPATH%;"%JAVA_HOME%\lib\tools.jar"
for %%i in ("..\..\lib\*.jar") do call ".\lcp.bat" %%i

:runAnt
echo on
java -classpath %LOCALCLASSPATH% org.apache.tools.ant.Main %ANT_CMD_LINE_ARGS%

:end
