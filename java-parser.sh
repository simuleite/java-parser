#!/bin/bash

# Java Parser Launcher Script
# Executable JAR: ~/.abcoder/java-parser.jar

JAR_PATH="$HOME/.abcoder/java-parser.jar"

if [ ! -f "$JAR_PATH" ]; then
    echo "Error: JAR not found at $JAR_PATH"
    echo "Please run: cd /Users/bytedance/astRepo/java/java-parser && ./build.sh"
    exit 1
fi

# Run the parser with all arguments passed through
java -jar "$JAR_PATH" "$@"
