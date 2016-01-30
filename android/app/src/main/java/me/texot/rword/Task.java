package me.texot.rword;

import java.util.LinkedList;

/**
 * Created by texot on 16/1/30.
 */
public class Task {
    private WordDbAdapter.WordData m_word;
    private int m_priority;
    private int m_pos;
    private int m_maxOffset;
    private static int MAX_DEPTH = 10;
    //private TaskList m_taskList;
    private boolean m_isLoop = false;
    public Task(WordDbAdapter.WordData word, int priority, int pos, int maxOffset) {
        m_word = word;
        m_priority = priority;
        m_pos = pos;
        m_maxOffset = maxOffset;
        //m_taskList = taskList;
    }

    public int getPriority() {
        return m_priority;
    }

    public WordDbAdapter.WordData getWord() {
        return m_word;
    }

    public boolean put(TaskList taskList, int depth) {
        if(m_isLoop) return false;
        if(depth > MAX_DEPTH) return false;
        m_isLoop = true;
        for(int off=0; off <= m_maxOffset; off++) {
            for(int position : new int[]{ m_pos - off, m_pos + off }) {
                if(position < 0) continue;
                Task tmp = taskList.set(position, this);
                if (tmp == null || ( this.m_priority <= tmp.getPriority() && tmp.put(taskList, depth + 1))) {
                    m_isLoop = false;
                    return true;
                }
                taskList.set(position, tmp);

                if(off == 0) break;
            }
        }
        m_isLoop = false;
        return false;
    }


}
