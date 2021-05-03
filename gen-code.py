
import os

codePath = "/Users/yanfeiwuji/IdeaProjects/keycloak-justauth/src/main/java/com/yfwj/justauth/social"
needTempPath = "/Users/yanfeiwuji/IdeaProjects/keycloak-justauth/temp"


codeTempPath = "/Users/yanfeiwuji/IdeaProjects/keycloak-justauth/gen-temp/WeChatOpenIdentityProviderFactory.java"

jpath = "/Users/yanfeiwuji/IdeaProjects/keycloak-justauth/src/main/java/com/yfwj/justauth/social/common/JustAuthKey.java"


def printO(result):
    for r in result:
        print("com.yfwj.justauth.social." +
              r["c"] + 'IdentityProviderFactory')


def printKicon(result):
    for r in result:
        print("kcLogoIdP-" +
              r["o"] + '=')


def getR():
    result = []
    with open(jpath) as f:

        for line in f.readlines():
            if ("\"" in line) and (not ("//" in line)):
                result.append(
                    {
                        "o": line.split("\"")[1],
                        "c": line.split("\"")[1].title().replace("_", ""),
                        "j": line.split("(")[0]
                    }
                )
    return result


def genClass(result):
    for r in result:
        with open(codeTempPath) as temp, open(codePath+"/" + r["c"] + 'IdentityProviderFactory.java', "w", encoding='utf-8') as need:
            for line in temp:
                new_line = line.replace("${C}", r["c"]).replace("${J}", r["j"])
                need.write(new_line)


def genTemp(result):
    need = """<div data-ng-include data-src="resourceUrl + '/partials/realm-identity-provider-social.html'"></div>"""
    for r in result:

        p = needTempPath + "/realm-identity-provider-" + r['o'] + ".html"
        open(p, mode="w+", encoding="utf-8")
        with open(needTempPath+"/realm-identity-provider-"+r['o']+".html", mode="w", encoding='utf-8') as t1, open(needTempPath+"/realm-identity-provider-"+r["o"]+"-ext.html", mode="w", encoding='utf-8') as t2:
            t1.write(need)


if __name__ == "__main__":
    result = getR()
    #genClass(result)
    #genTemp(result)
    #printO(result)
    printKicon(result)
