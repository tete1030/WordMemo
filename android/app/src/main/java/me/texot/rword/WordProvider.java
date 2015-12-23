package me.texot.rword;

import org.apache.commons.math3.distribution.NormalDistribution;


import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by texot on 15/12/22.
 */

public class WordProvider {
    private static final String TAG = "WordProvider";
    private WordDbAdapter m_dbAdapter;
    private ArrayList<WordDbAdapter.WordData> m_wordList;
    private int m_currentWordIndex = -1;

    public WordProvider(Context context)
    {
        m_wordList = new ArrayList<>();
        m_dbAdapter = new WordDbAdapter(context);
        m_dbAdapter.open();
    }

    public void prepareWordList(int listId)
    {
        WordDbAdapter.WordData[] list = m_dbAdapter.getWordListWithContent(listId);
        if(list != null && list.length > 0)
            m_wordList.addAll(Arrays.asList(list));
        else if (list == null)
            Log.e(TAG, "prepareWordList: list null");
        else
            Log.e(TAG, "prepareWordList: list empty");
    }

    public void clearWordList(){
        m_wordList.clear();
    }

    public WordDbAdapter.WordData getNextWord()
    {
        if(m_wordList.size() > m_currentWordIndex + 1) {
            return m_wordList.get(++m_currentWordIndex);
        }
        else
            return null;
    }
}
