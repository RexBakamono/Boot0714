<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<h1>hello world!</h1>

测试循环list<br>
<#list ls as p>
    ${p.id}-----${p.name}-----${p.password}<br>
</#list>

<br><br><br>

测试循环list中的map<br>
<#list list as map>
    <#list map?keys as key>
        <#if key="id">
            id:${map["id"]}
        </#if>
        <#if key="name">
            name:${map["name"]}
        </#if>
        <#if key="pass">
            pass:${map["pass"]}
        </#if>
        <br>
    </#list>
</#list>


</body>
</html>