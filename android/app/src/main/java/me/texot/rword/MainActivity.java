package me.texot.rword;

import android.app.Application;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tv_word;
    TextView tv_pronunciation;
    TextView tv_paraphrase;
    Button btn_lastno;
    Button btn_curno;
    Button btn_curyes;

    IWordProvider wordProvider;

    public MainActivity()
    {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_word = (TextView) findViewById(R.id.txt_word);
        tv_pronunciation = (TextView) findViewById(R.id.txt_pronunciation);
        tv_paraphrase = (TextView) findViewById(R.id.txt_paraphrase);
        btn_lastno = (Button) findViewById(R.id.btn_lastno);
        btn_curno = (Button) findViewById(R.id.btn_no);
        btn_curyes = (Button) findViewById(R.id.btn_yes);

        btn_lastno.setOnClickListener(MainActivity.this);
        btn_curno.setOnClickListener(MainActivity.this);
        btn_curyes.setOnClickListener(MainActivity.this);

        wordProvider = new AwkwardProvider(getApplicationContext());
        wordProvider.prepareWordList(7);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.nextWord();
                    }
                });
            }
        }, 0, 1000);



//        new CountDownTimer(3000, 1000) {
//
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            public void onFinish() {
//                MainActivity.this.nextWord();
//            }
//        }.start();

    }

    public void nextWord()
    {
        wordProvider.setCurrentResult(1);
        WordDbAdapter.WordData word = wordProvider.getNextWord();
        if(word != null) {
            tv_word.setText(word.word);
            tv_pronunciation.setText("[" + word.pronAndParaphrList.get(0).first + "]");
            tv_paraphrase.setText(word.pronAndParaphrList.get(0).second);
            //tv_paraphrase.setVisibility(View.INVISIBLE);
        }
    }

    public void onClickLastNo()
    {

    }

    public void onClickCurrentNo()
    {

    }

    public void onClickCurrentYes()
    {

    }


    @Override
    public void onClick(View view) {
        if(view == btn_lastno)
            onClickLastNo();
        else if(view == btn_curno)
            onClickCurrentNo();
        else if(view == btn_curyes)
            onClickCurrentYes();
    }
}
