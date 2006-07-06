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
set ANT= .\bin\ant.bat

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
REM set PATH=%PATH%;..\output\classes
REM set PATH=%PATH%;..
set PATH=%PATH%;"%ANT_HOME%\bin"
set PATH=%PATH%;"%JAVA_HOME%\lib\tools.jar"
REM for %%i in ("..\lib\*.jar") do call ".\lcp.bat" %%i
set CLASSPATH=.\lib\junit.jar

REM set common ANT options
set JAXP_DOM_FACTORY="org.apache.xerces.jaxp.DocumentBuilderFactoryImpl"
set JAXP_SAX_FACTORY="org.apache.xerces.jaxp.SAXParserFactoryImpl"

rem set ANT_CMD_LINE_ARGS=%ANT_CMD_LINE_ARGS% -Djava.library.path=.
set ANT_CMD_LINE_ARGS=%ANT_CMD_LINE_ARGS% -Djavax.xml.parsers.DocumentBuilderFactory=%JAXP_DOM_FACTORY%
set ANT_CMD_LINE_ARGS=%ANT_CMD_LINE_ARGS% -Djavax.xml.parsers.SAXParserFactory=%JAXP_SAX_FACTORY%

:RunAnt
echo on
%ANT% %ANT_CMD_LINE_ARGS%

:end
