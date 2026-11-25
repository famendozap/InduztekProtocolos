package com.induztek.protocolos.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.induztek.protocolos.model.Equipo
import com.induztek.protocolos.model.Protocolo
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

object PdfProtocoloGenerator {

    fun generarYCompartirPdf(context: Context, equipo: Equipo?, protocolo: Protocolo) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paintHeader = Paint().apply {
            color = Color.rgb(15, 76, 129) // Deep blue primary
            textSize = 20f
            isFakeBoldText = true
        }

        val paintSubHeader = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 14f
            isFakeBoldText = true
        }

        val paintText = Paint().apply {
            color = Color.BLACK
            textSize = 11f
        }

        val paintLabel = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            isFakeBoldText = true
        }

        val paintLine = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = 40f

        // Header Title
        canvas.drawText("INDUZTEK PROTOCOLOS ELÉCTRICOS", 40f, y, paintHeader)
        y += 24f
        canvas.drawText("ACTA DE PRUEBAS Y PROTOCOLO TÉCNICO N° ${protocolo.id}", 40f, y, paintSubHeader)
        y += 15f
        canvas.drawLine(40f, y, 555f, y, paintLine)
        y += 25f

        // Equipment details section
        canvas.drawText("INFORMACIÓN DEL EQUIPO", 40f, y, paintSubHeader)
        y += 20f
        canvas.drawText("Código TAG:", 40f, y, paintLabel)
        canvas.drawText(equipo?.codigo ?: "N/A", 160f, y, paintText)

        canvas.drawText("Tipo de Equipo:", 300f, y, paintLabel)
        canvas.drawText(equipo?.tipo?.etiqueta ?: "N/A", 420f, y, paintText)
        y += 18f

        canvas.drawText("Marca / Modelo:", 40f, y, paintLabel)
        canvas.drawText("${equipo?.marca ?: "N/A"} / ${equipo?.modelo ?: "N/A"}", 160f, y, paintText)

        canvas.drawText("N° Serie:", 300f, y, paintLabel)
        canvas.drawText(equipo?.numeroSerie ?: "N/A", 420f, y, paintText)
        y += 18f

        canvas.drawText("Instalación / Cliente:", 40f, y, paintLabel)
        canvas.drawText(equipo?.instalacionCliente ?: "N/A", 160f, y, paintText)
        y += 25f
        canvas.drawLine(40f, y, 555f, y, paintLine)
        y += 25f

        // Protocol details section
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        canvas.drawText("DETALLES DEL PROTOCOLO DE PRUEBAS", 40f, y, paintSubHeader)
        y += 20f

        canvas.drawText("Fecha y Hora:", 40f, y, paintLabel)
        canvas.drawText(protocolo.fechaHora.format(formatter), 160f, y, paintText)

        canvas.drawText("Estado Protocolo:", 300f, y, paintLabel)
        canvas.drawText(protocolo.estado.etiqueta, 420f, y, paintText)
        y += 18f

        canvas.drawText("Técnico Terreno:", 40f, y, paintLabel)
        canvas.drawText(protocolo.tecnicoNombre.ifBlank { "N/A" }, 160f, y, paintText)

        canvas.drawText("Ingeniero Formalizador:", 300f, y, paintLabel)
        canvas.drawText(protocolo.ingenieroNombre ?: "Pendiente de formalizar", 420f, y, paintText)
        y += 18f

        if (protocolo.temperaturaAmbiente != null || protocolo.humedadRelativa != null) {
            canvas.drawText("Condiciones Amb.:", 40f, y, paintLabel)
            canvas.drawText("${protocolo.temperaturaAmbiente ?: "--"} °C | ${protocolo.humedadRelativa ?: "--"} % HR", 160f, y, paintText)
            y += 18f
        }

        if (!protocolo.instrumentosUsados.isNullOrBlank()) {
            canvas.drawText("Instrumentos:", 40f, y, paintLabel)
            canvas.drawText(protocolo.instrumentosUsados, 160f, y, paintText)
            y += 18f
        }

        canvas.drawText("Resultado General:", 40f, y, paintLabel)
        val paintResultado = Paint(paintText).apply {
            isFakeBoldText = true
            color = if (protocolo.resultadoGeneral.contains("NO CONFORME")) Color.RED else Color.rgb(0, 128, 0)
        }
        canvas.drawText(protocolo.resultadoGeneral, 160f, y, paintResultado)
        y += 25f

        canvas.drawLine(40f, y, 555f, y, paintLine)
        y += 25f

        // Measurements section
        canvas.drawText("MEDICIONES REGISTRADAS", 40f, y, paintSubHeader)
        y += 20f

        canvas.drawText("Parámetro", 40f, y, paintLabel)
        canvas.drawText("Valor Medido", 260f, y, paintLabel)
        canvas.drawText("Unidad", 440f, y, paintLabel)
        y += 15f
        canvas.drawLine(40f, y, 555f, y, paintLine)
        y += 15f

        for (medicion in protocolo.mediciones) {
            canvas.drawText(medicion.nombre, 40f, y, paintText)
            canvas.drawText(medicion.valor, 260f, y, paintText)
            canvas.drawText(medicion.unidad, 440f, y, paintText)
            y += 18f
        }

        if (protocolo.observaciones.isNotBlank()) {
            y += 15f
            canvas.drawText("Observaciones:", 40f, y, paintLabel)
            y += 15f
            canvas.drawText(protocolo.observaciones, 40f, y, paintText)
        }

        // Footer
        y = 800f
        canvas.drawLine(40f, y, 555f, y, paintLine)
        y += 15f
        canvas.drawText("Documento generado por Induztek Protocolos Eléctricos — Documento Oficial de Terreno", 40f, y, Paint().apply { color = Color.GRAY; textSize = 9f })

        pdfDocument.finishPage(page)

        // Save PDF file
        val pdfFile = File(context.cacheDir, "Protocolo_${protocolo.id}_${equipo?.codigo ?: "EQUIPO"}.pdf")
        try {
            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Acta de Protocolo N° ${protocolo.id} - ${equipo?.codigo}")
                putExtra(Intent.EXTRA_TEXT, "Adjunto el acta en PDF del protocolo N° ${protocolo.id} para el equipo ${equipo?.codigo}.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir Acta de Protocolo en PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
        }
    }
}
