#!/bin/sh
# -------------------------------------------------------------------------
# This is the Unix shutdown script for the application
# -------------------------------------------------------------------------

PID_FILE=".pid"
MAX_TRIES=6

alive() {
    kill -0 $1 >/dev/null 2>&1
}

if [ -f ${PID_FILE} ]; then
    cat ${PID_FILE} | while read PID ; do
        INDEX=1

        while alive $PID && [ $INDEX -le $MAX_TRIES ]; do
            echo "Stopping process: ${PID} (try=$INDEX)"
            kill $PID
            sleep 3
            INDEX=`expr $INDEX + 1`
        done

        if alive $PID ; then
            echo "Aborting process: ${PID}"
            kill -ABRT $PID
        fi
    done
    if [ -f "$PID_FILE" ]; then
        rm $PID_FILE
    fi
else
    echo "Service already shut down."
    exit 1
fi


