mvn clean package
echo "building --- "
rm -rf ~/tools/keycloak-jb/keycloak/providers
mkdir ~/tools/keycloak-jb/keycloak/providers/
cp ./target/keycloak-justauth2-12.0.1-jar-with-dependencies.jar ~/tools/keycloak-jb/keycloak/providers/
echo "copy success"
# shellcheck disable=SC2028
echo "\n"
sh ~/tools/keycloak-jb/keycloak/bin/standalone.sh




