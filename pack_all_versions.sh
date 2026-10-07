#!/data/data/com.termux/files/usr/bin/bash
# Pack all version mod jars into a single outer "SurvivalAid-all-versions.jar"
# whose fabric.mod.json id is "survival_aid_wrapper".
#
# Both the source jar name and the target name embed the mod version, so
# neither is hardcoded here: the source is globbed out of build/libs/ and the
# version is read from each version's gradle.properties. Bumping mod_version
# therefore needs no edit to this script. The outer fabric.mod.json "jars"
# list is generated from the names actually packed, so it can never drift.
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
VERSIONS="1.21 1.21.1 1.21.2 1.21.3 1.21.4 1.21.5 1.21.6 1.21.10 1.21.11 26.1.2 26.2"

OUT="$DIR/SurvivalAid-all-versions.jar"
STAGE="$DIR/.pack_stage"
rm -rf "$STAGE"
mkdir -p "$STAGE/META-INF/jars"

if ! command -v jar >/dev/null 2>&1; then
    echo "!! 'jar' not on PATH -- set JAVA_HOME and add \$JAVA_HOME/bin" >&2
    exit 1
fi

# 1) Copy every built version jar into META-INF/jars/ with wrapper naming.
packed=""
missing=""
for v in $VERSIONS; do
    modver=$(grep -E '^mod_version' "$DIR/$v/gradle.properties" | cut -d= -f2 | tr -d ' \r')
    [ -z "$modver" ] && modver="1.0.3"

    if [ "$v" = "26.1.2" ]; then
        tgt="carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-${modver}.jar"
    else
        tgt="carpet-survival-aid-mc${v}-${modver}.jar"
    fi

    # build/libs may still hold jars from an older naming scheme; take the
    # non-sources jar with the newest mtime.
    j=$(ls -t "$DIR/$v"/build/libs/*.jar 2>/dev/null | grep -v -- '-sources.jar' | head -1)
    if [ -n "$j" ] && [ -f "$j" ]; then
        cp "$j" "$STAGE/META-INF/jars/$tgt"
        echo "  packed $tgt   <- $(basename "$j")"
        packed="$packed $tgt"
    else
        echo "  MISSING jar under $v/build/libs/" >&2
        missing="$missing $v"
    fi
done

if [ -n "$missing" ]; then
    echo "!! no jar for:$missing -- refusing to write a partial wrapper" >&2
    rm -rf "$STAGE"
    exit 1
fi

# 2) Write the outer wrapper fabric.mod.json (id: survival_aid_wrapper).
#    The "jars" list is generated from $packed, so it always matches disk.
{
    cat <<'JSON'
{
    "schemaVersion": 1,
    "id": "survival_aid_wrapper",
    "version": "1.0.0",
    "name": "Carpet SurvivalAid Wrapper",
    "description": "A wrapper that embeds Carpet SurvivalAid jars for multiple Minecraft versions.",
    "authors": [
        "SurvivalAid"
    ],
    "license": "Other",
    "environment": "*",
    "entrypoints": {
    },
    "depends": {
        "minecraft": "*",
        "fabricloader": ">=0.15.0"
    },
    "jars": [
JSON
    first=1
    for f in $packed; do
        [ $first -eq 1 ] || printf ',\n'
        first=0
        printf '        {\n            "file": "META-INF/jars/%s"\n        }' "$f"
    done
    printf '\n    ]\n}\n'
} > "$STAGE/fabric.mod.json"

# 3) Zip into the outer jar
rm -f "$OUT"
cd "$STAGE"
jar cf "$OUT" META-INF fabric.mod.json
echo "=== Created $OUT ==="
# leave $STAGE before deleting it, else a JVM launched from a deleted cwd dies
cd "$DIR"
rm -rf "$STAGE"
jar tf "$OUT"
