package com.infomaniak.lib.pdfview.model;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.util.HashMap;

/// method imported from old implementations

public class PdfBitmap implements Parcelable {
    private final Bitmap image;
    private final int height;
    private final int width;
    private final int pageNumber;
    private final int pdfX;
    private final int pdfY;
    private Type type;
    private boolean isRemovable;
    private HashMap<String, String> metadata;
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PdfBitmap createFromParcel(Parcel in) {
            return new PdfBitmap(in);
        }

        public PdfBitmap[] newArray(int size) {
            return new PdfBitmap[size];
        }
    };

    public PdfBitmap(Bitmap image, int width, int height, int pdfX, int pdfY, int page, Type type) {
        this.image = image;
        this.height = height;
        this.width = width;
        this.pdfX = pdfX;
        this.pdfY = pdfY;
        this.pageNumber = page;
        this.type = type;
        this.isRemovable = true;
        this.metadata = new HashMap<>();
    }

    public PdfBitmap(Parcel in) {
        this.image = (Bitmap)in.readParcelable(Bitmap.class.getClassLoader());
        this.height = in.readInt();
        this.width = in.readInt();
        this.pdfX = in.readInt();
        this.pdfY = in.readInt();
        this.pageNumber = in.readInt();
        String typeString = in.readString();
        if (typeString != null) {
            this.type = PdfBitmap.Type.valueOf(typeString);
        }

        this.isRemovable = in.readByte() != 0;
        in.readMap(this.metadata, HashMap.class.getClassLoader());
    }

    public Bitmap getBitmapImage() {
        return this.image;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.image, flags);
        dest.writeInt(this.height);
        dest.writeInt(this.width);
        dest.writeInt(this.pdfX);
        dest.writeInt(this.pdfY);
        dest.writeInt(this.pageNumber);
        dest.writeString(this.type.name());
        dest.writeByte((byte)(this.isRemovable ? 1 : 0));
        dest.writeMap(this.metadata);
    }

    public int getPdfX() {
        return this.pdfX;
    }

    public int getPdfY() {
        return this.pdfY;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isRemovable() {
        return this.isRemovable;
    }

    public void setIsRemovable(boolean isRemovable) {
        this.isRemovable = isRemovable;
    }

    public Rect getRect() {
        return new Rect(pdfX - width / 2, pdfY - height / 2, pdfX + width / 2, pdfY + height / 2);
    }

    public Rect getZoomedRect(float scaleX, float scaleY) {
        Rect bitmapRect = getRect();
        return new Rect(
                (int)(bitmapRect.left * scaleX),
                (int)(bitmapRect.top * scaleY),
                (int)(bitmapRect.right * scaleX),
                (int)(bitmapRect.bottom * scaleY)
        );
    }

    public HashMap<String, String> getMetadata() {
        return this.metadata;
    }

    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    public String toString() {
        String result = "page:" + this.pageNumber + ", x:" + this.pdfX + ", y:" + this.pdfY + ", width:" + this.width + ", height:" + this.height + ", type:" + this.type.name();
        return result;
    }

    public boolean equals(Object o) {
        boolean result = false;

        try {
            if (o == this) {
                result = true;
            } else if (o instanceof PdfBitmap) {
                PdfBitmap that = (PdfBitmap)o;
                boolean sameBitmaps = that.getBitmapImage().sameAs(this.image);
                result = that.getPdfX() == this.pdfX && that.getPdfY() == this.pdfY && that.getHeight() == this.height && that.getWidth() == this.width && that.getPageNumber() == this.pageNumber && sameBitmaps;
            }
        } catch (Exception e) {
            Log.e("PdfBitmap", e.getLocalizedMessage(), e);
        }

        return result;
    }

    public boolean intersect(Rect rect, int page) {
        return pageNumber == page && rect.intersect(this.getRect());
    }

    public boolean intersect(PdfBitmap bitmap) {
        return this.intersect(bitmap.getRect(), bitmap.pageNumber);
    }

    public boolean isInsideBounds(Rect rect) {
        return rect.contains(this.getRect());
    }

    public static enum Type {
        SIGNATURE,
        SIGNATURE_USER_IMAGE,
        IMAGE;

        private Type() {
        }
    }
}
