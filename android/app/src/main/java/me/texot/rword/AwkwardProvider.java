package me.texot.rword;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by texot on 16/1/26.
 */

public class AwkwardProvider implements IWordProvider {
    private static final String TAG = "AwkwardProvider";

    private static int[] REME_INTERVAL_LIST = {
            1,3,7,12,48,100
    };

    private static int[] REME_INTERVAL_MAX_OFFSET_LIST = {
            0,0,2,4,10,20
    };

    private WordDbAdapter m_dbAdapter;
    private ArrayList<WordDbAdapter.WordData> m_wordList;
    private TaskList m_taskList;
    private int m_taskIndex;

    public AwkwardProvider(Context context) {
        m_dbAdapter = new WordDbAdapter(context);
        m_dbAdapter.open();
        m_wordList = new ArrayList<>();
        m_taskList = new TaskList();
        m_taskIndex = -1;
    }

    @Override
    public void prepareWordList(int listId) {
        WordDbAdapter.WordData[] list = m_dbAdapter.getWordListWithContent(listId);

        if (list == null) {
            Log.e(TAG, "prepareWordList: list null");
            return;
        }
        else if(list.length <= 0)
            Log.e(TAG, "prepareWordList: list empty");

        for (WordDbAdapter.WordData word : list) {
            m_wordList.add(word);
            int emptyPos = 0;
            TaskList tmp;
            while(true) {
                tmp = new TaskList(m_taskList);
                emptyPos = tmp.findNextEmpty(emptyPos);
                new Task(word, Integer.MAX_VALUE, emptyPos, 0).put(tmp, 0);
                int pos = emptyPos;
                boolean allSuccess = true;
                for (int intervalIndex = 0; intervalIndex < REME_INTERVAL_LIST.length; intervalIndex++) {
                    pos += 1 + REME_INTERVAL_LIST[intervalIndex];
                    if(!new Task(word, intervalIndex, pos, REME_INTERVAL_MAX_OFFSET_LIST[intervalIndex])
                            .put(tmp, 0)) {
                        allSuccess = false;
                        break;
                    }
                }
                if(allSuccess)
                    break;
                emptyPos++;
            }
            m_taskList = tmp;

        }

    }

    public String getStatistics() {
        StringBuilder sb = new StringBuilder();
        Task[] tasks = m_taskList.toArrayOfAll();
        for(WordDbAdapter.WordData word : m_wordList) {
            sb.append(word.word).append(": ");
            ArrayList<Integer> indexList = new ArrayList<>();
            for(int i=0; i<tasks.length; i++)
                if(tasks[i] != null && tasks[i].getWord() == word) {
                    indexList.add(i);
                    sb.append(i).append(',');
                }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(';');
            for(int i=1; i<indexList.size(); i++) {
                sb.append(indexList.get(i) - indexList.get(i-1) - 1).append(',');
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    public WordDbAdapter.WordData getNextWord() {
        int taskIndex = m_taskList.findNextAvail(m_taskIndex + 1);
        if(taskIndex >= 0) {
            m_taskIndex = taskIndex;
            return m_taskList.get(m_taskIndex).getWord();
        }
        return null;
    }

    @Override
    public void setCurrentResult(int result) {

    }

    @Override
    public void setLastResult(int result) {

    }
}
