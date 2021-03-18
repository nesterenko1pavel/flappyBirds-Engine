package com.example.tappydefenderengine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.util.Random;

public class EnemyShip {

    private Bitmap bitmap;
    private int x, y;
    private int speed = 1;

    private int maxX;
    private int maxY;
    private int minX;
    private int minY;

    private Rect hitBox;

    public int overScreenState;

    public EnemyShip(Context context, int screenX, int screenY) {
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.enemy);
        bitmap = Bitmap.createScaledBitmap(bitmap, 100, 160, true);

        scaleBitmap(screenX);

        maxX = screenX;
        maxY = screenY;
        minX = 0;
        minY = 0;

        Random generator = new Random();
        speed = generator.nextInt(6) + 10;      // [10, 15]

        x = screenX;
        y = generator.nextInt(maxY) - bitmap.getHeight();

        hitBox = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());

        overScreenState = 0;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void update(int playerSpeed, int stop) {

        x -= playerSpeed;
        x -= speed;

        if (stop == 0) {
            if (x < minX - bitmap.getWidth()) {
                Random generator = new Random();
                speed = generator.nextInt(10) + 10;
                x = maxX;
                y = generator.nextInt(maxY) - bitmap.getHeight();
            }
        }
        if (x < minX - bitmap.getWidth()) {
            overScreenState = 1;
        }

        hitBox.left = x;
        hitBox.top = y;
        hitBox.right = x + bitmap.getWidth();
        hitBox.bottom = y + bitmap.getHeight();
    }

    public Rect getHitBox() {
        return hitBox;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void scaleBitmap(int x) {
        if (x < 1000) {
            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 3, bitmap.getHeight() / 3, false);
        } else if (x < 1200) {
            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, false);
        }
    }
}
