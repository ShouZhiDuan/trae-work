#!/bin/bash
set -e

agent=${1:-"ursa_sm2"}
node=${2:-"node01_sm2"}
base_dir=/etc/pki
dir=${base_dir}/${node}
node_private_key=${dir}/private/${node}.key
node_csr=${dir}/csr/${node}.csr
node_crt=${dir}/certs/${node}.crt
node_pem=${dir}/certs/${node}.pem

mkdir -pv ${dir}/{certs,csr,crl,newcerts,private,db}
touch ${dir}/db/index.txt

echo "01" > ${dir}/db/serial
echo "01" > ${dir}/db/crlnumber

(umask 066; openssl ecparam -genkey -name SM2 -out ${node_private_key})
openssl req -new -key ${node_private_key} -out ${node_csr} -sm3 -sigopt "sm2_id:1234567812345678" <<EOF
CN
Zhejiang
Hangzhou
Nuowei
${agent}
${node}.nuowei.com
${node}@nuowei.com


EOF

openssl ca -config /usr/local/ssl/ssl/openssl-sm2-agent.cnf -extensions server_cert -days 36800  -in ${node_csr} -out ${node_crt} -sigopt "sm2_id:1234567812345678" -sm2-id "1234567812345678" -md sm3

openssl x509 -in ${node_crt} -noout -text

openssl x509 -in ${node_crt} -pubkey -noout > ${node_pem}
