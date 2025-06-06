#!/bin/bash
set -e

agent=${1:-"ursa_sm2"}
base_dir=/etc/pki
dir=${base_dir}/${agent}
agent_private_key=${dir}/private/${agent}.key
agent_csr=${dir}/csr/${agent}.csr
agent_crt=${dir}/certs/${agent}.crt
agent_pem=${dir}/certs/${agent}.pem

mkdir -pv ${dir}/{certs,csr,crl,newcerts,private,db}
touch ${dir}/db/index.txt

echo "01" > ${dir}/db/serial
echo "01" > ${dir}/db/crlnumber

(umask 066; openssl ecparam -genkey -name SM2 -out ${agent_private_key})

openssl req -new -key ${agent_private_key} -out ${agent_csr} -sm3 -sigopt "sm2_id:1234567812345678"<<EOF
CN
Zhejiang
Hangzhou
Nuowei
${agent}
${agent}.nuowei.com
${agent}@nuowei.com


EOF

openssl ca -config /usr/local/ssl/ssl/openssl-sm2-root.cnf -extensions v3_intermediate_ca -days 37000  -in ${agent_csr} -out ${agent_crt} -sigopt "sm2_id:1234567812345678" -sm2-id "1234567812345678" -md sm3

openssl x509 -in ${agent_crt} -noout -text

openssl x509 -in ${agent_crt} -pubkey -noout > ${agent_pem}
