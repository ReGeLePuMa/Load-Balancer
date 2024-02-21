/* Implement this class. */

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MyDispatcher extends Dispatcher
{
    private AtomicInteger nextHost = new AtomicInteger(0);
    private static final Comparator<Host> SQComp = Comparator.comparingInt(Host::getQueueSize)
                                                             .thenComparing(Host::getId);
    private static final Comparator<Host> LWLComp = Comparator.comparingLong(Host::getWorkLeft)
                                                              .thenComparing(Host::getId);                                                    
    public MyDispatcher(SchedulingAlgorithm algorithm, List<Host> hosts)
    {
        super(algorithm, hosts);
    }

    @Override
    public synchronized void addTask(Task task)
    {
        switch (algorithm) 
        {
            case LEAST_WORK_LEFT:
            {
                Collections.min(hosts, LWLComp).addTask(task);
                break;
            }
            case ROUND_ROBIN:
            {
                hosts.get(nextHost.get()).addTask(task);
                nextHost.set((nextHost.get() + 1) % hosts.size());
                break;
            }
            case SHORTEST_QUEUE:
            {
                Collections.min(hosts, SQComp).addTask(task);
                break;
            }
            case SIZE_INTERVAL_TASK_ASSIGNMENT:
            {
                hosts.get(task.getType().ordinal()).addTask(task);
                break;
            }
            default:
                break;
        }
    }
}
