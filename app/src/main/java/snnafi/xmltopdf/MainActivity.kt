package snnafi.xmltopdf

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintAttributes.Margins
import android.print.PrintAttributes.Resolution
import android.print.pdf.PrintedPdfDocument
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    lateinit var pdfDirPath: File
    lateinit var file: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (checkSelfPermission(
                REQUESTED_PERMISSIONS.get(
                    0
                ), PERMISSION_REQ_ID
            ) &&
            checkSelfPermission(
                REQUESTED_PERMISSIONS.get(
                    1
                ), PERMISSION_REQ_ID
            )
        ) {
            Toast.makeText(this, "GRANTED", Toast.LENGTH_LONG).show()
            pdfGenerate()
        }


    }

    fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        Log.i(
            LOG_TAG,
            "checkSelfPermission $permission $requestCode"
        )
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            )
            !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                REQUESTED_PERMISSIONS,
                requestCode
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(
            LOG_TAG,
            "onRequestPermissionsResult " + grantResults[0] + " " + requestCode
        )
        when (requestCode) {
            PERMISSION_REQ_ID -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(
                        LOG_TAG,
                        "Need permissions " + Manifest.permission.RECORD_AUDIO + "/" + Manifest.permission.CAMERA
                    )

                }
                Toast.makeText(this, "GRANTED", Toast.LENGTH_LONG).show()
            }
        }
    }




    fun pdfGenerate() {
        val printAttrs = PrintAttributes.Builder().setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(Resolution("snnafi", PRINT_SERVICE, 300, 500))
            .setMinMargins(Margins.NO_MARGINS).build()

        val document: PdfDocument = PrintedPdfDocument(this, printAttrs)
        val bmp = BitmapFactory.decodeResource(getResources(), R.drawable.hostpitalin);
        val logo = Bitmap.createScaledBitmap(bmp, 120, 120, false);

        val v = findViewById<RelativeLayout>(R.id.parent)
        v.layoutParams = FrameLayout.LayoutParams(
            441,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        v.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        v.layout(0, 0, v.measuredWidth, v.measuredHeight)

        val pageInfo = PageInfo.Builder(v.measuredWidth, v.measuredHeight, 1).create()
        // create a new page from the PageInfo
        val page: PdfDocument.Page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        canvas.drawBitmap(logo, 56f, 40f, paint) // axis
        v.draw(canvas)

        document.finishPage(page)

        try {
            pdfDirPath = File(application.getCacheDir(), "pdfs")
            pdfDirPath.mkdirs()
            file = File(pdfDirPath, "document.pdf")
            val contentUri: Uri =
                FileProvider.getUriForFile(this, "snnafi.xmltopdf.fileprovider", file)
            val os = FileOutputStream(file)
            document.writeTo(os)
            document.close()
            os.close()
            Log.d(LOG_TAG, "openGeneratedPDF")
            openGeneratedPDF()
        } catch (e: IOException) {
            throw RuntimeException("Error generating file", e)
        }
    }

    private fun openGeneratedPDF() {

        try {
            if (file.exists()) {
                val path = FileProvider.getUriForFile(
                    applicationContext, "snnafi.xmltopdf.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(path, "application/pdf")
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    applicationContext.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                           Log.d(LOG_TAG, e.localizedMessage)
                }
            } else {

            }
        } catch (exception: Exception) {
            Log.d(LOG_TAG, exception.localizedMessage)

        }
    }

    companion object {
        // Permissions
        private const val PERMISSION_REQ_ID = 22
        private val REQUESTED_PERMISSIONS =
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        private val LOG_TAG: String =
            MainActivity::class.java.getSimpleName()


    }
}