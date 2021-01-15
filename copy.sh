mvn clean package
echo "building --- "
rm -rf ~/tools/keycloak-jb/keycloak/providers
mkdir ~/tools/keycloak-jb/keycloak/providers/
cp ./target/keycloak-justauth-12.0.1-jar-with-dependencies.jar ~/tools/keycloak-jb/keycloak/providers/

cp ./ui/phone-number-add.ftl ~/tools/keycloak-jb/keycloak/themes/base/login/
echo "copy success"
# shellcheck disable=SC2028
echo "\n"
sh ~/tools/keycloak-jb/keycloak/bin/standalone.sh




