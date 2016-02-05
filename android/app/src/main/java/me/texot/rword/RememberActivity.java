package me.texot.rword;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


public class RememberActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView m_tvWord;
    private TextView m_tvPronunciation;
    private TextView m_tvParaphrase;
    private TextView m_tvProgress;
    private Button m_btnLastno;
    private Button m_btnCurno;
    private Button m_btnCuryes;

    private IWordProvider m_wordProvider;

    private CountDownTimer m_remeTimer, m_showTimer;

    private int m_wordListID;

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
        m_btnLastno = (Button) findViewById(R.id.btn_lastno);
        m_btnCurno = (Button) findViewById(R.id.btn_no);
        m_btnCuryes = (Button) findViewById(R.id.btn_yes);

        m_btnLastno.setOnClickListener(RememberActivity.this);
        m_btnLastno.setVisibility(View.INVISIBLE);
        m_btnCurno.setOnClickListener(RememberActivity.this);
        m_btnCuryes.setOnClickListener(RememberActivity.this);

        m_wordProvider = new AwkwardProvider();
        m_wordProvider.prepareWordList(listid);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        Timer timer = new Timer(false);
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                RememberActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        RememberActivity.this.nextWord();
//                    }
//                });
//            }
//        }, 0, 1000);



        m_remeTimer = new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                RememberActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RememberActivity.this.onRemeNotSuccess();
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

        nextWord();


    }

    public void showWordParaphrase() {
        m_tvParaphrase.setVisibility(View.VISIBLE);
    }

    public void nextWord()
    {
        WordData word = m_wordProvider.getNextWord();
        if(word != null) {
            m_tvWord.setText(word.word);
            m_tvPronunciation.setText("[" + word.pronAndParaphrList.get(0).first + "]");
            m_tvParaphrase.setVisibility(View.INVISIBLE);
            m_tvParaphrase.setText(word.pronAndParaphrList.get(0).second);
            m_btnCurno.setEnabled(true);
            m_btnCuryes.setEnabled(true);
            m_tvProgress.setText(String.format("%d/%d", m_wordProvider.getCurrentCompletedCount(), m_wordProvider.getTotalCount()));
        }
        m_remeTimer.start();
    }

    public void onRemeNotSuccess() {
        m_remeTimer.cancel();
        m_showTimer.cancel();
        m_wordProvider.setCurrentResult(0);
        showWordParaphrase();
        m_showTimer.start();
    }

    public void onRemeSuccess() {
        m_remeTimer.cancel();
        m_showTimer.cancel();
        m_wordProvider.setCurrentResult(1);
        showWordParaphrase();
        m_showTimer.start();
    }

    public void onLastRemeNotSuccess() {
        m_remeTimer.cancel();
        m_showTimer.cancel();
        m_wordProvider.setLastResult(0);
        m_remeTimer.start();
    }

    public void onClickLastNo()
    {
        onLastRemeNotSuccess();
    }

    public void onClickCurrentNo()
    {
        m_btnCurno.setEnabled(false);
        onRemeNotSuccess();
    }

    public void onClickCurrentYes()
    {
        m_btnCuryes.setEnabled(false);
        onRemeSuccess();
    }


    @Override
    public void onClick(View view) {
        if(view == m_btnLastno)
            onClickLastNo();
        else if(view == m_btnCurno)
            onClickCurrentNo();
        else if(view == m_btnCuryes)
            onClickCurrentYes();
    }
}
