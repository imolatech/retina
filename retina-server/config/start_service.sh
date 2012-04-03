#!/bin/sh
# -------------------------------------------------------------------------
# This is the Unix startup script for the application
# -------------------------------------------------------------------------

PID_FILE=".pid"

DEBUG=0
SUSPEND=n
BACKGROUND=1
TEST_SCRIPT=0
TRACE=0
JAVA_OPTS=''
USER_JAVA_OPTS=''
DEFAULT_MONITORING_SUPPORT_COLLECTD_CLIENT_PATH=/opt/imolatech/collectd-service/junixsocket
DEFAULT_MONITORING_SUPPORT_COLLECTD_DATA_CHANNEL=udpSender

error() {
  if [ $# -gt 0 ]; then
    msg="ERROR: $1"
    shift
  else
    msg="ERROR"
  fi
  echo "$msg"
  while [ $# -gt 0 ]; do
    msg="$1"
    shift
    echo "   $msg"
  done
  exit 1
}


help() {
    echo ""
    echo "This script reads properties from config/system.properties"
    echo "and starts the application"
    echo ""
    sed -n -e '/) \#\#/d' -e '1,/\#\# handle commands/d' \
      -e '/\#\# done handle commands/,10000d' \
      -e 's:^[ \t]*\([^(]*\)):  \1:p' \
      -e 's:^[ \t]*##[ \t]*\(.*\):      \1:p'  "$0"
}


get_var()
{
    sed -n -e "s/^$1=//p" service.properties | tr -d '\n\r'
}


get_var_with_default()
{
    RESULT=`sed -n -e "s/^$1=//p" service.properties | tr -d '\n\r'`
    if [ -z "$RESULT" ]; then
        RESULT="$2"
    fi
    printf -- "$RESULT"
}


tools_path() {
    TOOLS_JAR="${JAVA_HOME}/lib/tools.jar"

    if [ ! -f "$TOOLS_JAR" ]; then
        case "`uname`" in
            Darwin*)
                TOOLS_JAR="${JAVA_HOME}/../Classes/classes.jar"
                ;;
        esac
    fi
    if [ ! -f "$TOOLS_JAR" ]; then
        error "tools.jar not found"
    fi
}


get_pid() {
    if [ ! -f .pid ]; then
        error ".pid missing. Is app running?"
    fi
    PID=`cat .pid`
}


full_path() {
    if [ -d "$1" ]; then
        ( cd "$1" >/dev/null 2>&1 ; pwd )
    else
        error "bad dir: $1"
    fi
}


btrace_install() {
    echo "installing btrace probe using args: $@"
    tools_path
    get_pid
    "$JAVA_HOME/bin/java" -Dcom.sun.btrace.probeDescPath=. \
        -Dcom.sun.btrace.dumpClasses=false \
        -Dcom.sun.btrace.debug=false \
        -Dcom.sun.btrace.unsafe=false \
        -classpath ./btrace/*:../lib/*:${TOOLS_JAR} com.sun.btrace.client.Main $PID "$@"

    ## TODO I may want to tail the btrace file here.
    exit
}



######
######    Functions that can be overridden in custom_start.sh
######


custom_init() {
  true
}

custom_arg() {
  true
}

custom_pre_exec() {
  true
}


######    End of custom_start.sh functions


CONFIG_DIR=`dirname "$0"`
CONFIG_DIR=`full_path "$CONFIG_DIR"`

cd "$CONFIG_DIR"

if [ -f "custom_start.sh" ]; then
  echo "Loading start extensions: custom_start.sh"
  . ./custom_start.sh
fi


custom_init || error "custom_init failed"

## handle commands
while [ $# -gt 0 ];
do
    arg=$1
    shift
    case "$arg" in
        -debug | --debug | -d)
            ## Enable debugging in process.
            DEBUG=1 ;;

        -suspend | --suspend)
            ## Syspend process until debugger attaches.
            SUSPEND=y ;;

        --test)
            ## internal testing
            TEST_SCRIPT=1 ;;

        -foreground | --foreground | -f)
            ## Run in forground and log to attached terminal.
            BACKGROUND=0 ;;

        -trace | -t)
            ## Install btrace probes located in config/trace on start.
            TRACE=1 ;;

        -btrace | --btrace)
            ## Install new btrace class into running application. The process must be running.
            ## All arguments after the option are passed to btrace.
            ## Be aware that this will not start the application and other options have no effect when
            ## combined with this option.
            btrace_install "$@"
            ;;

        -D*)
            ## define system property in JVM (same syntax as java)
            USER_JAVA_OPTS="$USER_JAVA_OPTS $arg"
            ;;

         -J)
            ## pass arg to jvm (ex: -J -XX:MaxPermGen=1g)
            arg="$1"
            shift
            USER_JAVA_OPTS="$USER_JAVA_OPTS $arg"
            ;;

         -J*)
            ## pass arg to jmv (ex: -J-XX:MaxPermGen=1g)
            arg="`echo $arg | sed 's/^-J//'`"
            USER_JAVA_OPTS="$USER_JAVA_OPTS $arg"
            ;;

         -help | -h | -? | --help)
            ## print help
            help
            exit 1
            ;;

        *)
            ## error out for unknown command
            custom_arg "$arg" || error "bad arg: $arg"
            ;;
    esac
done
## done handle commands



if [ -f "$PID_FILE" ]; then
    ./stop_service.sh
fi

tools_path


# ### START Custom Config ###
APP_NAME=`get_var appName`
APP_NAME=${APP_NAME}`get_var appInstance`
HOME_DIR=`get_var homeDir`
JMX_PORT=`get_var jmxPort`
LOG_DIR=`get_var logDir`
LOG_LEVEL=`get_var logLevel`
MEMORY_SIZE=`get_var memorySize`
MORE_JAVA_OPTIONS=`get_var moreJavaOptions`
ENVIRONMENT=`get_var environment`
JVM_MEMORY_BITS="`get_var_with_default jvm.memory.bits 32`"
JVM_MEMORY_PERMGEN=`get_var jvm.memory.permgen`
MONITORING_SUPPORT_ENABLED=`get_var_with_default monitoring.support.enabled false`
MONITORING_SUPPORT_COLLECTD_CLIENT_PATH=`get_var_with_default monitoring.support.collectd.client.path "$DEFAULT_MONITORING_SUPPORT_COLLECTD_CLIENT_PATH"`
MONITORING_SUPPORT_COLLECTD_DATA_CHANNEL=`get_var_with_default monitoring.support.collectd.dataChannel "$DEFAULT_MONITORING_SUPPORT_COLLECTD_DATA_CHANNEL"`

# ### END Custom Config ###

if [ $TEST_SCRIPT -eq 1 ]; then
    echo "appName=$APP_NAME"
    echo "homeDir=$HOME_DIR"
    echo "jmxPort=$JMX_PORT"
    echo "logDir=$LOG_DIR"
    echo "logLevel=$LOG_LEVEL"
    echo "memorySize=$MEMORY_SIZE"
    echo "moreJavaOptions=$MORE_JAVA_OPTIONS"
    echo "monitoring.support.enabled=$MONITORING_SUPPORT_ENABLED"
fi


JAVA_OPTS="$JAVA_OPTS \
    -DappName=${APP_NAME} \
    -Denv=${ENVIRONMENT} \
    -Dconfig_dir=${CONFIG_DIR} \
    -Dhome_dir=${HOME_DIR} \
    -Dapp_log_dir=${LOG_DIR} \
    -Dlog_level=${LOG_LEVEL} \
    -Dassembly=assembly.xml \
    -Duser.timezone=UTC"

if [ "$MONITORING_SUPPORT_ENABLED" = "true" -a "$MONITORING_SUPPORT_COLLECTD_DATA_CHANNEL" = "socketSender" ] ; then
	JAVA_OPTS="$JAVA_OPTS -Djava.library.path=$MONITORING_SUPPORT_COLLECTD_CLIENT_PATH"
fi

CLASS_NAME="com.imolatech.retina.RetinaServer"
DEBUG_PORT="5005"
TRACE_OPTS=""


if [ -z "$JAVA_HOME" ] ; then
    error "The environment variable 'JAVA_HOME' has not been set.  Please do this and then execute this script again."
fi

if [ ! -d $LOG_DIR ] ; then
    mkdir $LOG_DIR
fi

if [ $TRACE -ne 0 ]; then
    TRACE_OPTS="-javaagent:btrace/btrace-agent.jar=scriptdir=trace"
fi

[ -f ./default.log ] || [ -h ./default.log ] || ln -s "$LOG_DIR/$APP_NAME.log" default.log

echo "-"
echo "- Starting -"
echo "-"
echo "* Logging information may be found in ${LOG_DIR}/${APP_NAME}.log *"

if [ "$JVM_MEMORY_BITS" = "32" ]; then
    JAVA_OPTS="${JAVA_OPTS} -d32"
elif [ "$JVM_MEMORY_BITS" = "64" ]; then
    JAVA_OPTS="${JAVA_OPTS} -d64"
else
    error "Invalid size specified for jvm.memory.bits. Must be 32 or 64. |$JVM_MEMORY_BITS|"
fi



CP="${HOME_DIR}/lib/*:${HOME_DIR}/jwebsocket/lib/*:${HOME_DIR}/config"

GC_LOGGING="-verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:${LOG_DIR}/${APP_NAME}-gc.log"

PERM_GEN=
if [ "$JVM_MEMORY_PERMGEN" ]; then
  PERM_GEN="-XX:MaxPermSize=$JVM_MEMORY_PERMGEN -XX:PermSize=$JVM_MEMORY_PERMGEN"
fi

GC=-XX:+UseConcMarkSweepGC

JAVA_OPTS="${JAVA_OPTS} \
    ${MEM_OPT} \
    -ea -Xms${MEMORY_SIZE} \
    -Xmx${MEMORY_SIZE} \
    ${PERM_GEN} \
    -Dfile.encoding=UTF-8 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -Dcom.sun.management.jmxremote.port=${JMX_PORT} \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false"


if [ $DEBUG -eq 1 ] ; then
    JAVA_OPTS="-Xdebug \
        -Xnoagent \
        -Djava.compiler=NONE \
        -Xrunjdwp:transport=dt_socket,server=y,address=${DEBUG_PORT},suspend=$SUSPEND \
        ${JAVA_OPTS}"
fi

custom_pre_exec || error "custom_pre_exec failed"

if [ $BACKGROUND -eq 1 ]; then
    JAVA_OPTS="${JAVA_OPTS} -Dfnd.console.log.disable=true"
    $JAVA_HOME/bin/java $TRACE_OPTS $JAVA_OPTS -classpath "$CP" ${GC} ${GC_LOGGING} ${MORE_JAVA_OPTIONS} ${USER_JAVA_OPTS} $CLASS_NAME </dev/null >/dev/null 2>&1 &
    PID=$!
    echo "$PID" >> $PID_FILE
else
    JAVA_OPTS="${JAVA_OPTS} -Dfnd.console.log.disable=false"
    echo $$ >> $PID_FILE
    exec $JAVA_HOME/bin/java $TRACE_OPTS $JAVA_OPTS -classpath "$CP" ${GC} ${GC_LOGGING} ${MORE_JAVA_OPTIONS} ${USER_JAVA_OPTS} $CLASS_NAME
fi
