package dev.rayfdj.kotlinutils.springer

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

data class Book(val title: String, val author: String, val edition: String, val year: String,
                val category: String, val url: String) {
    fun suggestedFileName(): String { return "$title, $edition - $author.pdf" }
}

fun extractBooksFromExcelFile(xlsxFile: File): List<Book> {
    // drop(1): skip the first row because it contains the headers
    return WorkbookFactory.create(xlsxFile).getSheetAt(0).drop(1).map {
        Book(it.getCell(0).stringCellValue, it.getCell(1).stringCellValue,
                it.getCell(2).stringCellValue, it.getCell(4).numericCellValue.toString(),
                it.getCell(11).stringCellValue, it.getCell(18).stringCellValue)
    }
}

fun deriveFullLocalPathForBook(downloadFolder: String, book: Book): Path {
    val fullLocalFileName = arrayOf(
            downloadFolder,
            book.category,
            book.suggestedFileName()).joinToString(separator = File.separator)
    return Paths.get(fullLocalFileName)
}

fun createDirectoriesAndFile(fullLocalFilePath: Path) {
    Files.createDirectories(fullLocalFilePath.parent)
    if(!Files.exists(fullLocalFilePath)) { Files.createFile(fullLocalFilePath) }
}

fun getBookDownloadURL(book: Book): URL {
    val bookPage = Jsoup.connect(book.url).get()
    val bookCanonicalURL = bookPage.select("link[rel=canonical]").attr("href")

    val bookCanonicalPage = Jsoup.connect(bookCanonicalURL).get()
    val bookPDFRelativeURL = bookCanonicalPage.select("a[href^=\"/content/pdf/\"]").attr("href")

    return URL("https://link.springer.com${bookPDFRelativeURL}")
}

fun downloadAndSaveBook(bookDownloadURL: URL, fullLocalFilePath: Path) {
    Channels.newChannel(bookDownloadURL.openStream()).use { inChannel ->
        FileOutputStream(fullLocalFilePath.toFile()).channel.use { outChannel ->
            print("Saving $bookDownloadURL to $fullLocalFilePath... ")
            outChannel.transferFrom(inChannel, 0, Long.MAX_VALUE)
            println("DONE.")
        }
    }
}

fun main(args: Array<String>) {
    if(args.size != 2) {
        println("Please pass <full_path_to_springer_excel_file> and <full_path_to_download_folder")
        kotlin.system.exitProcess(-1)
    }
    val (excelFile, downloadFolder) = args

    val books = extractBooksFromExcelFile(File(excelFile))

    books.forEach { book ->
        val fullLocalFilePath = deriveFullLocalPathForBook(downloadFolder, book)
        createDirectoriesAndFile(fullLocalFilePath)
        val bookDownloadURL = getBookDownloadURL(book)
        downloadAndSaveBook(bookDownloadURL, fullLocalFilePath)
    }
}
