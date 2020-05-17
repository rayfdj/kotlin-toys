#!/usr/bin/env kotlin

@file:Repository("https://jcenter.bintray.com")
@file:DependsOn("org.jsoup:jsoup:1.13.1")

val lyricsPageDocument = org.jsoup.Jsoup.connect("http://99-bottles-of-beer.net/lyrics.html").get()

val lineSep = System.lineSeparator()

lyricsPageDocument.select("div[id=\"main\"] p").map {
    p -> p.textNodes().joinToString(separator=lineSep) { it.toString().trim() }
}.joinToString(separator=lineSep.repeat(2))
