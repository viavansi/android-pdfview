package com.infomaniak.lib.pdfview;

import com.infomaniak.lib.pdfview.util.FitPolicy;
import com.shockwave.pdfium.util.Size;

public class DisplayOptions {
    /** True if scrolling is vertical, else it's horizontal */
    boolean isVertical;
    /**
     * True if every page should fit separately according to the FitPolicy,
     * else the largest page fits and other pages scale relatively
     */
    boolean fitEachPage;
    Size viewSize;
    FitPolicy pageFitPolicy;
    PDFSpacing pdfSpacing;

    public DisplayOptions(
            boolean isVertical,
            PDFSpacing pdfSpacing,
            boolean fitEachPage,
            Size viewSize,
            FitPolicy pageFitPolicy) {
        this.isVertical = isVertical;
        this.pdfSpacing = pdfSpacing;
        this.fitEachPage = fitEachPage;
        this.viewSize = viewSize;
        this.pageFitPolicy = pageFitPolicy;
    }
}
