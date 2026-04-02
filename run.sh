#!/bin/sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"

if [ "${TERM_PROGRAM:-}" = "vscode" ] && [ "${SPLENDOR_RUNSH_IN_NEW_TERMINAL:-0}" != "1" ]; then
    echo "[run.sh] Detected VS Code terminal. Opening a new Terminal window..."
    export SPLENDOR_RUNSH_IN_NEW_TERMINAL=1
    osascript <<EOF
tell application "Terminal"
    activate
    do script "cd $(printf '%q' "$SCRIPT_DIR") && SPLENDOR_RUNSH_IN_NEW_TERMINAL=1 ./run.sh"
end tell
EOF
    exit 0
fi

cd "$SCRIPT_DIR"
exec mvn clean javafx:run
