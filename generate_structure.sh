#!/bin/sh
set -e

START_DIR="src/main/java/edu/cs102/g04t06"
BASE="$START_DIR/game"

# --------------------------------------------------
# 1. Create base directory
# --------------------------------------------------
mkdir -p "$BASE"

# --------------------------------------------------
# 2. Directory structure (parent : children...)
# --------------------------------------------------
STRUCTURE="
rules:entities valueobjects
execution:ai
presentation:console network
infrastructure:config data
"

echo "$STRUCTURE" | while IFS=: read -r parent children; do
  [ -z "$parent" ] && continue
  for child in $children; do
    mkdir -p "$BASE/$parent/$child"
  done
done

# --------------------------------------------------
# 3. Create Java files using loops
# --------------------------------------------------

# ---- rules ----
RULES_FILES="
GameState
GameRules
entities/Noble
entities/Player
entities/Card
entities/GemColor
entities/ActionType
valueobjects/GemCollection
valueobjects/CardMarket
valueobjects/Cost
"

for f in $RULES_FILES; do
  touch "$BASE/rules/$f.java"
done

# ---- execution ----
EXECUTION_FILES="
ai/AIPlayer
GameEngine
ActionExecutor
ActionResult
"

for f in $EXECUTION_FILES; do
  touch "$BASE/execution/$f.java"
done

# ---- infrastructure ----
INFRA_FILES="
config/ConfigLoader
data/ExcelDataLoader
"

for f in $INFRA_FILES; do
  touch "$BASE/infrastructure/$f.java"
done

# ---- presentation (console) ----
PRESENTATION_FILES="
console/ConsoleUI
console/InputHandler
console/GameRenderer
"

for f in $PRESENTATION_FILES; do
  touch "$BASE/presentation/$f.java"
done

echo "✅ Project structure created successfully"