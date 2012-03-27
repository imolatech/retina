cls
echo off
rem -------------------------------------------------------------------------
rem This is the Windows startup script for the application
rem -------------------------------------------------------------------------

rem ### START Variable Config From service.properties (Todo: Find a better way in Windows)###
grep "^appName=" service.properties | sed s/appName=// | xargs > input.txt
set /p APP_NAME=< input.txt
rm input.txt

grep "^appInstance=" service.properties | sed s/appInstance=// | xargs > input.txt
set /p APP_INSTANCE=< input.txt
set APP_NAME=%APP_NAME%%APP_INSTANCE%
rm input.txt

grep "^homeDir=" service.properties | sed s/homeDir=// | xargs > input.txt
set /p HOME_DIR=< input.txt
rm input.txt

grep "^jmxPort=" service.properties | sed s/jmxPort=// | xargs > input.txt
set /p JMX_PORT=< input.txt
rm input.txt

grep "^logDir=" service.properties | sed s/logDir=// | xargs > input.txt
set /p LOG_DIR=< input.txt
rm input.txt

grep "^logLevel=" service.properties | sed s/logLevel=// | xargs > input.txt
set /p LOG_LEVEL=< input.txt
rm input.txt

grep "^memorySize=" service.properties | sed s/memorySize=// | xargs > input.txt
set /p MEMORY_SIZE=< input.txt
rm input.txt

grep "^moreJavaOptions=" service.properties | sed s/moreJavaOptions=// | xargs > input.txt
set /p MORE_JAVA_OPTIONS=< input.txt
rm input.txt

grep "^monitoring.support.enabled=" service.properties | sed s/monitoring.support.enabled=// | xargs > input.txt
set /p MONITORING_SUPPORT_ENABLED=< input.txt
rm input.txt

rem ### END Variable Config ###

set JAVA_OPTS=-DappName=%APP_NAME% -Denv=%ENVIRONMENT% -Dhome_dir=%HOME_DIR% -Dapp_log_dir=%LOG_DIR% -Dlog_level=%LOG_LEVEL% -Dassembly=assembly.xml -Duser.timezone=UTC

set CLASS_NAME=com.active.services.core.server.DefaultServer
set DEBUG_PORT=5005

rem This funny block of code is used to simply find the actual directory that this batch
rem file is in.  We use this to set the current working directory since this file
rem may be invoked from anywhere on the file system.
for %%x in (%0) do set START_PATH=%%~dpsx
rem echo START_PATH = %START_PATH%
cd %START_PATH%

if "%JAVA_HOME%" == "" goto NO_JAVA_HOME

set TOOLS_JAR="%JAVA_HOME%/lib/tools.jar"

echo -
echo - Starting -
echo -
echo * Logging information may be found in %LOG_DIR%/%APP_NAME%.log *
        
set JAVA_OPTS=%JAVA_OPTS% -ea -ms%MEMORY_SIZE% -mx%MEMORY_SIZE% -Dfile.encoding=UTF-8 -classpath %HOME_DIR%/lib/*;%HOME_DIR%/jetty/lib/*;%HOME_DIR%/config/;%HOME_DIR%/jetty/;%TOOLS_JAR%
set JAVA_OPTS=-Dcom.sun.management.jmxremote.port=%JMX_PORT% -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false %JAVA_OPTS%
title %APP_NAME%

IF "%1" == "-d" (
rem -agentlib:yjpagent
rem    set JAVA_OPTS= -Dcom.sun.management.jmxremote -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=%DEBUG_PORT%,suspend=n %JAVA_OPTS%
    set JAVA_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=%DEBUG_PORT%,suspend=n %JAVA_OPTS%
)

"%JAVA_HOME%\bin\java" %JAVA_OPTS% %MORE_JAVA_OPTIONS% %CLASS_NAME% 
goto end

:NO_JAVA_HOME
echo The environment variable 'JAVA_HOME' has not been set.  Please do this and then execute this script again.
goto end

:end
rem exit
