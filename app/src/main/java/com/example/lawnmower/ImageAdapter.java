package com.example.lawnmower;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.provider.MediaStore;

public class ImageAdapter {

    private int height;
    private int width;
    private byte[] ImageBuilder;

    /**
     * This class is designed to assembly an image by adding multiple byte[] together, e.g.
     * packets sended over via udp.
     * @param width the width of the assembled image
     * @param height the height of the assembled image
     */
    public ImageAdapter(int width, int height) {
        this.height = height;
        this.width = width;
        ImageBuilder = new byte[0];
    }

    /**
     * Adds an byte[] part of the Image to the already transmitted parts or creates a new one
     * @param imgPart The part of the Image being added
     */
    public void addByteArrayToImage(Byte[] imgPart, int offset) {
        if(imgPart.length > 0) {
            int BufferSize = ImageBuilder.length + imgPart.length;
            int oldSize = ImageBuilder.length;
            byte[] tempPuffer = new byte[BufferSize];

            //Maybe OOB Exceptions
            for(int i :tempPuffer) {
                if(i < oldSize) {
                    tempPuffer[i] = ImageBuilder[i + offset];
                } else {
                    tempPuffer[i] = imgPart[i - oldSize + offset];
                }
            }

            ImageBuilder = tempPuffer;
        }
    }

    /**
     * Returns the completed Image
     * @return the Image
     */
    public Bitmap getImage() {

        // not sure if this way of parsing the Image will work, maybe it needs to be parsed differently
        Bitmap image = BitmapFactory.decodeByteArray(ImageBuilder, 0 , ImageBuilder.length);
        ImageBuilder = new byte[0];
        return image;
    }
}