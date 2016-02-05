package me.texot.rword;

import org.apache.commons.math3.distribution.NormalDistribution;


import android.content.Context;
import android.util.Log;

import java.util.ArrayList;


/**
 * Created by texot on 15/12/22.
 */

public class NormalDistributionWordProvider implements IWordProvider {
    private static final String TAG = "NDistWordProvider";
    private WordDbAdapter m_dbAdapter;
    private ArrayList<RememberInfo> m_wordList;
    private int m_currentWordIndex = -1;
    private int m_lastWordIndex = -1;
    private int m_curTime = 0;

    private int m_offset[] = {0, 1, 2, 8, 16, 32, 64};
    private NormalDistribution m_offsetDist[];

    public NormalDistributionWordProvider() {
        m_wordList = new ArrayList<>();
        m_dbAdapter = WordDbAdapter.getInstance(null);
        m_dbAdapter.open();
        m_offsetDist = new NormalDistribution[m_offset.length];
        m_offsetDist[0] = null;
        for (int i = 1; i < m_offset.length; i++) {
            m_offsetDist[i] = new NormalDistribution(0,Math.pow(((double)i)/2, 1.5));
        }
    }

    @Override
    public void prepareWordList(int listId) {
        WordData[] list = m_dbAdapter.getWordListWithContent(listId);

        if (list == null) {
            Log.e(TAG, "prepareWordList: list null");
            return;
        }
        else if(list.length <= 0)
            Log.e(TAG, "prepareWordList: list empty");

        for (WordData word : list) {
            m_wordList.add(new RememberInfo(word));
        }
    }

    public void clearWordList() {
        m_wordList.clear();
    }

    @Override
    public WordData getNextWord() {
        int topPriorityIndex = -1;
        double topPriority = 0;

        m_curTime++;
        m_lastWordIndex = m_currentWordIndex;

        for(int index=0; index < m_wordList.size(); index++) {
            RememberInfo word = m_wordList.get(index);
            int wordLevel = word.getLevel();
            int lastRemTime = word.getLastRemTime();
            double priority = 0.0;
            if(wordLevel > 0 && wordLevel < m_offset.length) {
                priority = m_offsetDist[wordLevel].density(m_curTime-lastRemTime-m_offset[wordLevel]);

            }
            else if(wordLevel == 0)
                // level 6 of -10~+10 time flexibility
                priority = 0.01;
            else {
                priority = 0.01 * (1 - lastRemTime / m_curTime);
            }

            if (priority > topPriority && index != m_lastWordIndex) {
                topPriority = priority;
                topPriorityIndex = index;
            }

        }

        m_currentWordIndex = topPriorityIndex;
        if(topPriorityIndex != -1)
            return m_wordList.get(topPriorityIndex).getWord();
        else
            return null;
    }

    @Override
    public void setCurrentResult(int result) {
        if(m_currentWordIndex >= 0) {
            m_wordList.get(m_currentWordIndex).setRemResult(m_curTime, result);
        }
    }

    @Override
    public void setLastResult(int result) {
        if(m_lastWordIndex >= 0) {
            m_wordList.get(m_lastWordIndex).setRemResult(m_curTime, result);
        }
    }

    @Override
    public int getCurrentCompletedCount() {
        return m_curTime - 1;
    }

    @Override
    public int getTotalCount() {
        return m_wordList.size();
    }
}
