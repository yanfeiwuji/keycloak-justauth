mvn clean package
echo "building --- "
rm -rf ~/work/tools/keycloak-21.1.1/providers
mkdir ~/work/tools/keycloak-21.1.1/providers/
cp ./target/keycloak-justauth-21.1.1-jar-with-dependencies.jar ~/work/tools/keycloak-21.1.1/providers/

# ui change is large
# cp ./ui/phone-number-add.ftl ~/work/tools/keycloak-21.1.1/themes/base/login/
echo "copy success"
# shellcheck disable=SC2028
echo "\n"
sh ~/work/tools/keycloak-21.1.1/bin/kc.sh start-dev




