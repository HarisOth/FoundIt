package com.example.foundit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.util.ArrayList;

/**
 * ImageAdapter is a custom adapter used for displaying images in a GridView.
 * It loads image files from local storage, rotates them based on EXIF orientation,
 * and returns ImageViews to be displayed in a grid layout.
 */
public class ImageAdapter extends BaseAdapter {

    private Context context;

    private ArrayList<String> imagePaths;

    public ImageAdapter(Context context, ArrayList<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return imagePaths.size();
    }

    // Returns the image path at the specified position
    @Override
    public Object getItem(int position) {
        return imagePaths.get(position);
    }

    // Returns a unique ID for the item, which is just the position here
    @Override
    public long getItemId(int position) {
        return position;
    }

    // This method creates (or reuses) and returns a view (ImageView) for each item in the grid
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(300, 300)); // Set size of each image
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // Crop the image to fill the ImageView
        } else {
            imageView = (ImageView) convertView;
        }

        try {
            // Get the image file path at the current position
            String path = imagePaths.get(position);

            // Decode the image file into a Bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(path);

            // Read EXIF data to determine image orientation (needed to fix rotation)
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            // Matrix is used to rotate the bitmap
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90); // Rotate 90 degrees
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180); // Rotate 180 degrees
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270); // Rotate 270 degrees
                    break;
                default:
                    // No rotation needed
                    break;
            }

            // Create a new rotated bitmap using the matrix
            bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            // Set the rotated bitmap into the ImageView
            imageView.setImageBitmap(bitmap);

        } catch (IOException e) {
            // Handle exceptions when loading image or reading EXIF
            e.printStackTrace();
        }

        // Return the ImageView to be displayed in the GridView
        return imageView;
    }
}
