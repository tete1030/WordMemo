package me.texot.rword;

/**
 * Created by texot on 15/12/25.
 */
public interface IWordProvider {
    public void close();
    public void prepareWordList(int listId);
    public WordData getNextWord();
    public void setCurrentResult(int result);
    public void setLastResult(int result);
    public int getCurrentCompletedCount();
    public int getTotalCount();
}
