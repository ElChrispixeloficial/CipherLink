#!/bin/bash
# Generate JavaFirm V2+V3 signing keystore for CipherLink
# Run this script once to generate the keystore

KEYSTORE_DIR="keystore"
KEYSTORE_FILE="$KEYSTORE_DIR/release.jks"
KEY_ALIAS="cipherlink"
KEYSTORE_PASSWORD="cipherlink"
KEY_PASSWORD="cipherlink"

echo "Generating CipherLink signing keystore..."

mkdir -p "$KEYSTORE_DIR"

keytool -genkeypair \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -keystore "$KEYSTORE_FILE" \
  -storepass "$KEYSTORE_PASSWORD" \
  -keypass "$KEY_PASSWORD" \
  -dname "CN=CipherLink, OU=Development, O=ElChrispixeloficial, L=Unknown, ST=Unknown, C=US"

if [ -f "$KEYSTORE_FILE" ]; then
  echo "Keystore generated successfully at $KEYSTORE_FILE"
  echo ""
  echo "IMPORTANT: Keep this keystore file secure!"
  echo "You will need it to sign future releases."
  echo ""
  echo "To build a signed release APK:"
  echo "  KEYSTORE_PASSWORD=$KEYSTORE_PASSWORD KEY_ALIAS=$KEY_ALIAS KEY_PASSWORD=$KEY_PASSWORD ./gradlew assembleRelease"
else
  echo "Failed to generate keystore"
fi
