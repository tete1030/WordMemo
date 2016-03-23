package me.texot.rword;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by texot on 16/1/30.
 */
public class TaskList implements Iterable<Task> {
    private static int TASKLIST_SIZE_INCREASE_STEP = 10;
    private static int TASKLIST_INIT_SIZE = 10;
    private ArrayList<Task> m_tasks = new ArrayList<>(TASKLIST_INIT_SIZE);

    public TaskList(TaskList orig) {
        m_tasks = new ArrayList<>(orig.m_tasks);
        trimEndEmpty();
    }

    public TaskList() {

    }


    public Task set(int location, Task object) {
        while(location >= m_tasks.size()) {
            for(int i=0; i<=TASKLIST_SIZE_INCREASE_STEP; i++) {
                m_tasks.add(null);
            }
        }

        return m_tasks.set(location, object);
    }

    private void trimEndEmpty() {
        for(int i = m_tasks.size() - 1; i>=0 && m_tasks.get(i)==null; i--) {
            m_tasks.remove(i);
        }
    }

    public void trimEmpty() {
        for(int i = 0; i < m_tasks.size(); ) {
            if(m_tasks.get(i) == null)
                m_tasks.remove(i);
            else
                i++;
        }
    }

    public Task get(int location) {
        if(location < m_tasks.size())
            return m_tasks.get(location);
        return null;
    }

    public int findNextAvail(int startIndex) {
        int index;
        for(index = startIndex; index < m_tasks.size(); index++) {
            if(m_tasks.get(index) != null)
                return index;
        }
        return -1;
    }

    public int findNextEmpty(int startIndex) {
        int index;
        for(index = startIndex; index < m_tasks.size(); index++) {
            if(m_tasks.get(index) == null)
                return index;
        }
        return m_tasks.size();
    }

    public int countOfAvail() {
        int c = 0;
        for(Task t : m_tasks)
            if(t != null) c++;
        return c;
    }

    public int countOfAll() {
        return m_tasks.size();
    }


    public Integer[] getEmptyIndexArray() {
        ArrayList<Integer> list = new ArrayList<>();
        for(int i = 0; i < m_tasks.size(); i++)
            if(m_tasks.get(i) == null)
                list.add(i);
        return list.toArray(new Integer[list.size()]);
    }

    @Override
    public String toString() {
        return toStringOfAll();
    }

    public String toStringOfAll() {
        StringBuilder sb = new StringBuilder();
        for(Task t : m_tasks)
            if(t != null)
                sb.append(t.getWord().word).append('\n');
            else
                sb.append("<null>").append('\n');
        return sb.toString();
    }

    public String toStringOfAvail() {
        StringBuilder sb = new StringBuilder();
        for(Task t : m_tasks)
            if(t != null)
                sb.append(t.getWord().word).append('\n');
        return sb.toString();
    }

    public Task[] toArrayOfAll() {
        return m_tasks.toArray(new Task[m_tasks.size()]);
    }

    public Task[] toArrayOfAvail() {
        ArrayList<Task> retTaskList = new ArrayList<>();
        for(Task t : m_tasks)
            if(t != null)
                retTaskList.add(t);
        return retTaskList.toArray(new Task[retTaskList.size()]);
    }

    @Override
    public Iterator<Task> iterator() {
        return m_tasks.iterator();
    }
}
