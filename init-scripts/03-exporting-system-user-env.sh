if test -f /var/run/secrets/nais.io/srvtrekk/password
then
    export  SRVTREKK_PASSWORD=$(cat /var/run/secrets/nais.io/srvtrekk/password)
    echo '- exporting SRVTREKK_PASSWORD'
fi