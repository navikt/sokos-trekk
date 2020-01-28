if test -f /var/run/secrets/nais.io/certificate/srvtrekk/keystore
then
 CERT_PATH='/var/run/secrets/nais.io/certificate/srvtrekk/keystore-extracted'
 openssl base64 -d -A -in /var/run/secrets/nais.io/certificate/srvur-rf1358/keystore -out $CERT_PATH
 export SRVTREKK_CERTIFICATE_KEYSTORE=$CERT_PATH
 echo '- exporting SRVTREKK_CERTIFICATE_KEYSTORE'
fi

if test -f /var/run/secrets/nais.io/certificate/srvtrekk/keystorealias
then
    export  SRVTREKK_CERTIFICATE_KEYSTOREALIAS=$(cat /var/run/secrets/nais.io/certificate/srvtrekk/keystorealias)
    echo '- exporting SRVTREKK_CERTIFICATE_KEYSTOREALIAS'
fi

if test -f /var/run/secrets/nais.io/certificate/srvtrekk/keystorepassword
then
    export  SRVTREKK_CERTIFICATE_KEYSTORE_PASSWORD=$(cat /var/run/secrets/nais.io/certificate/srvtrekk/keystorepassword)
    echo '- exporting SRVTREKK_CERTIFICATE_KEYSTORE_PASSWORD'
fi

if test -f /var/run/secrets/nais.io/srvtrekk/password
then
    export  SRVTREKK_PASSWORD=$(cat /var/run/secrets/nais.io/srvtrekk/password)
    echo '- exporting SRVTREKK_PASSWORD'
fi