
具体使用看Dockerfile

justauth  http://www.justauth.cn/

docker 启动
docker run -p 8080:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin registry.cn-hangzhou.aliyuncs.com/yfwj/yfwj-keycloak:12.0.1

docker network create keycloak
docker run -p 80:8080 --name keycloak\
  -e KEYCLOAK_USER=admin \
  -e KEYCLOAK_PASSWORD=admin \
  -e DB_VENDOR=mysql \
  -e DB_ADDR=keycloak-mysql \
  -e DB_PORT=3306 \
  -e DB_DATABASE=keycloak \
  -e DB_USER=root \
  -e DB_PASSWORD=keycloak-root \
  --network keycloak \
  registry.cn-hangzhou.aliyuncs.com/yfwj/yfwj-keycloak:12.0.1

docker run -d -p 3306:3306 --name keycloak-mysql \
  -e MYSQL_ROOT_PASSWORD=keycloak-root  \
  -e MYSQL_DATABASE=keycloak \
  --network keycloak \
  mysql:5.7

## 2021-05-03
1. 升级justauth到 1.16.0 [justauth升级内容](https://justauth.wiki/update.html)
