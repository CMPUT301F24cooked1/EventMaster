package com.example.eventmaster;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Image {

    /**
     * Creates a bitmap of the profile picture that is based on the name of the User
     *
     * @param name the user's name
     * @param imageSize the size of the image
     * @param backgroundColor the profile picture color
     * @param textColor the text color
     *
     * @return the bitmap of the created profile picture for the user
     */
    public static Bitmap generateProfilePicture(String name, int imageSize, int backgroundColor, int textColor) {
        Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);

        // Draws a circle
        float radius = imageSize / 2f;
        canvas.drawCircle(radius, radius, radius, backgroundPaint);

        // Changes the color and sets text
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(imageSize * 0.5f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Get the first letter of the name (and capitalize it)
        String firstLetter = name.substring(0, 1).toUpperCase();

        // Measure the text size
        Rect textBounds = new Rect();
        textPaint.getTextBounds(firstLetter, 0, firstLetter.length(), textBounds);

        // Calculate the text position (centered)
        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));

        // Draw the letter on the canvas
        canvas.drawText(firstLetter, xPos, yPos, textPaint);

        return bitmap; // Return the bitmap
    }

}
