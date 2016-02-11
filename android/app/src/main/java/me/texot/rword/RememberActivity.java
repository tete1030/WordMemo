package me.texot.rword;

import android.os.CountDownTimer;
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

// TODO: Multi-thread secure check

public class RememberActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView m_tvWord;
    private TextView m_tvPronunciation;
    private TextView m_tvParaphrase;
    private TextView m_tvProgress;
    private Button m_btnCurNo;
    private Button m_btnCurYes;
    private Button m_btnPause;

    private IWordProvider m_wordProvider;

    private CountDownTimer m_rememTimer, m_showTimer;

    private Timer m_preRememberTimer = new Timer(false);

    private int m_wordListID;

    private enum UIState {
        NONE,
        PREREMEMBER,
        REMEMBER,
        SHOW
    }

    private UIState m_uiState;

    public RememberActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remember);

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
                    // TODO: Pause all timer and action
                }
                else if(action == MotionEvent.ACTION_UP) {
                    // TODO: Restore all timer and action
                }
                return false;
            }
        });

        m_wordProvider = new AwkwardProvider();
        m_wordProvider.prepareWordList(listid);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        m_rememTimer = new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                RememberActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RememberActivity.this.onRememNotSuccess();
                    }

                });
            }
        };

        m_showTimer = new CountDownTimer(1000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                RememberActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RememberActivity.this.nextWord();
                    }

                });

            }
        };

        m_uiState = UIState.NONE;

        startRemember();


    }

    public void showWordParaphrase() {
        m_tvParaphrase.setVisibility(View.VISIBLE);
    }

    public void startRemember() {
        m_btnCurNo.setEnabled(true);
        m_btnCurYes.setEnabled(true);
        nextWord();
    }

    public void nextWord()
    {
        WordData word = m_wordProvider.getNextWord();
        if(word != null) {
            m_uiState = UIState.PREREMEMBER;
            // wait for 500ms to compensate user respond delay

            m_preRememberTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    RememberActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RememberActivity.this.onToRememberState();
                        }
                    });
                }
            }, 500);
            m_tvWord.setText(word.word);
            m_tvPronunciation.setText("[" + word.pronAndParaphrList.get(0).first + "]");
            m_tvParaphrase.setVisibility(View.INVISIBLE);
            m_tvParaphrase.setText(word.pronAndParaphrList.get(0).second);
            m_tvProgress.setText(String.format("%d/%d", m_wordProvider.getCurrentCompletedCount() + 1, m_wordProvider.getTotalCount()));
        }
        m_rememTimer.start();
    }

    public void onToRememberState() {
        m_uiState = UIState.REMEMBER;
        m_btnCurNo.setEnabled(true);
        m_btnCurYes.setEnabled(true);
    }

    public void onToShowState() {
        m_uiState = UIState.SHOW;
        m_rememTimer.cancel();
        showWordParaphrase();
        m_showTimer.start();
    }

    public void onRememNotSuccess() {
        if(m_uiState == UIState.PREREMEMBER) {
            m_wordProvider.setLastResult(0);
        }
        else if(m_uiState == UIState.REMEMBER) {
            m_wordProvider.setCurrentResult(0);
            onToShowState();
        }
        else if(m_uiState == UIState.SHOW) {
            m_wordProvider.setCurrentResult(0);
        }

    }

    public void onRememSuccess() {
        if(m_uiState == UIState.PREREMEMBER) {
            m_wordProvider.setLastResult(1);
        }
        else if(m_uiState == UIState.REMEMBER) {
            m_wordProvider.setCurrentResult(1);
            onToShowState();
        }
        else if(m_uiState == UIState.SHOW) {
            m_wordProvider.setCurrentResult(1);
        }
    }

    public void onClickLastNo()
    {

    }

    public void onClickCurrentNo()
    {
        m_btnCurNo.setEnabled(false);
        onRememNotSuccess();
    }

    public void onClickCurrentYes()
    {
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
