#!/bin/bash

# Build script for reni-java-parser

set -e

echo "Building java-parser..."

# Run Maven build
mvn clean package

# Check if build was successful
if [ $? -eq 0 ]; then
  echo "✓ Build successful!"
  echo ""
  echo "Executable JAR created at:"
  echo "  target/uniast-java-parser-1.0.0-SNAPSHOT.jar"
  echo ""
  echo "To run the parser:"
else
  echo "✗ Build failed!"
  exit 1
fi

mkdir -p $HOME/.abcoder/
cp target/uniast-java-parser-1.0.0-SNAPSHOT-jar-with-dependencies.jar $HOME/.abcoder/java-parser.jar

# ==================== 核心：安全获取【脚本自身真实路径】====================
# 无论在哪里执行，都会定位到脚本本身所在的目录
SCRIPT_PATH=$(readlink -f "$0")      # 脚本真实路径
SCRIPT_DIR=$(dirname "$SCRIPT_PATH") # 脚本所在目录
SCRIPT_NAME="java-parser.sh"         # 你的脚本名
COMMAND_NAME="java-parser"           # 你想全局使用的命令名
# ==========================================================================

# 目标软链路径（无需 sudo，全局可用）
TARGET="$HOME/.local/bin/$COMMAND_NAME"

mkdir -p ~/.local/bin
ln -sf "$SCRIPT_DIR/$SCRIPT_NAME" "$TARGET"
echo "✅ 全局命令创建完成！现在你可以在任何目录输入：$COMMAND_NAME /path/to/java/repo"
