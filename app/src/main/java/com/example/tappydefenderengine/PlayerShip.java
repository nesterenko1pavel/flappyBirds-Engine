package com.example.tappydefenderengine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class PlayerShip {

    private Bitmap bitmap;
    private int x, y;
    private int speed;

    private Boolean boosting;

    private final int GRAVITY = -12;

    private int maxY;
    private int minY;

    private int MIN_SPEED = 1;
    private final int MAX_SPEED = 20;

    private Rect hitBox;

    private int screenX;

    private int shieldStrength;

    public PlayerShip(Context context, int screenX, int screenY) {
        x = 50;
        y = 50;
        speed = 1;

        this.screenX = screenX;

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ship);
        bitmap = Bitmap.createScaledBitmap(bitmap, 200, 105, true);
        boosting = false;

        minY = 0;
        maxY = screenY - bitmap.getHeight();

        hitBox = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());

        shieldStrength = 2;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getSpeed() {
        return speed;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBoosting() {
        boosting = true;
    }

    public void stopBoosting() {
        boosting = false;
    }

    public Rect getHitBox() {
        return hitBox;
    }

    public int getShieldStrength() {
        return shieldStrength;
    }

    public void setMIN_SPEED(int MIN_SPEED) {
        this.MIN_SPEED = MIN_SPEED;
    }

    public void update() {

        if (boosting && MIN_SPEED != 0) {
            speed += 2;
        } else {
            speed -= 5;
        }

        if (speed > MAX_SPEED) {
            speed = MAX_SPEED;
        }

        if (speed < MIN_SPEED) {
            speed = MIN_SPEED;
        }

        y -= speed + GRAVITY;

        if (y < minY) {
            y = minY;
        }

        if (y > maxY) {
            y = maxY;
        }

        hitBox.left = x;
        hitBox.top = y;
        hitBox.right = x + bitmap.getWidth();
        hitBox.bottom = y + bitmap.getHeight();

    }

    public void reduceShieldStrength() {
        shieldStrength--;
    }
}
