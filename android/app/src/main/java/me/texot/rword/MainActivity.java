package me.texot.rword;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private View m_view;
            private TextView m_textView;
            private Context m_ctx;
            public ViewHolder(View v, final Context ctx) {
                super(v);
                m_view = v;
                m_textView = (TextView) v.findViewById(R.id.txt_itemtext);
                m_ctx = ctx;
            }
            public void setID(final int id) {
                m_textView.setText(String.valueOf(id));
                m_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(m_ctx, RememberActivity.class);
                        intent.putExtra("listid", id);
                        m_ctx.startActivity(intent);
                    }
                });
            }
        }

        private int[] m_wordList;
        private Context m_ctx;

        public MyAdapter(int[] wordList, Context ctx) {
            m_wordList = wordList.clone();
            m_ctx = ctx;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_wordlist_item, parent, false);
            ViewHolder vh = new ViewHolder(v, m_ctx);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.setID(m_wordList[position]);
        }

        @Override
        public int getItemCount() {
            return m_wordList.length;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        WordDbAdapter wdbAdapter = WordDbAdapter.getInstance(this.getApplicationContext());
        wdbAdapter.open();
        int wordListIDs[] = wdbAdapter.getWordListIDs();
        wdbAdapter.close();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_wordlist);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyAdapter(wordListIDs, this));


    }

}
