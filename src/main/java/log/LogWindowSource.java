package log;

import gui.LogWindow;

import java.util.*;

/**
 * Что починить:
 * 1. Этот класс порождает утечку ресурсов (связанные слушатели оказываются
 * удерживаемыми в памяти)
 * 2. Этот класс хранит активные сообщения лога, но в такой реализации он
 * их лишь накапливает. Надо же, чтобы количество сообщений в логе было ограничено
 * величиной m_iQueueLength (т.е. реально нужна очередь сообщений
 * ограниченного размера)
 */
public class LogWindowSource
{
    private int m_iQueueLength;

    private ArrayDeque<LogEntry> m_messages;
    private final ArrayList<LogChangeListener> m_listeners;
    private volatile LogChangeListener[] m_activeListeners;

    public LogWindowSource(int iQueueLength)
    {
        m_iQueueLength = iQueueLength;
        m_messages = new ArrayDeque<LogEntry>(iQueueLength);
        m_listeners = new ArrayList<LogChangeListener>();
    }
//dequeue
    public void registerListener(LogChangeListener listener)
    {
        synchronized(m_listeners)
        {
            m_listeners.add(listener);
            m_activeListeners = null;
        }
    }

    public void unregisterListener(LogChangeListener listener)
    {
        synchronized(m_listeners)
        {
            m_listeners.remove(listener);
            m_activeListeners = null;
        }
    }

    public void unregisterAllListener(){
    }

    public Boolean isContains(LogWindow logWin){
        return m_listeners.contains(logWin);
    }

    synchronized public void append(LogLevel logLevel, String strMessage)
    {
        LogEntry entry = new LogEntry(logLevel, strMessage);
        delMore();
        m_messages.add(entry);
        LogChangeListener [] activeListeners = m_activeListeners;
        if (activeListeners == null)
        {
            synchronized (m_listeners)
            {
                if (m_activeListeners == null)
                {
                    activeListeners = m_listeners.toArray(new LogChangeListener [0]);
                    m_activeListeners = activeListeners;
                }
            }
        }
        for (LogChangeListener listener : activeListeners)
        {
            listener.onLogChanged();
        }
    }

    private void delMore() {
        if (m_iQueueLength <= m_messages.size()){
            m_messages.removeFirst();
        }
    }

    public int size()
    {
        return m_messages.size();
    }

    public Iterable<LogEntry> range(int startFrom, int count)
    {
        if (startFrom < 0 || startFrom >= m_messages.size())
        {
            return Collections.emptyList();
        }
        int indexTo = Math.min(startFrom + count, m_messages.size());
        ArrayList<LogEntry> temp = new ArrayList<>(m_messages);
        return temp.subList(startFrom, indexTo);
    }

    public Iterable<LogEntry> all()
    {
        return m_messages;
    }
}
