package snnafi.xmltopdf;


import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    File pdfDirPath;
    File file;
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                        checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
                    pdfGenerate();
                }

            }
        });
    }

    private void pdfGenerate() {
        PrintAttributes printAttrs = new PrintAttributes.Builder().setColorMode(PrintAttributes.COLOR_MODE_COLOR).setMediaSize(PrintAttributes.MediaSize.NA_LETTER).setResolution(new PrintAttributes.Resolution("snnafi", PRINT_SERVICE, 300, 300)).setMinMargins(PrintAttributes.Margins.NO_MARGINS).build();
        PdfDocument document = new PrintedPdfDocument(this, printAttrs);
        // crate a page description
        RelativeLayout layout = findViewById(R.id.parent);
        layout.setLayoutParams(new FrameLayout.LayoutParams(441, RelativeLayout.LayoutParams.MATCH_PARENT));
        layout.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        layout.layout(0, 0, layout.getMeasuredWidth(), layout.getMeasuredHeight());
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(layout.getMeasuredWidth(), layout.getMeasuredHeight(), 1).create();

        PdfDocument.Page page = document.startPage(pageInfo);
        layout.draw(page.getCanvas());
        document.finishPage(page);

        try {
            pdfDirPath = new File(getApplication().getCacheDir(), "pdfs");
            file = new File(pdfDirPath, "document.pdf");
            Uri contentUri = FileProvider.getUriForFile(this, "snnafi.xmltopdf.fileprovider", file);
            FileOutputStream os = new FileOutputStream(file);
            document.writeTo(os);
            document.close();
            os.close();
            openGeneratedPDF();

        } catch (IOException e) {
            throw new RuntimeException("Error generating file", e);
        }
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    REQUESTED_PERMISSIONS,
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Log.i(LOG_TAG, "Need permissions " + Manifest.permission.READ_EXTERNAL_STORAGE + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            ///
        }
    }

    private void openGeneratedPDF() {
        try {
            if (file.exists()) {
                Uri path = FileProvider.getUriForFile(getApplicationContext(), "snnafi.xmltopdf.fileprovider", file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(path, "application/pdf");

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {

                }
            } else {

            }
        } catch (Exception exception) {

        }

    }

}
