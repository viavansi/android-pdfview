/*
 * Infomaniak android-pdf-viewer
 * Copyright (C) 2024 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.infomaniak.lib.pdfpreview.sample

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import com.infomaniak.lib.pdfview.PDFView.Configurator
import com.infomaniak.lib.pdfview.listener.*
import com.infomaniak.lib.pdfview.model.PdfBitmap
import com.infomaniak.lib.pdfview.sample.R
import com.infomaniak.lib.pdfview.sample.databinding.ActivityMainBinding
import com.infomaniak.lib.pdfview.scroll.DefaultScrollHandle
import com.infomaniak.lib.pdfview.scroll.ScrollHandle
import com.infomaniak.lib.pdfview.util.FitPolicy
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfPasswordException

class PDFViewActivity : AppCompatActivity(), OnLongPressListener, OnDoubleTapListener, OnTapListener, OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener, OnErrorListener {

    private var uri: Uri? = null
    private var pageNumber = 0
    private var pdfFileName: String? = null

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: PDFViewViewModel by viewModels()

    private val pdfScrollHandle by lazy { getScrollHandle() }

    private val pdfBitmaps: Collection<PdfBitmap> = listOf(
        PdfBitmap(
            getYourCustomOverlayBitmap(),
            100,
            50,
            20,
            20,
            0,
            PdfBitmap.Type.SIGNATURE
        ),
        PdfBitmap(
            getYourCustomOverlayBitmap(),
            100,
            50,
            20,
            20,
            1,
            PdfBitmap.Type.SIGNATURE
        )
    )

    private fun getYourCustomOverlayBitmap(): Bitmap {
        // Creamos el bitmap una sola vez para mejorar el rendimiento

        val width = 1000
        val height = 1000

        // Crea un bitmap mutable
        val myDotOverlayBitmap = createBitmap(width, height)

        // Crea un Canvas para dibujar en el bitmap
        val canvas = Canvas(myDotOverlayBitmap)

        // Pinta el bitmap de negro
        canvas.drawColor(Color.BLACK)

        return myDotOverlayBitmap
    }

    private val selectFileResult = registerForActivityResult(StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == RESULT_OK) {
            activityResult.data?.let { intent ->
                uri = intent.data
                displayFromUri(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initializePDFView()
        binding.selectFile.setOnClickListener { pickFile() }
    }

    private fun pickFile() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), PERMISSION_CODE)
                return
            }
        }
        launchPicker()
    }

    private fun launchPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        runCatching {
            selectFileResult.launch(intent)
        }.onFailure { exception ->
            if (exception is ActivityNotFoundException) {
                Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializePDFView() {
        binding.pdfView.setBackgroundColor(Color.LTGRAY)
        if (uri != null) {
            displayFromUri(uri)
        } else {
            displayFromAsset()
        }
        title = pdfFileName
    }

    @SuppressLint("InflateParams")
    private fun getScrollHandle(): ScrollHandle = DefaultScrollHandle(this).apply {
        val view = layoutInflater.inflate(R.layout.handle_background, null);
        setPageHandleView(view, view.findViewById(R.id.pageIndicator))
        setTextColor(ResourcesCompat.getColor(resources, android.R.color.white, null))
        setTextSize(DEFAULT_TEXT_SIZE_DP)
        setHandleSize(HANDLE_WIDTH_DP, HANDLE_HEIGHT_DP)
        setHandlePaddings(0, HANDLE_PADDING_TOP_DP, 0, HANDLE_PADDING_BOTTOM_DP)
    }

    private fun displayFromAsset(password: String? = null) {
        pdfFileName = SAMPLE_FILE
        loadPDF(binding.pdfView.fromAsset(SAMPLE_FILE), password)
    }

    private fun displayFromUri(uri: Uri?, password: String? = null) {
        pdfFileName = viewModel.getFileName(contentResolver, uri)
        loadPDF(binding.pdfView.fromUri(uri), password)
    }

    private val overlayPaint = Paint().apply {
        isFilterBitmap = true      // suaviza la escala
        isAntiAlias = true         // antialiasing en dibujos
        // puedes ajustar más flags según necesites
    }

    private fun loadPDF(pdfConfigurator: Configurator, password: String? = null) {
        pdfConfigurator.defaultPage(pageNumber)
            .onPageChange(this)
            .onTap(this)
            .onLongPress(this)
            .onDoubleTap(this)
            .enableAnnotationRendering(true)
            .enableDoubletapZoom(false)
            .onLoad(this)
            .scrollHandle(pdfScrollHandle)
            .pageSeparatorSpacing(PDF_PAGE_SPACING_DP)
            .startEndSpacing(START_END_SPACING_DP, START_END_SPACING_DP)
            .zoom(MIN_ZOOM, MID_ZOOM, MAX_ZOOM)
            .onPageError(this)
            .pageFitPolicy(FitPolicy.BOTH)
            .password(password)
            .onDrawAll { canvas, pageWidth, pageHeight, page ->
                for (pdfBitmap in pdfBitmaps) {
                    if (pdfBitmap.pageNumber != page) {
                        continue
                    }
                    // Calculate scale from PDF-space to screen-space
                    val pdfPageSize = binding.pdfView.pdfFile.getPageSize(page)
                    val scaleX = pageWidth / pdfPageSize.width
                    val scaleY = pageHeight / pdfPageSize.height

                    // Compute destination rectangle in screen pixels
                    val left   = (pdfBitmap.pdfX * scaleX)
                    val top    = (pdfBitmap.pdfY * scaleY)
                    val right  = ((pdfBitmap.pdfX + pdfBitmap.width) * scaleX)
                    val bottom = ((pdfBitmap.pdfY + pdfBitmap.height) * scaleY)

                    // Draw the bitmap with correct scaling and positioning
                    canvas.drawBitmap(
                        pdfBitmap.bitmapImage,
                        null,
                        RectF(left, top, right, bottom),
                        overlayPaint
                    )
                }
            }
            .load()
    }

    private fun openPasswordDialog() {
        PasswordDialog(
            onPasswordEntered = { password -> displayFromUri(uri, password) },
        ).also { it.show(supportFragmentManager, "TAG") }
    }

    private fun printBookmarksTree(bookmarks: List<PdfDocument.Bookmark>, sep: String) {
        for (bookmark in bookmarks) {
            Log.e(TAG, String.format("%s %s, p %d", sep, bookmark.title, bookmark.pageIdx))
            if (bookmark.hasChildren()) printBookmarksTree(bookmark.children, "$sep-")
        }
    }

    override fun loadComplete(nbPages: Int) {
        val meta = binding.pdfView.documentMeta
        Log.e(TAG, "title = " + meta.title)
        Log.e(TAG, "author = " + meta.author)
        Log.e(TAG, "subject = " + meta.subject)
        Log.e(TAG, "keywords = " + meta.keywords)
        Log.e(TAG, "creator = " + meta.creator)
        Log.e(TAG, "producer = " + meta.producer)
        Log.e(TAG, "creationDate = " + meta.creationDate)
        Log.e(TAG, "modDate = " + meta.modDate)
        printBookmarksTree(binding.pdfView.tableOfContents, "-")
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        pageNumber = page
        title = String.format("%s %s / %s", pdfFileName, page + 1, pageCount)
    }

    /**
     * Listener for response to user permission request
     *
     * @param requestCode  Check that permission request code matches
     * @param permissions  Permissions that requested
     * @param grantResults Whether permissions granted
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchPicker()
        }
    }

    override fun onPageError(page: Int, t: Throwable) {
        Log.e(TAG, "Cannot load page $page")
    }

    override fun onError(throwable: Throwable?) {
        when (throwable) {
            is PdfPasswordException -> openPasswordDialog()
        }
    }

    companion object {
        private val TAG: String = PDFViewActivity::class.java.simpleName
        private const val PERMISSION_CODE = 42042
        private const val SAMPLE_FILE = "sample.pdf"
        private const val READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"
        private const val HANDLE_WIDTH_DP = 65
        private const val HANDLE_HEIGHT_DP = 40
        private const val HANDLE_PADDING_TOP_DP = 40
        private const val HANDLE_PADDING_BOTTOM_DP = 40
        private const val PDF_PAGE_SPACING_DP = 10
        private const val DEFAULT_TEXT_SIZE_DP = 16
        private const val START_END_SPACING_DP = 10
        private const val MIN_ZOOM = 0.93f
        private const val MID_ZOOM = 3.0f
        private const val MAX_ZOOM = 6.0f
    }

    override fun onLongPress(e: MotionEvent?) {
        if (e != null) {
            val pdfPoint: PointF = binding.pdfView.convertCoords(e)
            val page: Int = binding.pdfView.pageForCoords(e)
            Log.d("PDF Debug: LongPress", "page: "+ page +", pdf coords: ("+pdfPoint.x+", "+pdfPoint.y+"), screen coords: ("+e.x+", "+e.y+")")
        }
    }

    override fun onTap(e: MotionEvent?): Boolean {
        if (e != null) {
            val pdfPoint: PointF = binding.pdfView.convertCoords(e)
            val page: Int = binding.pdfView.pageForCoords(e)
            Log.d("PDF Debug: Tap", "page: "+ page +", pdf coords: ("+pdfPoint.x+", "+pdfPoint.y+"), screen coords: ("+e.x+", "+e.y+")")
        }
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        if (e != null) {
            val pdfPoint: PointF = binding.pdfView.convertCoords(e)
            val page: Int = binding.pdfView.pageForCoords(e)
            Log.d("PDF Debug: DoubleTap", "page: "+ page +", pdf coords: ("+pdfPoint.x+", "+pdfPoint.y+"), screen coords: ("+e.x+", "+e.y+")")
        }
        return true
    }
}
