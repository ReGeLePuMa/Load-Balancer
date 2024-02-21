/* Implement this class. */

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

public class MyHost extends Host
{
    private final PriorityBlockingQueue<Task> queue;
    private Task currentTask;
    private volatile boolean isRunning;
    private volatile boolean wasInterrupted;
    

    private static final class MyTimer
    {
        private long startTime;
        private long endTime;

        public MyTimer(long duration)
        {
            startTime = System.currentTimeMillis();
            endTime = startTime + duration;
        }
        public long getLeft()
        {
            return endTime - System.currentTimeMillis();
        }
        public boolean isFinished()
        {
            return getLeft() <= 0;
        }
    }

    public MyHost()
    {
        queue = new PriorityBlockingQueue<>(10, Comparator.comparingInt(Task::getPriority).reversed().thenComparing(Task::getId));
        currentTask = null;
        isRunning = true;
        wasInterrupted = false;
    }

    @Override
    public void run()
    {
        while (isRunning)
        {
            currentTask = queue.poll();
            wasInterrupted = false;
            if (currentTask != null)
            {
                MyTimer timer = new MyTimer(currentTask.getLeft());
                while (!timer.isFinished())
                {
                    currentTask.setLeft(timer.getLeft());
                    if (wasInterrupted)
                    {
                        currentTask.setLeft(timer.getLeft());
                        queue.add(currentTask);
                        break;
                    }
                }
                if(!wasInterrupted)
                {
                    currentTask.finish();
                }
            }
        }
    }

    @Override
    public synchronized void addTask(Task task)
    {
        queue.add(task);
        if (currentTask != null && currentTask.isPreemptible()
            && task.getPriority() > currentTask.getPriority())
        {
            wasInterrupted = true;
        }
    }

    @Override
    public int getQueueSize()
    {
        return queue.size() + (currentTask == null ? 0 : 1);
    }

    @Override
    public long getWorkLeft()
    {
        return queue.stream().mapToLong(Task::getLeft).sum() + (currentTask == null ? 0 : currentTask.getLeft());
    }

    @Override
    public void shutdown()
    {
        isRunning = false;
    }
}
