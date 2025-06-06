#!/bin/bash
set -e

openssl version

wget http://192.168.50.207:31200/package/openssl/openssl-OpenSSL_1_1_1wc.tar.gz
tar zxvf openssl-OpenSSL_1_1_1wc.tar.gz
cd openssl-OpenSSL_1_1_1wc
./config --prefix=/usr/local/ssl
make && make install

mv -f /usr/bin/openssl /usr/bin/openssl.old
mv -f /usr/include/openssl /usr/include/openssl.old

ln -s /usr/local/ssl/bin/openssl /usr/bin/openssl
ln -s /usr/local/ssl/include/openssl /usr/include/openssl

echo "/usr/local/ssl/lib">>/etc/ld.so.conf
ldconfig -v

openssl version -a

scp openssl-*.cnf root@192.168.50.70:/usr/local/ssl/ssl

scp *.sh root@192.168.50.70:/etc/pki/rsa/
scp -r root@192.168.50.70:/etc/pki/CA .
scp -r root@192.168.50.70:/etc/pki/ursa .
scp -r root@192.168.50.70:/etc/pki/node01 .
scp -r root@192.168.50.70:/etc/pki/user01 .

scp *.sh root@192.168.50.70:/etc/pki/sm2/
scp -r root@192.168.50.70:/etc/pki/CA_sm2 .
scp -r root@192.168.50.70:/etc/pki/ursa_sm2 .
scp -r root@192.168.50.70:/etc/pki/node01_sm2 .
scp -r root@192.168.50.70:/etc/pki/user01_sm2 .