#!/bin/bash
set -e

agent=${1:-"ursa"}
node=${2:-"node01"}

file_name=${node}
base_dir=/etc/pki

node_dir=${base_dir}/${node}
node_private_key=${node_dir}/private/${file_name}.key
node_csr_file=${node_dir}/certs/${file_name}.csr
node_crt_file=${node_dir}/certs/${file_name}.crt
node_public_key=${node_dir}/certs/${file_name}.pem

mkdir -pv ${node_dir}/{certs,crl,newcerts,private}

touch $node_dir/index.txt
echo "01" > $node_dir/serial
echo "01" > $node_dir/crlnumber

(umask 066; openssl genrsa -out $node_private_key  2048)
openssl req -new -key $node_private_key -out $node_csr_file <<EOF
CN
Zhejiang
Hangzhou
Nuowei
${agent}
${node}.nuowei.com
${node}@nuowei.com


EOF

openssl ca -config /usr/local/ssl/ssl/openssl-agent.cnf -name CA_default_agent -in $node_csr_file -out $node_crt_file -days 36800

openssl x509 -in $node_crt_file -noout -text

openssl x509 -in $node_crt_file -pubkey -noout > $node_public_key