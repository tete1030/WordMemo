package me.texot.rword;

/**
 * Created by texot on 15/12/26.
 */
public class RememberInfo {
    private WordDbAdapter.WordData m_word;
    private int m_level;
    private int m_wrongTimes;
    private int m_lastRemTime;

    public int getLastRemTime() {
        return m_lastRemTime;
    }



    public RememberInfo(WordDbAdapter.WordData word) {
        m_word = word;
        m_level = 0;
        m_wrongTimes = 0;
        m_lastRemTime = 0;
    }

    public WordDbAdapter.WordData getWord() {
        return m_word;
    }

    public void setRemResult(int time, int result) {
        m_lastRemTime = time;
        if(result == 0) {
            m_wrongTimes++;
            m_level = 1;
        }
        else {
            m_level++;
        }
    }

    public int getLevel() {
        return m_level;
    }

    public int getWrongTimes() {
        return m_wrongTimes;
    }

}
