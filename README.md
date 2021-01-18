
keycloak 集成 Github、Gitee、微博、钉钉、百度、Coding、腾讯云开发者平台、OSChina、支付宝、QQ、微信、淘宝、Google、Facebook、抖音、领英、小米、微软、今日头条、Teambition、StackOverflow、Pinterest、人人、华为、企业微信、酷家乐、Gitlab、美团、饿了么和推特等第三方平台的授权登录。

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
