mvn clean package

docker build -t registry.cn-hangzhou.aliyuncs.com/yfwj/yfwj-keycloak:12.0.1 .

docker push registry.cn-hangzhou.aliyuncs.com/yfwj/yfwj-keycloak:12.0.1
