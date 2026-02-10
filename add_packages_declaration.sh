#!/bin/sh
set -e

SRC_ROOT="src/main/java/edu/cs102/g04t06/game"
BASE_PKG="edu.cs102.g04t06.game"

find "$SRC_ROOT" -name "*.java" | while read -r file; do

  # Skip if package already declared
  if grep -Eq '^[[:space:]]*package[[:space:]]+' "$file"; then
    echo "↷ Skipping (package exists): $file"
    continue
  fi

  # Get path relative to SRC_ROOT
  rel_path=$(printf '%s\n' "$file" \
    | sed "s|^$SRC_ROOT/||" \
    | sed 's|/[^/]*\.java$||')

  # Build package name
  if [ -n "$rel_path" ]; then
    pkg="$BASE_PKG.$(printf '%s\n' "$rel_path" | tr '/' '.')"
  else
    pkg="$BASE_PKG"
  fi

  # Insert package declaration
  {
    echo "package $pkg;"
    echo
    cat "$file"
  } > "$file.tmp" && mv "$file.tmp" "$file"

  echo "✓ Added package to: $file"

done

echo "✅ Package insertion complete"
