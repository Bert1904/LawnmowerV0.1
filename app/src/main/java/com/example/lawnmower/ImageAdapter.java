package com.example.lawnmower;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageAdapter {

    private int height;
    private int width;
    private byte[] ImageBuilder;

    public ImageAdapter(int width, int height) {
        this.height = height;
        this.width = width;
        ImageBuilder = new byte[0];
    }

    public void addByteArrayToImage(Byte[] imgPart, boolean lastPiece) {
        if(imgPart.length > 0) {
            int BufferSize = ImageBuilder.length + imgPart.length;
            int oldSize = ImageBuilder.length;
            byte[] tempPuffer = new byte[BufferSize];

            //Maybe OOB Exceptions
            for(int i = 0; i < BufferSize; i++) {
                if(i < oldSize) {
                    tempPuffer[i] = ImageBuilder[i];
                } else {
                    tempPuffer[i] = imgPart[i - oldSize];
                }
            }

            ImageBuilder = tempPuffer;
        }
    }

    public Bitmap getImage() {

        // not sure if this way of parsing the Image will work, maybe it needs to be parsed differently
        Bitmap image = BitmapFactory.decodeByteArray(ImageBuilder, 0 , ImageBuilder.length);
        ImageBuilder = new byte[0];
        return image;
    }
}