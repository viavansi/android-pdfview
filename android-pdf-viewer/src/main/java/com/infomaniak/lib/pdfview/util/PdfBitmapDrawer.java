package com.infomaniak.lib.pdfview.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.infomaniak.lib.pdfview.PDFView;
import com.infomaniak.lib.pdfview.model.PdfBitmap;
import com.shockwave.pdfium.util.Size;
import com.shockwave.pdfium.util.SizeF;

import java.util.ArrayList;
import java.util.Collection;

public class PdfBitmapDrawer {

    private Collection<PdfBitmap> pdfBitmaps;
    private final PDFView pdfView;
    private boolean allowOutOfBounds;
    private boolean allowOverlap;
    private Paint overlayPaint;

    public PdfBitmapDrawer(PDFView pdfView) {
        this.pdfBitmaps = new ArrayList<>();
        this.pdfView = pdfView;
        this.allowOutOfBounds = false;
        this.allowOverlap = false;
        this.overlayPaint = new Paint();
        setup();
    }

    public PdfBitmapDrawer(PDFView pdfView, boolean allowOverlap, boolean allowOutOfBounds) {
        this.pdfBitmaps = new ArrayList<>();
        this.pdfView = pdfView;
        this.allowOutOfBounds = allowOutOfBounds;
        this.allowOverlap = allowOverlap;
        this.overlayPaint = new Paint();
        setup();
    }

    private void setup() {
        overlayPaint.setFilterBitmap(true);
        overlayPaint.setAntiAlias(true);
    }

    public PDFView getPdfView() {
        return pdfView;
    }

    public Collection<PdfBitmap> getPdfBitmaps() {
        return pdfBitmaps;
    }

    public void setPdfBitmaps(Collection<PdfBitmap> pdfBitmaps) {
        this.pdfBitmaps = pdfBitmaps;
    }

    public Paint getOverlayPaint() {
        return overlayPaint;
    }

    public void setOverlayPaint(Paint overlayPaint) {
        this.overlayPaint = overlayPaint;
    }

    public boolean isAllowOutOfBounds() {
        return allowOutOfBounds;
    }

    public void setAllowOutOfBounds(boolean allowOutOfBounds) {
        this.allowOutOfBounds = allowOutOfBounds;
    }

    public boolean isAllowOverlap() {
        return allowOverlap;
    }

    public void setAllowOverlap(boolean allowOverlap) {
        this.allowOverlap = allowOverlap;
    }

    public void addBitmap(PdfBitmap newPdfBitmap) {
        boolean removed = false;
        if (!allowOverlap) {
            PdfBitmap bitmapPresent = getBitmapInPosition(newPdfBitmap.getRect(), newPdfBitmap.getPageNumber());
            if (bitmapPresent != null) {
                if (bitmapPresent.isRemovable()) {
                    pdfBitmaps.remove(bitmapPresent);
                    removed = true;
                } else {
                    return;
                }
            }
        }

        // Check if inside pdf
        if (!allowOutOfBounds) {
            SizeF pdfSize = pdfView.getPdfFile().getPageSizePoint(newPdfBitmap.getPageNumber());
            Rect pdfPageRect = new Rect(0, 0, (int) pdfSize.getWidth(), (int) pdfSize.getHeight());
            if (!newPdfBitmap.isInsideBounds(pdfPageRect)) {
                // Is not inside bounds of pdf page. Do nothing.
                return;
            }
        }

        if (!removed) {
            pdfBitmaps.add(newPdfBitmap);
        }
    }

    public PdfBitmap getBitmapInPosition(Rect rect, int page) {
        for (PdfBitmap bitmap : pdfBitmaps) {
            // Check if overlaps with previous bitmap
            if (bitmap.intersect(rect, page)) {
                return bitmap;
            }
        }
        return null;
    }

    public void redraw() {
        pdfView.invalidate();
    }

    public void removeBitmap(PdfBitmap pdfBitmap) {
        pdfBitmaps.remove(pdfBitmap);
    }

    public void drawBitmapsOnLayer(Canvas canvas, float pageWidth, float pageHeight, int page) {
        for (PdfBitmap pdfBitmap : pdfBitmaps) {
            if (pdfBitmap.getPageNumber() != page) {
                continue;
            }
            // Calculate scale from PDF-space to screen-space
            Size pdfPageSize = pdfView.getPdfFile().getPageSizePoint(page).toSize();
            float scaleX = pageWidth / pdfPageSize.getWidth();
            float scaleY = pageHeight / pdfPageSize.getHeight();

            Rect bitmapRect = pdfBitmap.getZoomedRectInvertedY(scaleX, scaleY, pdfPageSize.getHeight());

            // Draw the bitmap with correct scaling and positioning
            canvas.drawBitmap(
                    pdfBitmap.getBitmapImage(),
                    null,
                    bitmapRect,
                    overlayPaint
            );
        }
    }
}
