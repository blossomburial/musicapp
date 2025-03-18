<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<h1> ILM </h1>
<#list products as product>
<div>
    <p><b>${product.title}</b> | <a href="/product/${product.id}">Подробнее...</a></p>
</div>
</#list>
<hr>
<h3>Создать новый товар</h3>
<form action="/product/create" method="post">
    название: <input type="text" name="title"/><br><br>
    альбом: <input type="text" name="album"/><br><br>
    длител: <input type="number" name="length"/><br><br>
    дата релиза: <input type="date" name="releasedate"/><br><br>
    просл: <input type="number" name="listenerscnt"/><br><br>
    испол: <input type="text" name="author"/><br><br>
    платф: <input type="text" name="platform"/><br><br>
    <input type="submit" value="Добавить товар"/>
</form>
</body>
</html>