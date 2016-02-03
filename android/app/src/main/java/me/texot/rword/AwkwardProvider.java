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
    private ArrayList<WordDbAdapter.WordData> m_wordList = new ArrayList<>();
    private TaskList m_taskList = new TaskList();
    private int m_taskIndex = -1;
    private int m_lastTaskIndex = -1;
    private LinkedList<RememberInfo> m_remeHistory = new LinkedList<>();

    public AwkwardProvider(Context context) {
        m_dbAdapter = new WordDbAdapter(context);
        m_dbAdapter.open();
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
            RememberInfo rminfo = new RememberInfo(word);
            while(true) {
                tmp = new TaskList(m_taskList);
                emptyPos = tmp.findNextEmpty(emptyPos);
                new Task(word, rminfo, Integer.MAX_VALUE, emptyPos, 0).put(tmp, 0);
                int pos = emptyPos;
                boolean allSuccess = true;
                for (int intervalIndex = 0; intervalIndex < REME_INTERVAL_LIST.length; intervalIndex++) {
                    pos += 1 + REME_INTERVAL_LIST[intervalIndex];
                    if(!new Task(word, rminfo, intervalIndex, pos, REME_INTERVAL_MAX_OFFSET_LIST[intervalIndex])
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
        //int taskIndex = m_taskList.findNextAvail(m_taskIndex + 1);
        int taskIndex = m_taskIndex + 1;
        if(taskIndex >= 0 && taskIndex < m_taskList.countOfAll()) {
            m_lastTaskIndex = m_taskIndex;
            m_taskIndex = taskIndex;
            Task curtask = m_taskList.get(m_taskIndex);
            if(curtask != null) {
                RememberInfo rminfo = curtask.getRememberInfo();
                m_remeHistory.remove(rminfo);
                m_remeHistory.add(curtask.getRememberInfo());
                return curtask.getWord();
            }
            else {
                RememberInfo rminfo = null;
                for(RememberInfo ri : m_remeHistory) {
                    if(m_taskIndex + 1 >= m_taskList.countOfAll()) {
                        rminfo = ri;
                        break;
                    }
                    Task nextTask = m_taskList.get(m_taskIndex + 1);
                    if(nextTask == null) {
                        rminfo = ri;
                        break;
                    }
                    if (ri != nextTask.getRememberInfo()) {
                        rminfo = ri;
                        break;
                    }
                }
                if(rminfo == null)
                    rminfo = m_remeHistory.get(0);
                if(rminfo != null) {
                    curtask = new Task(rminfo.getWord(), rminfo, Integer.MAX_VALUE, taskIndex, Integer.MAX_VALUE);
                    curtask.put(m_taskList, 0);
                    m_remeHistory.remove(rminfo);
                    m_remeHistory.add(rminfo);
                    return curtask.getWord();
                }
            }
        }
        return null;
    }

    private void setResult(int index, int result) {
        Task task = m_taskList.get(index);
        task.getRememberInfo().setRemResult(index + 1, result);
        task.getRememberInfo().setLevel(task.getPriority() + 1);
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
