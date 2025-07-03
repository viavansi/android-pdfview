package com.infomaniak.lib.pdfview.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import com.infomaniak.lib.pdfview.PDFView;
import com.infomaniak.lib.pdfview.model.PdfBitmap;
import com.shockwave.pdfium.util.SizeF;

import java.util.ArrayList;
import java.util.Collection;

public class BitmapDrawer {

    private final Collection<PdfBitmap> pdfBitmaps;
    private final PDFView pdfView;
    private boolean allowOutOfBounds;
    private boolean allowOverlap;
    private Paint overlayPaint;

    public BitmapDrawer(PDFView pdfView) {
        this.pdfBitmaps = new ArrayList<>();
        this.pdfView = pdfView;
        this.allowOutOfBounds = false;
        this.allowOverlap = false;
        this.overlayPaint = new Paint();
        setup();
    }

    public BitmapDrawer(PDFView pdfView, boolean allowOverlap, boolean allowOutOfBounds) {
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
            for (PdfBitmap bitmap : pdfBitmaps) {
                // Check if overlaps with previous bitmap
                if (newPdfBitmap.intersect(bitmap)) {
                    if (!bitmap.isRemovable()) {
                        // Overlaps and can't remove. Do nothing.
                        return;
                    }
                    pdfBitmaps.remove(bitmap);
                    removed = true;
                    break;
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
        pdfView.invalidate();
    }

    public void removeBitmap(PdfBitmap pdfBitmap) {
        pdfBitmaps.remove(pdfBitmap);
        pdfView.invalidate();
    }

    public void drawBitmapsOnLayer(Canvas canvas, float pageWidth, float pageHeight, int page) {
        for (PdfBitmap pdfBitmap : pdfBitmaps) {
            if (pdfBitmap.getPageNumber() != page) {
                continue;
            }
            // Calculate scale from PDF-space to screen-space
            SizeF pdfPageSize = pdfView.getPdfFile().getPageSizePoint(page);
            float scaleX = pageWidth / pdfPageSize.getWidth();
            float scaleY = pageHeight / pdfPageSize.getHeight();

            Rect bitmapRect = pdfBitmap.getZoomedRect(scaleX, scaleY);

            overlayPaint.setFilterBitmap(true);
            overlayPaint.setAntiAlias(true);

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
