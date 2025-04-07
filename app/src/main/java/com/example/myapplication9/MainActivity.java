package com.example.myapplication9;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView randomCharacterTextView;
    private TextView statusTextView;
    private Button startButton;
    private Button stopButton;
    private BroadcastReceiver broadcastReceiver;
    private Intent serviceIntent;
    private boolean isServiceRunning = false;
    private static final String KEY_IS_SERVICE_RUNNING = "is_service_running";
    private static final String KEY_LAST_CHARACTER = "last_character";
    private Animation fadeInAnimation;
    private Animation buttonAnimation;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        randomCharacterTextView = findViewById(R.id.editText_randomCharacter);
        statusTextView = findViewById(R.id.textView_status);
        startButton = findViewById(R.id.button_start);
        stopButton = findViewById(R.id.button_end);

        broadcastReceiver = new MyBroadcastReceiver();
        serviceIntent = new Intent(this, RandomCharacterService.class);

        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_press);

        setupButtonAnimations();

        if (savedInstanceState != null) {
            isServiceRunning = savedInstanceState.getBoolean(KEY_IS_SERVICE_RUNNING, false);
            String lastChar = savedInstanceState.getString(KEY_LAST_CHARACTER, "");
            if (!lastChar.isEmpty()) {
                randomCharacterTextView.setText(lastChar);
            }
            if (isServiceRunning) {
                startService(serviceIntent);
                updateStatusView(true);
            }
        }

        // Установим начальный текст
        randomCharacterTextView.setText("?");
        stopButton.setEnabled(false);
    }

    private void setupButtonAnimations() {
        buttonAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mainHandler.postDelayed(() -> {
                    startButton.clearAnimation();
                    stopButton.clearAnimation();
                }, 50);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void updateStatusView(boolean isRunning) {
        mainHandler.post(() -> {
            if (isRunning) {
                statusTextView.setText("Статус: работает");
                statusTextView.setTextColor(Color.parseColor("#4CAF50"));
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            } else {
                statusTextView.setText("Статус: остановлен");
                statusTextView.setTextColor(Color.parseColor("#F44336"));
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });
    }

    private void updateCharacterWithAnimation(final String character) {
        Log.d(TAG, "Обновление символа: " + character);
        mainHandler.post(() -> {
            Log.d(TAG, "Установка текста в UI");
            randomCharacterTextView.setText(character);
            randomCharacterTextView.startAnimation(fadeInAnimation);
            Log.d(TAG, "Текст после обновления: " + randomCharacterTextView.getText());
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_SERVICE_RUNNING, isServiceRunning);
        outState.putString(KEY_LAST_CHARACTER, randomCharacterTextView.getText().toString());
    }

    public void onClick(View view) {
        view.startAnimation(buttonAnimation);

        if (view.getId() == R.id.button_start) {
            Log.d(TAG, "Нажата кнопка СТАРТ");
            startService(serviceIntent);
            isServiceRunning = true;
            updateStatusView(true);
        } else if (view.getId() == R.id.button_end) {
            Log.d(TAG, "Нажата кнопка СТОП");
            stopService(serviceIntent);
            randomCharacterTextView.setText("?");
            isServiceRunning = false;
            updateStatusView(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(RandomCharacterService.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadcastReceiver, intentFilter);
        updateStatusView(isServiceRunning);
        Log.d(TAG, "BroadcastReceiver зарегистрирован");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(broadcastReceiver);
        Log.d(TAG, "BroadcastReceiver отменен");
    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Log.d(TAG, "Получен broadcast");
                char data = intent.getCharExtra(RandomCharacterService.EXTRA_CHAR, '?');
                Log.d(TAG, "Полученный символ: " + data);
                updateCharacterWithAnimation(String.valueOf(data));
            } catch (Exception e) {
                Log.e(TAG, "Ошибка в onReceive", e);
                e.printStackTrace();
            }
        }
    }
}