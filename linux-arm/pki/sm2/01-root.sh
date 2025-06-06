#!/bin/bash
set -e

# https://help.aliyun.com/zh/iot/user-guide/generate-custom-certificate
whereis openssl
#dnf list openssl*
#rpm -ql openssl
#rpm -ql openssl-libs
openssl  version
openssl  version -d

base_dir=/etc/pki
dir=${base_dir}/CA_sm2                 # Where everything is kept
certs=${dir}/certs                    # Where the issued certs are kept
crl_dir=${dir}/crl                    # Where the issued crl are kept
database=${dir}/db/index.txt          # database index file.
new_certs_dir=${dir}/newcerts         # default place for new certs.

certificate=${dir}/certs/cacert.crt   # The CA certificate
cert_csr=${dir}/csr/cacert.csr
serial=${dir}/db/serial                  # The current serial number
crlnumber=${dir}/crl/crlnumber            # the current crl number
crl=${dir}/crl/ca.crl.pem                    # The current CRL
private_key=${dir}/private/cakey.key  # The private key

mkdir -pv ${dir}/{certs,csr,crl,newcerts,private,db}

touch ${dir}/db/index.txt

echo "01" > ${dir}/db/serial
echo "01" > ${dir}/db/crlnumber

(umask 066; openssl ecparam -genkey -name SM2 -out ${private_key})

# https://country-code.cl/
openssl req -new -config /usr/local/ssl/ssl/openssl-sm2-root.cnf -key ${private_key} -out ${cert_csr} -sm3 -sigopt "sm2_id:1234567812345678" <<EOF
CN
Zhejiang
Hangzhou
Nuowei
Certificate Authority
ca.nuowei.com
ca@nuowei.com


EOF

openssl ca -selfsign -config /usr/local/ssl/ssl/openssl-sm2-root.cnf -in ${cert_csr} -extensions v3_ca -days 38000 -out ${certificate}

openssl x509 -in ${certificate} -noout -text


