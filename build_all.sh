#!/data/data/com.termux/files/usr/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
JDK21=/data/data/com.dsharnessmobile.shell/files/usr/lib/jvm/java-21-openjdk
JDK25=/data/data/com.dsharnessmobile.shell/files/usr/lib/jvm/java-25-openjdk
export GRADLE_USER_HOME=/data/user/0/com.dsharnessmobile.shell/files/home/.gradle
export JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=$DIR/.tmp"
mkdir -p "$DIR/.tmp"
VERSIONS="1.21 1.21.1 1.21.2 1.21.3 1.21.4 1.21.5 1.21.6 1.21.10 1.21.11 26.1.2 26.2"
for v in $VERSIONS; do
    if [[ "$v" == 26.* ]]; then jdk=$JDK25; else jdk=$JDK21; fi
    export JAVA_HOME="$jdk"
    export PATH="$jdk/bin:$PATH"
    echo "=== Building $v (JDK $(basename $jdk)) ==="
    cd "$DIR" && bash ./gradlew -p "$v" build -x test --no-daemon 2>&1 || echo "!!! $v FAILED !!!"
    echo ""
done
echo "=== All done ==="
