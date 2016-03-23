package me.texot.rword;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;

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
    private ArrayList<WordData> m_wordList = new ArrayList<>();
    private TaskList m_taskList = new TaskList();
    private int m_taskIndex = -1;
    private int m_lastTaskIndex = -1;
    private LinkedList<RememberInfo> m_remeHistory = new LinkedList<>();
    private boolean m_isPadding = true;

    public AwkwardProvider() {
        m_dbAdapter = WordDbAdapter.getInstance(null);
        m_dbAdapter.open();
    }

    @Override
    public void close() {
        if(m_dbAdapter != null) {
            m_dbAdapter.close();
            m_dbAdapter = null;
        }
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
        this.close();
    }


    public void setPadding(boolean padding) {
        m_isPadding = padding;
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
            m_wordList.add(word);
            int emptyPos = 0;
            TaskList tmp;
            RememberInfo rminfo = new RememberInfo(word);
            while(true) {
                tmp = new TaskList(m_taskList);
                emptyPos = tmp.findNextEmpty(emptyPos);
                new Task(word, rminfo, 0, emptyPos, 0).put(tmp, 0);
                int pos = emptyPos;
                boolean allSuccess = true;
                for (int intervalIndex = 0; intervalIndex < REME_INTERVAL_LIST.length; intervalIndex++) {
                    pos += 1 + REME_INTERVAL_LIST[intervalIndex];
                    if(!new Task(word, rminfo, intervalIndex + 1, pos, REME_INTERVAL_MAX_OFFSET_LIST[intervalIndex])
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
        for(WordData word : m_wordList) {
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
    public WordData getNextWord() {
        //int taskIndex = m_taskList.findNextAvail(m_taskIndex + 1);
        int taskIndex = m_taskIndex;
        WordData ret = null;

        while(true) {

            if (taskIndex >= m_taskList.countOfAll()) {
                Log.i("getNextWord", "index touch bound");
                break;
            }

            taskIndex++;

            Task curtask = m_taskList.get(taskIndex);
            Log.i("getNextWord", String.format("word at index %d, word = %s", taskIndex, curtask));
            RememberInfo rminfo = null;
            if (curtask != null) {
                rminfo = curtask.getRememberInfo();

            } else if (m_isPadding) {
                Log.i("getNextWord", "fill empty word");
                for (RememberInfo ri : m_remeHistory) {
                    // if it's the last word, current history word is ok to be presented
                    if (taskIndex + 1 >= m_taskList.countOfAll()) {
                        rminfo = ri;
                        break;
                    }
                    // if next word is null or next word is not equal to current history word,
                    // current word is ok
                    Task nextTask = m_taskList.get(taskIndex + 1);
                    if (nextTask == null || nextTask.getRememberInfo() != ri) {
                        rminfo = ri;
                        break;
                    }

                }
                // if history word failed to get, return first word in history
                if (rminfo == null && m_remeHistory.size() > 0)
                    rminfo = m_remeHistory.get(0);

                if (rminfo != null) {
                    curtask = new Task(rminfo.getWord(), rminfo, Integer.MAX_VALUE, taskIndex, Integer.MAX_VALUE);
                    curtask.put(m_taskList, 0);
                }
            }

            if(rminfo != null) {
                m_remeHistory.remove(rminfo); // if exists
                m_remeHistory.add(rminfo);
                ret = curtask.getWord();
                break;
            }

        }

        if(m_taskIndex != taskIndex) {
            m_lastTaskIndex = m_taskIndex;
            m_taskIndex = taskIndex;
        }
        return ret;
    }

    private void setResult(int index, int result) {
        Task task = m_taskList.get(index);
        RememberInfo rminfo = task.getRememberInfo();
        rminfo.setRemResult(index + 1, result);
        rminfo.setLevel(task.getPriority() + 1);

        // if the first time remembering of a word is success, ignore future tasks of this word
        Log.i("setResult", String.format("index %d priority %d log result %d", index, task.getPriority(), result));
        if(task.getPriority() == 0 && result == 1) {
            for (int i = m_taskIndex + 1; i < m_taskList.countOfAll(); i++) {
                Task tmp = m_taskList.get(i);
                if (tmp != null && tmp.getRememberInfo() == rminfo) {
                    Log.i("setResult", String.format("delete index %d", i));
                    m_taskList.set(i, null);
                }
            }
        }
    }

    @Override
    public void setCurrentResult(int result) {
        if(m_taskIndex >= 0)
            setResult(m_taskIndex, result);
    }

    @Override
    public void setLastResult(int result) {
        if(m_lastTaskIndex >= 0)
            setResult(m_lastTaskIndex, result);
    }

    @Override
    public int getCurrentCompletedCount() {
        return m_taskIndex;
    }

    @Override
    public int getTotalCount() {
        return m_taskList.countOfAll();
    }
}
