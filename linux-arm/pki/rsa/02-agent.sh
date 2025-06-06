#!/bin/bash
set -e

################################
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
################################
agent=${1:-"ursa"}

base_dir=/etc/pki

agent_dir=${base_dir}/${agent}
agent_private_key=${agent_dir}/private/${agent}.key
agent_csr_file=${agent_dir}/certs/${agent}.csr
agent_crt_file=${agent_dir}/certs/${agent}.crt

mkdir -pv ${agent_dir}/{certs,crl,newcerts,private}

touch $agent_dir/index.txt
echo "01" > $agent_dir/serial
echo "01" > $agent_dir/crlnumber


#Country Name (2 letter code) [XX]:CN
#State or Province Name (full name) []:Zhejiang
#Locality Name (eg, city) [Default City]:Hangzhou
#Organization Name (eg, company) [Default Company Ltd]:Nuowei
#Organizational Unit Name (eg, section) []:Privacy-preserving Computing
#Common Name (eg, your name or your server's hostname) []:${agent}.nuowei.com
#Email Address []:${agent}@nuowei.com
(umask 066; openssl genrsa -out $agent_private_key  2048)
openssl req -new -key $agent_private_key -out $agent_csr_file <<EOF
CN
Zhejiang
Hangzhou
Nuowei
${agent}
${agent}.nuowei.com
${agent}@nuowei.com


EOF

openssl ca -config /usr/local/ssl/ssl/openssl-root.cnf -name CA_default_root -in $agent_csr_file -out $agent_crt_file -days 37000

openssl x509 -in $agent_crt_file -noout -text

openssl x509 -in $agent_crt_file -noout -subject
openssl x509 -in $agent_crt_file -noout -issuer
openssl x509 -in $agent_crt_file -noout -dates
openssl x509 -in $agent_crt_file -noout -serial
openssl x509 -in $agent_crt_file -noout -serial -subject
openssl ca -config /usr/local/ssl/ssl/openssl-root.cnf -name CA_default_root -status 01

#openssl ca -config /usr/local/ssl/ssl/openssl-root.cnf -name CA_default_root -revoke $agent_crt_file
#cat $database
## 生成证书吊销列表文件
#openssl ca -gencrl -out $crl
#cat $crlnumber
#cat $crl
#openssl crl -in $crl -noout -text
