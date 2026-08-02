#!/data/data/com.termux/files/usr/bin/bash
# Pack all version mod jars into a single outer "SurvivalAid-all-versions.jar"
# whose fabric.mod.json id is "survival_aid_wrapper" (see the reference jar in the
# parent directory: /storage/emulated/0/ide/SurvivalAid-all-versions.jar).
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
VERSIONS="1.21 1.21.1 1.21.2 1.21.3 1.21.4 1.21.5 1.21.6 1.21.10 1.21.11 26.1.2"

OUT="$DIR/SurvivalAid-all-versions.jar"
STAGE="$DIR/.pack_stage"
rm -rf "$STAGE"
mkdir -p "$STAGE/META-INF/jars"

# 1) Copy every built version jar into META-INF/jars/ with wrapper naming.
#    target name: carpet-survival-aid-mc<version>-1.0.1.jar  (26.1.2 has its own suffix)
for v in $VERSIONS; do
    if [ "$v" = "26.1.2" ]; then
        tgt="carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar"
    else
        tgt="carpet-survival-aid-mc${v}-1.0.1.jar"
    fi
    # built jar name: 1.21 is survival_aid-1.21-1.0.1.jar, others survival_aid-<version>.jar
    j="$DIR/$v/build/libs/survival_aid-$v.jar"
    [ -f "$j" ] || j="$DIR/$v/build/libs/survival_aid-$v-1.0.1.jar"
    if [ -f "$j" ]; then
        cp "$j" "$STAGE/META-INF/jars/$tgt"
        echo "  packed $tgt"
    else
        echo "  MISSING $j"
    fi
done

# 2) Write the outer wrapper fabric.mod.json (id: survival_aid_wrapper)
cat > "$STAGE/fabric.mod.json" <<'JSON'
{
    "schemaVersion": 1,
    "id": "survival_aid_wrapper",
    "version": "1.0.0",
    "name": "Carpet SurvivalAid Wrapper",
    "description": "A wrapper that embeds Carpet SurvivalAid jars for multiple Minecraft versions.",
    "authors": [
        "SurvivalAid"
    ],
    "license": "MIT",
    "environment": "*",
    "entrypoints": {
    },
    "depends": {
        "minecraft": "*",
        "fabricloader": ">=0.15.0"
    },
    "jars": [
        {
            "file": "META-INF/jars/carpet-survival-aid-mc1.21-1.0.1.jar"
        },
        {
            "file": "META-INF/jars/carpet-survival-aid-mc1.21.1-1.0.1.jar"
        },
        {
            "file": "META-INF/jars/carpet-survival-aid-mc1.21.2-1.0.1.jar"
        },
        {
            "file": "META-INF/jars/carpet-survival-aid-mc1.21.3-1.0.1.jar"
        },
        {
            "file": "META-INF/jars/carpet-survival-aid-mc1.21.4-1.0.1.jar"
        },
        {
            "file": "META-INF/jars/carpet-survival-aid-mc1.21.5-1.0.1.jar"
        },
        {
            "file": "META-INF/jars/carpet-survival-aid-mc1.21.6-1.0.1.jar"
        },
        {
            "file": "META-INF/jars/carpet-survival-aid-mc1.21.10-1.0.1.jar"
        },
        {
            "file": "META-INF/jars/carpet-survival-aid-mc1.21.11-1.0.1.jar"
        },
        {
            "file": "META-INF/jars/carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar"
        }
    ]
}
JSON

# 3) Zip into the outer jar
rm -f "$OUT"
cd "$STAGE"
jar cf "$OUT" META-INF fabric.mod.json
echo "=== Created $OUT ==="
# leave $STAGE before deleting it, else a JVM launched from a deleted cwd dies
cd "$DIR"
rm -rf "$STAGE"
jar tf "$OUT"
