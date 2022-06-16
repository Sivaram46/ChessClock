package com.example.chessclock;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView timerTop;
    private TextView movesTop;
    private TextView timerBottom;
    private TextView movesBottom;

    private ImageButton startPause;
    private ImageButton reset;
    private ImageButton schedule;

    private RelativeLayout topRl;
    private RelativeLayout bottomRl;

    private int minutes = 5;
    private int seconds = 0;
    // Total timer time in 1/10 th of a second
    private int getTotalTime() {
        return (minutes*60 + seconds) * 10;
    }

    private int secondsTop = getTotalTime();
    private int secondsBottom = getTotalTime();

    // increment value, that many seconds will be increased whenever the user makes a move
    private int increment = 0;

    private boolean running = false;
    // To save the state of running or not running when minimizing the app
    private boolean wasRunning;

    private boolean isTop;
    // To store which side - bottom or top was previously running
    private boolean prev_state;

    // To store whether the timer runs out or not
    private boolean ended = false;

    // To track down number of moves for top and bottom timers
    private int numMovesTop = 0;
    private int numMovesBottom = 0;

    private final int MAIN_ACTIVITY_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        if (savedInstanceState != null) {
            secondsTop = savedInstanceState.getInt("secondsTop");
            secondsBottom = savedInstanceState.getInt("secondsBottom");
            running = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
        }

        timerTop = (TextView) findViewById(R.id.timeTopText);
        timerBottom = (TextView) findViewById(R.id.timeBottomText);
        movesTop = (TextView) findViewById(R.id.movesTopText);
        movesBottom = (TextView) findViewById(R.id.movesBottomText);

        setDefaultTime();

        startPause = (ImageButton) findViewById(R.id.startButton);
        startPause.setEnabled(false);
        startPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Playing state
                if (!running) {
                    running = true;
                    startPause.setImageResource(R.drawable.pause);
                    if (isTop) {
                        topRl.setBackgroundResource(R.drawable.background2);
                        timerTop.setTextColor(getResources().getColor(R.color.black));
                        movesTop.setTextColor(getResources().getColor(R.color.textSecondary2));
                    }
                    else {
                        bottomRl.setBackgroundResource(R.drawable.background2);
                        timerBottom.setTextColor(getResources().getColor(R.color.black));
                        movesBottom.setTextColor(getResources().getColor(R.color.textSecondary2));
                    }
                }
                // Pausing state
                else {
                    running = false;
                    startPause.setImageResource(R.drawable.play);
                    resetLayout();
                }
            }
        });

        reset = (ImageButton) findViewById(R.id.resetButton);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });

        // Tried using ActivityResultLauncher API to transfer data between intents. Had to debug
        ActivityResultLauncher<Intent> launchTimer = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == MAIN_ACTIVITY_CODE) {
                            Intent data = result.getData();
                            if (data.hasExtra("Seconds")) {
                                seconds = data.getExtras().getInt("Seconds");
                            }
                            if (data.hasExtra("Minutes")) {
                                minutes = data.getExtras().getInt("Minutes");
                            }
                            // if the number picker is not changed, restore to the default value of
                            // 5 mins 0 secs. Without this if condition, previous value of minutes
                            // and seconds would be shown.
                            if (!data.hasExtra("Seconds") && !data.hasExtra("Minutes")) {
                                minutes = 5;
                                seconds = 0;
                            }

                            if (data.hasExtra("Increment")) {
                                increment = data.getExtras().getInt("Increment");
                            }
                            else {
                                increment = 0;
                            }

                            reset();
                            // unset wasRunning because when switching back from TimePicker class,
                            // the timer resumes instead of just in pause state.
                            wasRunning = false;
                        }

                    }
                }
        ); 

        schedule = (ImageButton) findViewById(R.id.scheduleButton);
        schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pause the timer when the button is pressed and also set the play image to the
                // startPause button
                wasRunning = running;
                running = false;
                startPause.setImageResource(R.drawable.play);

                Intent intent = new Intent(view.getContext(), TimePickerActivity.class);
                intent.putExtra("Minutes", minutes);
                intent.putExtra("Seconds", seconds);
                launchTimer.launch(intent);
            }
        });

        topRl = (RelativeLayout) findViewById(R.id.topRl);
        topRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ended) {
                    return;
                }

                bottomRl.setBackgroundResource(R.drawable.background2);
                timerBottom.setTextColor(getResources().getColor(R.color.black));
                movesBottom.setTextColor(getResources().getColor(R.color.textSecondary2));

                topRl.setBackgroundResource(R.drawable.background1);
                timerTop.setTextColor(getResources().getColor(R.color.white));
                movesTop.setTextColor(getResources().getColor(R.color.textSecondary1));

                prev_state = isTop;
                isTop = false;
                if (prev_state && running) {
                    numMovesBottom++;
                    // increment seconds when user make a move
                    secondsTop += (increment * 10);
                    setTimerTime(secondsTop, timerTop);
                }

                if (!running) {
                    running = true;
                }

                movesTop.setText(String.valueOf(numMovesBottom) + " moves");

                // Draw start or pause button at the center when running/not running
                if (running) {
                    startPause.setEnabled(true);
                    startPause.setImageResource(R.drawable.pause);
                }
                else {
                    startPause.setImageResource(R.drawable.play);
                }
            }
        });

        bottomRl = (RelativeLayout) findViewById(R.id.bottomRl);
        bottomRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ended) {
                    return;
                }

                bottomRl.setBackgroundResource(R.drawable.background1);
                timerBottom.setTextColor(getResources().getColor(R.color.white));
                movesBottom.setTextColor(getResources().getColor(R.color.textSecondary1));

                topRl.setBackgroundResource(R.drawable.background2);
                timerTop.setTextColor(getResources().getColor(R.color.black));
                movesTop.setTextColor(getResources().getColor(R.color.textSecondary2));

                prev_state = isTop;
                isTop = true;
                if (!prev_state && running) {
                    numMovesTop++;
                    // increment seconds when user make a move
                    secondsBottom += (increment * 10);
                    setTimerTime(secondsBottom, timerBottom);
                }

                if (!running) {
                    running = true;
                }
                movesBottom.setText(String.valueOf(numMovesTop) + " moves");

                if (running) {
                    startPause.setEnabled(true);
                    startPause.setImageResource(R.drawable.pause);
                }
                else {
                    startPause.setImageResource(R.drawable.play);
                }
            }
        });

        runTimer();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("secondsTop", secondsTop);
        savedInstanceState.putInt("secondsBottom", secondsBottom);
        savedInstanceState.putBoolean("running", running);
        savedInstanceState.putBoolean("wasRunning", wasRunning);
    }

    @Override
    protected void onPause() {
        super.onPause();
        wasRunning = running;
        running = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasRunning) {
            running = true;
        }
    }

    private void runTimer() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isTop) {
                    setTimerTime(secondsTop, timerTop);
                    if (running) {
                        secondsTop--;
                    }

                    // when timer running out of time
                    if (secondsTop <= 0) {
                        ended = true;
                        running = false;
                        startPause.setEnabled(false);
                        topRl.setBackgroundResource(R.drawable.background3);
                    }
                }
                else {
                    setTimerTime(secondsBottom, timerBottom);
                    if (running) {
                        secondsBottom--;
                    }

                    if (secondsBottom <= 0) {
                        ended = true;
                        running = false;
                        startPause.setEnabled(false);
                        bottomRl.setBackgroundResource(R.drawable.background3);
                    }
                }

                // Update the seconds variable every 100 milliseconds so that the play/pause buttons
                // seems more responsive. Have to divide the seconds variable by 10 before displaying it.
                handler.postDelayed(this, 100);
            }
        });
    }

    public void resetLayout() {
        bottomRl.setBackgroundResource(R.drawable.background1);
        topRl.setBackgroundResource(R.drawable.background1);

        timerBottom.setTextColor(getResources().getColor(R.color.white));
        timerTop.setTextColor(getResources().getColor(R.color.white));

        movesBottom.setTextColor(getResources().getColor(R.color.textSecondary1));
        movesTop.setTextColor(getResources().getColor(R.color.textSecondary1));
    }

    public void reset() {
        secondsBottom = secondsTop = getTotalTime();
        running = false;

        setDefaultTime();
        resetLayout();

        startPause.setEnabled(false);
        startPause.setImageResource(R.drawable.play);

        numMovesBottom = numMovesTop = 0;
        movesTop.setText(R.string.default_move);
        movesBottom.setText(R.string.default_move);

        ended = false;
    }

    public void setTimerTime(int timeSeconds, TextView textView) {
        int seconds = (timeSeconds/10) % 60;
        int minutes = (timeSeconds/10) / 60;
        String time = String.format(Locale.getDefault(), "%2d:%02d", minutes, seconds);
        SpannableString spanString = new SpannableString(time);
        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
        textView.setText(spanString);
    }

    public void setDefaultTime() {
        setTimerTime(getTotalTime(), timerTop);
        setTimerTime(getTotalTime(), timerBottom);
    }
}