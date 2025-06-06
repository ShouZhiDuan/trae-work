#!/bin/bash
set -e

agent=${1:-"ursa"}
node=${2:-"node01"}
user=${3:-"user01"}

file_name=${user}
base_dir=/etc/pki

user_dir=${base_dir}/${user}
user_private_key=${user_dir}/private/${file_name}.key
user_csr_file=${user_dir}/certs/${file_name}.csr
user_crt_file=${user_dir}/certs/${file_name}.crt
user_public_key=${user_dir}/certs/${file_name}.pem

mkdir -pv ${user_dir}/{certs,crl,newcerts,private}

touch $user_dir/index.txt
echo "01" > $user_dir/serial
echo "01" > $user_dir/crlnumber

(umask 066; openssl genrsa -out $user_private_key  2048)
openssl req -new -key $user_private_key -out $user_csr_file <<EOF
CN
Zhejiang
Hangzhou
Nuowei
${agent}
${user}.nuowei.com
${user}@nuowei.com


EOF

openssl ca -config /usr/local/ssl/ssl/openssl-node.cnf -name CA_default_node -in $user_csr_file -out $user_crt_file -days 36600

openssl x509 -in $user_crt_file -noout -text

openssl x509 -in $user_crt_file -pubkey -noout > $user_public_key