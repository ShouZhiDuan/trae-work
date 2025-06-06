#!/bin/bash
set -e

whereis openssl
#dnf list openssl*
#rpm -ql openssl
#rpm -ql openssl-libs
openssl  version
openssl  version -d

dir=/etc/pki/CA                     # Where everything is kept
certs=$dir/certs                    # Where the issued certs are kept
crl_dir=$dir/crl                    # Where the issued crl are kept
database=$dir/index.txt             # database index file.
new_certs_dir=$dir/newcerts         # default place for new certs.

certificate=$dir/certs/cacert.pem   # The CA certificate
serial=$dir/serial                  # The current serial number
crlnumber=$dir/crlnumber            # the current crl number
crl=$dir/crl.pem                    # The current CRL
private_key=$dir/private/cakey.key  # The private key

mkdir -pv $dir/{certs,crl,newcerts,private}

touch $dir/index.txt

echo "01" > $serial
echo "01" > $crlnumber

#openssl genrsa -out $private_enc_key -des3 2048
#openssl rsa -in $private_enc_key -out $private_key
#openssl rsa -in $private_key -pubkey -out $public_key
(umask 066; openssl genrsa -out $private_key  2048)
# https://country-code.cl/

openssl req -new -x509 -key $private_key -days 38000 -out $certificate <<EOF
CN
Zhejiang
Hangzhou
Nuowei
Certificate Authority
ca.nuowei.com
ca@nuowei.com
EOF

openssl x509 -in $certificate -noout -text