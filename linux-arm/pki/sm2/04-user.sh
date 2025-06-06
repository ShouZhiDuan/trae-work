#!/bin/bash
set -e

agent=${1:-"ursa_sm2"}
node=${2:-"node01_sm2"}
user=${3:-"user01_sm2"}
base_dir=/etc/pki
dir=${base_dir}/${user}
user_private_key=${dir}/private/${user}.key
user_csr=${dir}/csr/${user}.csr
user_crt=${dir}/certs/${user}.crt
user_pem=${dir}/certs/${user}.pem

mkdir -pv ${dir}/{certs,csr,crl,newcerts,private,db}
touch ${dir}/db/index.txt

echo "01" > ${dir}/db/serial
echo "01" > ${dir}/db/crlnumber

(umask 066; openssl ecparam -genkey -name SM2 -out ${user_private_key})
openssl req -new -key ${user_private_key} -out ${user_csr} -sm3 -sigopt "sm2_id:1234567812345678" <<EOF
CN
Zhejiang
Hangzhou
Nuowei
${agent}
${user}.nuowei.com
${user}@nuowei.com


EOF

openssl ca -config /usr/local/ssl/ssl/openssl-sm2-node.cnf -extensions usr_cert -days 36800  -in ${user_csr} -out ${user_crt} -sigopt "sm2_id:1234567812345678" -sm2-id "1234567812345678" -md sm3

openssl x509 -in ${user_crt} -noout -text

openssl x509 -in ${user_crt} -pubkey -noout > ${user_pem}
