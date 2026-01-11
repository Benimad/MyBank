package com.example.mybank.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.mybank.data.model.Account
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class BankStatementGenerator(private val context: Context) {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    fun generateStatement(
        account: Account,
        transactions: List<Transaction>,
        startDate: Long,
        endDate: Long
    ): Result<File> {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                textSize = 24f
                isFakeBoldText = true
            }

            val headerPaint = Paint().apply {
                textSize = 16f
                isFakeBoldText = true
            }

            val normalPaint = Paint().apply {
                textSize = 12f
            }

            val smallPaint = Paint().apply {
                textSize = 10f
            }

            var yPos = 50f

            canvas.drawText("MyBank", 50f, yPos, titlePaint)
            yPos += 30f
            canvas.drawText("Account Statement", 50f, yPos, headerPaint)
            yPos += 40f

            canvas.drawText("Account Name: ${account.accountName}", 50f, yPos, normalPaint)
            yPos += 20f
            canvas.drawText("Account Number: ${account.accountNumber}", 50f, yPos, normalPaint)
            yPos += 20f
            canvas.drawText("Account Type: ${account.accountType.name}", 50f, yPos, normalPaint)
            yPos += 20f
            canvas.drawText(
                "Statement Period: ${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}",
                50f,
                yPos,
                normalPaint
            )
            yPos += 20f
            canvas.drawText(
                "Current Balance: ${currencyFormat.format(account.balance)}",
                50f,
                yPos,
                normalPaint
            )
            yPos += 40f

            canvas.drawText("Transactions", 50f, yPos, headerPaint)
            yPos += 30f

            val creditTotal = transactions.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
            val debitTotal = transactions.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }

            canvas.drawText("Total Credits: ${currencyFormat.format(creditTotal)}", 50f, yPos, normalPaint)
            yPos += 20f
            canvas.drawText("Total Debits: ${currencyFormat.format(debitTotal)}", 50f, yPos, normalPaint)
            yPos += 30f

            canvas.drawText("Date", 50f, yPos, smallPaint)
            canvas.drawText("Description", 150f, yPos, smallPaint)
            canvas.drawText("Type", 350f, yPos, smallPaint)
            canvas.drawText("Amount", 450f, yPos, smallPaint)
            yPos += 5f
            canvas.drawLine(50f, yPos, 545f, yPos, smallPaint)
            yPos += 15f

            transactions.sortedByDescending { it.timestamp }.forEach { transaction ->
                if (yPos > 800) {
                    pdfDocument.finishPage(page)
                    val newPage = pdfDocument.startPage(pageInfo)
                    yPos = 50f
                }

                canvas.drawText(dateFormat.format(transaction.timestamp), 50f, yPos, smallPaint)
                canvas.drawText(
                    transaction.description.take(25),
                    150f,
                    yPos,
                    smallPaint
                )
                canvas.drawText(transaction.type.name, 350f, yPos, smallPaint)
                canvas.drawText(
                    "${if (transaction.type == TransactionType.DEBIT) "-" else "+"}${
                        currencyFormat.format(
                            transaction.amount
                        )
                    }",
                    450f,
                    yPos,
                    smallPaint
                )
                yPos += 18f
            }

            pdfDocument.finishPage(page)

            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "MyBank"
            )
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val fileName = "statement_${account.accountNumber}_${System.currentTimeMillis()}.pdf"
            val file = File(dir, fileName)

            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
