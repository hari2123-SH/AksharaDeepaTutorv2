#!/bin/sh
# Gradle wrapper script
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
exec "$GRADLE_USER_HOME/wrapper/dists/gradle-8.2-bin/*/gradle-8.2/bin/gradle" "$@" 2>/dev/null || \
  java -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
