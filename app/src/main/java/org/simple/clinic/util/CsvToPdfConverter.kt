package org.simple.clinic.util

import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.opencsv.CSVReader
import java.io.File
import java.io.FileReader
import javax.inject.Inject

class CsvToPdfConverter @Inject constructor() {

  fun convert(inputFile: File, outputFile: File) {
    val entries = readCsvFile(inputFile)

    createPdfDocument(outputFile, entries)
  }

  private fun createPdfDocument(outputFile: File, entries: List<Array<String>>) {
    val pdfDoc = PdfDocument(PdfWriter(outputFile))
    val documentName = entries[0].joinToString()
    val numberOfColumns = entries[1].size

    val table = Table(numberOfColumns)
        .setFontSize(8f)

    entries
        .drop(1) // First element is the document name, therefore it should not be part of the table
        .flatMap { it.toList() }
        .forEach(table::addCell)

    Document(pdfDoc, PageSize.A4.rotate()).run {
      add(Paragraph(documentName))
      add(table)
      close()
    }
  }

  private fun readCsvFile(file: File): List<Array<String>> {
    val reader = CSVReader(FileReader(file))
    return reader.readAll()
  }
}
