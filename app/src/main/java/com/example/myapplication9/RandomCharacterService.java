package com.example.myapplication9;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.Random;
import android.os.Handler;
import android.os.Looper;

public class RandomCharacterService extends Service {
    private boolean isRandomGeneratorOn;
    private final String TAG = "RandomCharacterService";
    private Thread workerThread;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    public static final String BROADCAST_ACTION = "my.custom.action.tag.lab9";
    public static final String EXTRA_CHAR = "randomCharacter";

    char[] alphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ".toCharArray();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Сервис создан");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Toast.makeText(getApplicationContext(), "Сервис запущен", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Сервис запущен...");
            Log.i(TAG, "ID потока в onStartCommand: " + Thread.currentThread().getId());
            isRandomGeneratorOn = true;

            workerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    startRandomGenerator();
                }
            });
            workerThread.start();

            return START_STICKY;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при запуске сервиса", e);
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    private void startRandomGenerator() {
        Log.d(TAG, "Генератор случайных букв запущен");
        while(isRandomGeneratorOn) {
            try {
                Thread.sleep(1000);
                if(isRandomGeneratorOn) {
                    int MIN = 0;
                    int MAX = alphabet.length - 1;
                    int randomIdx = new Random().nextInt(MAX - MIN + 1) + MIN;
                    final char randomChar = alphabet[randomIdx];
                    Log.i(TAG, "ID потока: " + Thread.currentThread().getId() + ", Случайная буква: " + randomChar);

                    mainHandler.post(() -> {
                        try {
                            Intent intent = new Intent(BROADCAST_ACTION);
                            intent.putExtra(EXTRA_CHAR, randomChar);
                            LocalBroadcastManager.getInstance(getApplicationContext())
                                    .sendBroadcast(intent);
                            Log.d(TAG, "Отправлен символ: " + randomChar);
                        } catch (Exception e) {
                            Log.e(TAG, "Ошибка при отправке broadcast", e);
                        }
                    });
                }
            } catch (InterruptedException e) {
                Log.w(TAG, "Поток прерван", e);
                break;
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при генерации случайной буквы", e);
            }
        }
        Log.d(TAG, "Генератор случайных букв остановлен");
    }

    private void stopRandomGenerator() {
        Log.d(TAG, "Остановка генератора случайных букв");
        isRandomGeneratorOn = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
            stopRandomGenerator();
            Toast.makeText(getApplicationContext(), "Сервис остановлен", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Сервис уничтожен...");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при остановке сервиса", e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}