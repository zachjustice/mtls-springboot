CLIENT_KEYSTORE_DIR=../client/src/main/resources
SERVER_KEYSTORE_DIR=../server/src/main/resources
CLIENT_KEYSTORE=client-nonprod.jks
SERVER_KEYSTORE=server-nonprod.jks
JAVA_CA_CERTS=$JAVA_HOME/jre/lib/security/cacerts

# Generate a client and server RSA 2048 key pair
keytool -genkeypair -alias client -keyalg RSA -keysize 2048 -dname "CN=Client,OU=Client,O=PlumStep,L=San Francisco,S=CA,C=U" -keypass Newclient02 -keystore $CLIENT_KEYSTORE -storepass Newclient02
keytool -genkeypair -alias server -keyalg RSA -keysize 2048 -dname "CN=Server,OU=Server,O=PlumStep,L=San Francisco,S=CA,C=U" -keypass Newclient02 -keystore $SERVER_KEYSTORE -storepass Newclient02

# Export public certificates for both the client and server
keytool -exportcert -alias client -file client-public.cer -keystore $CLIENT_KEYSTORE -storepass Newclient02
keytool -exportcert -alias server -file server-public.cer -keystore $SERVER_KEYSTORE -storepass Newclient02

# Import the client and server public certificates into each others keystore
keytool -importcert -keystore $CLIENT_KEYSTORE -alias server-public-cert -file server-public.cer -storepass Newclient02 -noprompt
keytool -importcert -keystore $SERVER_KEYSTORE -alias client-public-cert -file client-public.cer -storepass Newclient02 -noprompt
cp $CLIENT_KEYSTORE $CLIENT_KEYSTORE_DIR
cp $SERVER_KEYSTORE $SERVER_KEYSTORE_DIR
