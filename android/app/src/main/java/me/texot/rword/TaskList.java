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
        strimToMinimum();
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

    private void strimToMinimum() {
        while(m_tasks.size() > 0 && m_tasks.get(m_tasks.size() - 1) == null)
            m_tasks.remove(m_tasks.size()-1);
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


    public Integer[] getEmptyArray() {
        ArrayList<Integer> list = new ArrayList<>();
        for(int i = 0; i < m_tasks.size(); i++)
            if(m_tasks.get(i) == null)
                list.add(i);
        return list.toArray(new Integer[0]);
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
        return m_tasks.toArray(new Task[0]);
    }

    public Task[] toArrayOfAvail() {
        ArrayList<Task> retTaskList = new ArrayList<>();
        for(Task t : m_tasks)
            if(t != null)
                retTaskList.add(t);
        return retTaskList.toArray(new Task[0]);
    }

    @Override
    public Iterator<Task> iterator() {
        return m_tasks.iterator();
    }
}
