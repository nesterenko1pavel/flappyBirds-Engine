package com.example.tappydefenderengine;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class TDView extends SurfaceView implements Runnable {

    private Context context;
    private boolean gameEnded;

    private float distanceRemaining;
    private long timeTaken;
    private long timeStarted;
    private long fastestTime;

    volatile boolean playing;
    Thread gameThread = null;

    private PlayerShip player;
    private EnemyShip enemy1;
    private EnemyShip enemy2;
    private EnemyShip enemy3;
    public ArrayList<SpaceDust> dustList = new ArrayList<SpaceDust>();

    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder ourHolder;

    private int screenX;
    private int screenY;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private int stopEnemy;
    private int fixedY;

    public TDView(Context context, int x, int y) {
        super(context);
        this.context = context;

        ourHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;

        prefs = context.getSharedPreferences("HiScores", context.MODE_PRIVATE);
        editor = prefs.edit();
        fastestTime = prefs.getLong("fastestTime", 1000000);

        fixedY = -1;
        stopEnemy = 0;

        int numSpecs = 40;
        for (int i = 0; i < numSpecs; i++) {
            SpaceDust spec = new SpaceDust(screenX, screenY);
            dustList.add(spec);
        }

        startGame();
    }

    @Override
    public void run() {
        while (playing) {
            update();
            draw();
            control();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (stopEnemy == 0) player.stopBoosting();
                break;
            case MotionEvent.ACTION_DOWN:
                if (stopEnemy == 0) player.setBoosting();
                if (gameEnded) {
                    stopEnemy = 0;
                    animEnd(0);
                    startGame();
                }
                break;
        }

        return true;
    }

    private void update() {

        boolean hitDetected = false;

        if (Rect.intersects(player.getHitBox(), enemy1.getHitBox())) {
            hitDetected = true;
            enemy1.setX(-enemy1.getBitmap().getWidth());
        }

        if (Rect.intersects(player.getHitBox(), enemy2.getHitBox())) {
            hitDetected = true;
            enemy2.setX(-enemy2.getBitmap().getWidth());
        }

        if (Rect.intersects(player.getHitBox(), enemy3.getHitBox())) {
            hitDetected = true;
            enemy3.setX(-enemy3.getBitmap().getWidth());
        }

        if (hitDetected) {
            player.reduceShieldStrength();
            if (player.getShieldStrength() < 0) {
                stopEnemy = 1;
                animEnd(1);
                gameEnded = true;
            }
        }

        player.update();

        enemy1.update(player.getSpeed(), stopEnemy);
        enemy2.update(player.getSpeed(), stopEnemy);
        enemy3.update(player.getSpeed(), stopEnemy);

        for (SpaceDust sd : dustList) {
            sd.update(player.getSpeed());
        }

        if (!gameEnded) {
            distanceRemaining -= player.getSpeed();
            timeTaken = System.currentTimeMillis() - timeStarted;
        }

        if (distanceRemaining <= 0) {
            if (timeTaken < fastestTime) {
                editor.putLong("fastestTime", timeTaken);
                editor.commit();
                fastestTime = timeTaken;
            }

            distanceRemaining = 0;
            stopEnemy = 1;
            animEnd(1);
            gameEnded = true;
        }

        if (stopEnemy == 1) {
            animEnd(1);
        } else {
            player.setX(50);
        }
    }

    private void draw() {
        if (ourHolder.getSurface().isValid()) {
            canvas = ourHolder.lockCanvas();
            canvas.drawColor(Color.argb(255, 0, 0, 0));

            paint.setColor(Color.argb(255, 255, 255, 255));

//            canvas.drawRect(player.getHitBox().left, player.getHitBox().top, player.getHitBox().right, player.getHitBox().bottom, paint);
//            canvas.drawRect(enemy1.getHitBox().left, enemy1.getHitBox().top, enemy1.getHitBox().right, enemy1.getHitBox().bottom, paint);
//            canvas.drawRect(enemy2.getHitBox().left, enemy2.getHitBox().top, enemy2.getHitBox().right, enemy2.getHitBox().bottom, paint);
//            canvas.drawRect(enemy3.getHitBox().left, enemy3.getHitBox().top, enemy3.getHitBox().right, enemy3.getHitBox().bottom, paint);

            paint.setColor(Color.argb(255, 255, 255, 255));
            for (SpaceDust sd : dustList) {
                canvas.drawPoint(sd.getX(), sd.getY(), paint);
            }

            canvas.drawBitmap(player.getBitmap(), player.getX(), player.getY(), paint);
            canvas.drawBitmap(enemy1.getBitmap(), enemy1.getX(), enemy1.getY(), paint);
            canvas.drawBitmap(enemy2.getBitmap(), enemy2.getX(), enemy2.getY(), paint);
            canvas.drawBitmap(enemy3.getBitmap(), enemy3.getX(), enemy3.getY(), paint);

            if (!gameEnded) {
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(25);
                canvas.drawText("Fastest: " + formatTime(fastestTime) + "s", 10, 20, paint);
                canvas.drawText("Time: " + formatTime(timeTaken) + "s", screenX / 2, 20, paint);
                canvas.drawText("Distance: " + distanceRemaining / 1000 + "km", screenX / 3, screenY - 20, paint);
                canvas.drawText("Shield: " + player.getShieldStrength(), 10, screenY - 20, paint);
                canvas.drawText("Speed: " + player.getSpeed() * 60 + "mps", (screenX / 3) * 2, screenY - 20, paint);
            } else {
                paint.setTextSize(80);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("Game over", screenX / 2, 100, paint);
                paint.setTextSize(25);
                canvas.drawText("Fastest: " + formatTime(fastestTime) + "s", screenX / 2, 160, paint);
                canvas.drawText("Time: " + formatTime(timeTaken) + "s", screenX / 2, 200, paint);
                canvas.drawText("Distance remaining: " + distanceRemaining / 1000 + "km", screenX / 2, 240, paint);
                paint.setTextSize(80);
                canvas.drawText("Tap to replay!", screenX / 2, 350, paint);
            }

            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {

        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void startGame() {
        player = new PlayerShip(context, screenX, screenY);
        enemy1 = new EnemyShip(context, screenX, screenY);
        enemy2 = new EnemyShip(context, screenX, screenY);
        enemy3 = new EnemyShip(context, screenX, screenY);

        distanceRemaining = 10000;
        timeTaken = 0;

        timeStarted = System.currentTimeMillis();

        stopEnemy = 0;
        animEnd(0);
        gameEnded = false;
    }

    private String formatTime(long time) {
        long seconds = time / 1000;
        long thousandths = time  - (seconds * 1000);
        String strThousandths = thousandths + "";
        if (thousandths < 100) {
            strThousandths = "0" + thousandths;
        }
        if (thousandths < 10) {
            strThousandths = "0" + strThousandths;
        }
        return "" + seconds + "." + strThousandths;
    }

    private void animEnd(int ind) {
        if (ind == 1) {

            player.setMIN_SPEED(0);
            stopEnemy = 1;

            if (enemy1.overScreenState == 1) {
                if (enemy2.overScreenState == 1) {
                    if (enemy3.overScreenState == 1) {

                        for (SpaceDust sd : dustList) {
                            sd.setSpeed(0);
                        }

                    }
                }
            }

            if (fixedY == -1) {
                fixedY = player.getY();
            }

            player.setY(fixedY);
            player.setX(player.getX() + 30);

            enemy1.setSpeed(30);
            enemy2.setSpeed(30);
            enemy3.setSpeed(30);

        } else if (ind == 0) {

            Random generator = new Random();

            player.setMIN_SPEED(1);
            fixedY = -1;

            for (SpaceDust sd : dustList) {
                sd.setSpeed(generator.nextInt(15));
            }

        }
    }
}
