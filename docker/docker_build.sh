echo "build baize_tally image ......"

timestamp=$(date +'%Y%m%d%H%M%S')

name=baize_tally
version=${timestamp}

docker build -t ${name}:${version} .
echo "当前镜像版本为: " ${name}:${version}