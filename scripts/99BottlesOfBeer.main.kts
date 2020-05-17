#!/usr/bin/env kotlin

@file:Repository("https://jcenter.bintray.com")
@file:DependsOn("org.jsoup:jsoup:1.13.1")

val lyricsPageDocument = org.jsoup.Jsoup.connect("http://99-bottles-of-beer.net/lyrics.html").get()
val newLine = System.lineSeparator()
lyricsPageDocument.select("div[id=\"main\"] p").map {
    p -> p.textNodes().joinToString(separator=newLine) { it.toString().trim() }
}.joinToString(separator=newLine.repeat(2))
