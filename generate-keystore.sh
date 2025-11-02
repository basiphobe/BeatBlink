#!/bin/bash

# Extract passwords from gradle.properties
KEYSTORE_PASSWORD=$(grep "KEYSTORE_PASSWORD=" gradle.properties | cut -d'=' -f2)
KEY_PASSWORD=$(grep "KEY_PASSWORD=" gradle.properties | cut -d'=' -f2)
KEY_ALIAS=$(grep "KEY_ALIAS=" gradle.properties | cut -d'=' -f2)

echo "Creating keystore with extracted passwords..."
echo "Alias: $KEY_ALIAS"

# Create keystore non-interactively with the passwords from gradle.properties
keytool -genkey -v \
  -keystore app/keystore.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias "$KEY_ALIAS" \
  -storepass "$KEYSTORE_PASSWORD" \
  -keypass "$KEY_PASSWORD" \
  -dname "CN=BeatBlink, OU=Development, O=BeatBlink, L=Unknown, ST=Unknown, C=US"

echo "Keystore created successfully!"