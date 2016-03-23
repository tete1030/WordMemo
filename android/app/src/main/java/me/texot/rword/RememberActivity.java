package me.texot.rword;

import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

// TODO List
// * multi-thread safe check
// * dynamic paraphrase present duration according to length of text
// pause (show the phrase when pause pressed)
// * stop (enter the stop status, and stop after necessary words have been prompted)
// word succeed at first time do not appear again or decrease times
// * failed word statistic
// * half an hour one time is better
// * tune durations between words (decrease times and/or reduce durations between latter trials)
// * present other phrases

/*
FIXME same word appeared consistently when padding disabled (need re-constructing?), especially
FIXME when word between them is deleted as that word is known
*/

public class RememberActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView m_tvWord;
    private TextView m_tvPronunciation;
    private TextView m_tvParaphrase;
    private TextView m_tvProgress;
    private Button m_btnCurNo;
    private Button m_btnCurYes;
    private Button m_btnPause;

    private IWordProvider m_wordProvider;

    private CountDownTimer m_rememTimer, m_perceptionTimer;

    private Timer m_preRememberTimer;

    private Handler m_handler = new Handler();

    private int m_wordListID;

    private enum UIState {
        START,
        PREREMEMBER,
        REMEMBER,
        PERCEPTION,
        PAUSE,
        STOP
    }

    private UIState m_uiState;

    public RememberActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remember);

        // MainActivity notify listid
        int listid = getIntent().getIntExtra("listid", 0);
        if(listid == 0) {
            finish();
            return;
        }

        m_tvWord = (TextView) findViewById(R.id.txt_word);
        m_tvPronunciation = (TextView) findViewById(R.id.txt_pronunciation);
        m_tvParaphrase = (TextView) findViewById(R.id.txt_paraphrase);
        m_tvProgress = (TextView) findViewById(R.id.txt_progress);
        m_btnCurNo = (Button) findViewById(R.id.btn_no);
        m_btnCurYes = (Button) findViewById(R.id.btn_yes);
        m_btnPause = (Button) findViewById(R.id.btn_pause);

        m_btnCurNo.setOnClickListener(RememberActivity.this);
        m_btnCurYes.setOnClickListener(RememberActivity.this);
        m_btnPause.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if(action == MotionEvent.ACTION_DOWN) {
                    RememberActivity.this.onButtonPause(true);
                } else if(action == MotionEvent.ACTION_UP) {
                    RememberActivity.this.onButtonPause(false);
                }
                return false;
            }
        });

        AwkwardProvider awkProv = new AwkwardProvider();
        awkProv.setPadding(false);

        m_wordProvider = awkProv;
        m_wordProvider.prepareWordList(listid);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        m_rememTimer = new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
                // do countdown
            }

            public void onFinish() {
                m_handler.post(m_rememTimerRunnable);
            }
        };

        m_perceptionTimer = new CountDownTimer(1000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                m_handler.post(m_perceptionTimerRunnable);
            }
        };

        setUIState(UIState.START);

    }

    @Override
    public void onPause() {
        super.onPause();
        setUIState(UIState.PAUSE);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUIState(UIState.PERCEPTION);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setUIState(UIState.STOP);
        m_wordProvider.close();
    }


    public void onButtonPause(boolean isDown) {
        if(isDown) {
            Log.i("onButtonPause", "pause down");
            setUIState(UIState.PAUSE);
            onRememFailed();
        } else {
            Log.i("onButtonPause", "pause up");
            setUIState(UIState.PERCEPTION);
        }
    }

    private void stopAllTimer() {
        m_rememTimer.cancel();
        m_handler.removeCallbacks(m_rememTimerRunnable);
        m_perceptionTimer.cancel();
        m_handler.removeCallbacks(m_perceptionTimerRunnable);
        if(m_preRememberTimer != null) {
            m_preRememberTimer.cancel();
            m_preRememberTimer = null;
            m_handler.removeCallbacks(m_preRememTimerRunnable);
        }
    }

    public void setUIState(UIState state) {
        Log.i("setUIState", state.toString());
        m_uiState = state;

        switch(state) {
            case START:
                stopAllTimer();
                m_btnCurNo.setEnabled(true);
                m_btnCurYes.setEnabled(true);
                setUIState(UIState.PREREMEMBER);

                break;

            case PREREMEMBER:
                stopAllTimer();
                WordData word = m_wordProvider.getNextWord();
                if(word == null) {
                    setUIState(UIState.STOP);
                    break;
                }
                // wait for 500ms to compensate user respond delay
                m_preRememberTimer = new Timer(false);
                m_preRememberTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        RememberActivity.this.m_handler.post(m_preRememTimerRunnable);
                    }
                }, 500);

                m_tvWord.setText(word.word);
                m_tvPronunciation.setText(String.format("[%s]", word.pronAndParaphrList.get(0).first));
                m_tvParaphrase.setVisibility(View.INVISIBLE);
                m_tvParaphrase.setText(word.pronAndParaphrList.get(0).second);
                m_tvProgress.setText(String.format("%d/%d", m_wordProvider.getCurrentCompletedCount() + 1, m_wordProvider.getTotalCount()));
                m_rememTimer.start();
                break;
            case REMEMBER:
                m_btnCurNo.setEnabled(true);
                m_btnCurYes.setEnabled(true);

                break;
            case PERCEPTION:
                stopAllTimer();
                m_tvParaphrase.setVisibility(View.VISIBLE);
                m_perceptionTimer.start();
                break;
            case PAUSE:
                stopAllTimer();
                m_btnCurNo.setEnabled(false);
                m_btnCurYes.setEnabled(false);
                m_tvParaphrase.setVisibility(View.VISIBLE);
                break;
            case STOP:
                stopAllTimer();
                m_btnCurNo.setEnabled(false);
                m_btnCurYes.setEnabled(false);
                m_tvParaphrase.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private Runnable m_rememTimerRunnable = new Runnable() {
        @Override
        public void run() {
            RememberActivity.this.onRememTimer();
        }
    };

    private Runnable m_perceptionTimerRunnable = new Runnable() {
        @Override
        public void run() {
            RememberActivity.this.onPerceptionTimer();
        }
    };

    private Runnable m_preRememTimerRunnable = new Runnable() {
        @Override
        public void run() {
            RememberActivity.this.setUIState(UIState.REMEMBER);
        }
    };

    public void onRememTimer() {
        Log.i("onRememTimer", "");
        onRememFailed();
    }

    public void onPerceptionTimer() {
        Log.i("onPerceptionTimer", "");
        setUIState(UIState.PREREMEMBER);
    }

    public void onRememFailed() {
        Log.i("onRememFailed", "remember failed");
        if(m_uiState == UIState.PREREMEMBER) {
            m_wordProvider.setLastResult(0);
        }
        else if(m_uiState == UIState.REMEMBER) {
            m_wordProvider.setCurrentResult(0);
            setUIState(UIState.PERCEPTION);
        }
        else if(m_uiState == UIState.PERCEPTION) {
            m_wordProvider.setCurrentResult(0);
        }
        else if(m_uiState == UIState.PAUSE) {
            m_wordProvider.setCurrentResult(0);
        }

    }

    public void onRememSuccess() {
        Log.i("onRememSuccess", "remember success");
        if(m_uiState == UIState.PREREMEMBER) {
            m_wordProvider.setLastResult(1);
        }
        else if(m_uiState == UIState.REMEMBER) {
            m_wordProvider.setCurrentResult(1);
            setUIState(UIState.PERCEPTION);
        }
        else if(m_uiState == UIState.PERCEPTION) {
            m_wordProvider.setCurrentResult(1);
        }
    }

    public void onClickCurrentNo()
    {
        Log.i("onClickCurrentNo", "no");
        m_btnCurNo.setEnabled(false);
        onRememFailed();
    }

    public void onClickCurrentYes()
    {
        Log.i("onClickCurrentYes", "yes");
        m_btnCurYes.setEnabled(false);
        onRememSuccess();
    }


    @Override
    public void onClick(View view) {
        if(view == m_btnCurNo)
            onClickCurrentNo();
        else if(view == m_btnCurYes)
            onClickCurrentYes();
    }
}
