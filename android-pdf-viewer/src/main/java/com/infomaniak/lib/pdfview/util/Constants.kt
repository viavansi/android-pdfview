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
package com.infomaniak.lib.pdfview.util

object Constants {
    const val DEBUG_MODE: Boolean = false

    /** Between 0 and 1, the thumbnails quality (default 0.3). Increasing this value may cause performance decrease  */
    const val THUMBNAIL_RATIO: Float = 0.3f

    /** Minimum quality for printing  */
    const val THUMBNAIL_RATIO_PRINTING: Float = 0.75f

    /**
     * The size of the rendered parts (default 256)
     * Tinier : a little bit slower to have the whole page rendered but more reactive.
     * Bigger : user will have to wait longer to have the first visual results
     */
    const val PART_SIZE: Float = 256f

    /** Part of document above and below screen that should be preloaded, in dp  */
    const val PRELOAD_OFFSET: Int = 20

    object Cache {
        /** The size of the cache (number of bitmaps kept)  */
        const val CACHE_SIZE: Int = 120

        const val THUMBNAILS_CACHE_SIZE: Int = 8
    }

    object Pinch {
        const val MAXIMUM_ZOOM: Float = 100f

        const val  MINIMUM_ZOOM: Float = 0.3f
    }
}
