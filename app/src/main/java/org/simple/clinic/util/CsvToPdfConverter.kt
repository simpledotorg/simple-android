package org.simple.clinic.util

import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.opencsv.CSVReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import javax.inject.Inject

class CsvToPdfConverter @Inject constructor() {

  fun convert(inputStream: InputStream, outputStream: OutputStream) {
    val entries = readCsvFile(inputStream)

    createPdfDocument(outputStream, entries)
  }

  private fun createPdfDocument(outputStream: OutputStream, entries: List<Array<String>>) {
    val pdfDoc = PdfDocument(PdfWriter(outputStream))
    val documentName = entries[0].joinToString()
    val numberOfColumns = entries[1].size

    val table = Table(numberOfColumns)
        .setFontSize(8f)

    entries
        .drop(1) // First element is the document name, therefore it should not be part of the table
        .flatMap { it.toList() }
        .forEach(table::addCell)

    Document(pdfDoc, PageSize.A4.rotate()).use { document ->
      document.add(Paragraph(documentName))
      document.add(table)
    }
  }

  private fun readCsvFile(inputStream: InputStream): List<Array<String>> {
    val reader = CSVReader(InputStreamReader(inputStream))
    return reader.readAll()
  }
}
