package org.simple.clinic.util

import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.UnitValue
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
    val document = Document(pdfDoc, PageSize.A4.rotate())
    val documentName = entries[0].joinToString()
    val numberOfColumns = entries[1].size

    val table = Table(UnitValue.createPercentArray(numberOfColumns), true)
        .setFontSize(8f)

    document.add(Paragraph(documentName))
    document.add(table)

    // First element is the document name, therefore it should not be part of the table
    for (i in 1 until entries.size) {

      // Flushing content to doc every 100 rows to keep the memory footprint low to avoid OOM
      flushContentToTable(currentIndex = i, table = table)

      entries[i].forEach { cellContent ->
        val cell = Cell()
            .setKeepTogether(true)
            .add(Paragraph(cellContent))
            .setMargin(0f)

        table.addCell(cell)
      }
    }

    table.complete()
    document.close()
  }

  private fun flushContentToTable(currentIndex: Int, table: Table) {
    if (currentIndex % 100 == 0) {
      table.flush()
    }
  }

  private fun readCsvFile(inputStream: InputStream): List<Array<String>> {
    val reader = CSVReader(InputStreamReader(inputStream))
    return reader.readAll()
  }
}
