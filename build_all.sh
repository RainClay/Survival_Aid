#!/data/data/com.termux/files/usr/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
VERSIONS="1.21 1.21.1 1.21.2 1.21.3 1.21.4 1.21.5 1.21.6 1.21.10 1.21.11 26.1.2"
for v in $VERSIONS; do
    echo "=== Building $v ==="
    cd "$DIR" && ./gradlew -p "$v" build -x test --no-daemon 2>&1 || echo "!!! $v FAILED !!!"
    echo ""
done
echo "=== All done ==="
