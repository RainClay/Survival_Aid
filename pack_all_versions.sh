#!/data/data/com.termux/files/usr/bin/bash
# Pack all version mod jars into a single outer "SurvivalAid-all-versions.jar".
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
VERSIONS="1.21 1.21.1 1.21.2 1.21.3 1.21.4 1.21.5 1.21.6 1.21.10 1.21.11 26.1.2"

OUT="$DIR/SurvivalAid-all-versions.jar"
STAGE="$DIR/.pack_stage"
rm -rf "$STAGE"
mkdir -p "$STAGE/META-INF/jars"

# 1) Copy every built version jar into META-INF/jars/
for v in $VERSIONS; do
    j="$DIR/$v/build/libs/survival_aid-$v.jar"
    [ -f "$j" ] || j="$DIR/$v/build/libs/survival_aid-$v-1.0.1.jar"
    if [ -f "$j" ]; then
        cp "$j" "$STAGE/META-INF/jars/survival_aid-$v.jar"
        echo "  packed survival_aid-$v.jar"
    else
        echo "  MISSING $j"
    fi
done

# 2) Write the outer fabric.mod.json
cat > "$STAGE/fabric.mod.json" <<'JSON'
{
  "schemaVersion": 1,
  "id": "survival_aid",
  "version": "1.0.1",
  "name": "Carpet SurvivalAid (all versions)",
  "description": "Single-file bundle of SurvivalAid for MC 1.21 -> 1.21.11 + 26.1.2. Note: nested jars share the same mod id, so only one version is actually used at runtime - distribute per version instead.",
  "authors": ["Rain_Clay"],
  "contact": {},
  "license": "MIT",
  "environment": "*",
  "jars": [
    "META-INF/jars/survival_aid-1.21.jar",
    "META-INF/jars/survival_aid-1.21.1.jar",
    "META-INF/jars/survival_aid-1.21.2.jar",
    "META-INF/jars/survival_aid-1.21.3.jar",
    "META-INF/jars/survival_aid-1.21.4.jar",
    "META-INF/jars/survival_aid-1.21.5.jar",
    "META-INF/jars/survival_aid-1.21.6.jar",
    "META-INF/jars/survival_aid-1.21.10.jar",
    "META-INF/jars/survival_aid-1.21.11.jar",
    "META-INF/jars/survival_aid-26.1.2.jar"
  ],
  "depends": {
    "fabricloader": ">=0.15.0",
    "minecraft": "1.21 || 1.21.1 || 1.21.2 || 1.21.3 || 1.21.4 || 1.21.5 || 1.21.6 || 1.21.10 || 1.21.11 || 26.1.2",
    "java": ">=21",
    "fabric-api": "*",
    "carpet": "*"
  }
}
JSON

# 3) Zip into the outer jar
rm -f "$OUT"
cd "$STAGE"
jar cf "$OUT" META-INF fabric.mod.json
rm -rf "$STAGE"
echo "=== Created $OUT ==="
jar tf "$OUT"
