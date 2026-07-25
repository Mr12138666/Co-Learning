#!/bin/bash
# Maven wrapper that clears the CLASSPATH env var (which breaks Maven on this machine)
# Usage: cd backend && ./run-mvn.sh <maven args>

MAVEN_HOME="D:/DevTools/apache-maven-3.9.10"
PROJECT_DIR="$(pwd)"

exec env -u CLASSPATH java \
  -cp "${MAVEN_HOME}/boot/plexus-classworlds-2.9.0.jar" \
  -Dclassworlds.conf="${MAVEN_HOME}/bin/m2.conf" \
  -Dmaven.home="${MAVEN_HOME}" \
  -Dmaven.multiModuleProjectDirectory="${PROJECT_DIR}" \
  org.codehaus.plexus.classworlds.launcher.Launcher "$@"
