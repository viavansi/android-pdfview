package com.infomaniak.lib.pdfview;

public class PDFSpacing {
    /** Fixed spacing between pages in pixels */
    int pageSeparatorSpacing;
    /** Fixed spacing between pages in pixels */
    int startSpacing;
    /** Fixed spacing between pages in pixels */
    int endSpacing;
    /** Calculate spacing automatically so each page fits on it's own in the center of the view */
    boolean autoSpacing;

    public PDFSpacing(int pageSeparatorSpacing, int startSpacing, int endSpacing, boolean autoSpacing) {
        this.pageSeparatorSpacing = pageSeparatorSpacing;
        this.startSpacing = startSpacing;
        this.endSpacing = endSpacing;
        this.autoSpacing = autoSpacing;
    }
}
