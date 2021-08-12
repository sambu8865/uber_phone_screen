import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class Scratch {

  public static void main(String[] args) throws InterruptedException {
    Processor processor = new Processor(20);
    List<String> ips = new ArrayList<>();
    for (int i=0; i<10000; i++) {
      ips.add("ip-"+i);
    }
    System.out.println(processor.calculate(ips));
  }
}

class Processor {
  private Queue<String> queue;
  private int maxThreadCount;
  private Worker[] workers;
  private long freeSpace;

  public Processor(int threadCount) {
    this.maxThreadCount = threadCount;
    this.workers = new Worker[threadCount];
    this.freeSpace = 0l;
    this.queue = new LinkedList<>();
  }

  public long calculate(List<String> ips) throws InterruptedException {
    for (int i=0; i<maxThreadCount; i++) {
      workers[i] = new Worker(this, "worker-"+i, i);
    }

    for (String ip : ips) {
      queue.offer(ip);
    }

    for (Worker worker : workers) {
      worker.start();
    }

    for (Worker worker : workers) {
      worker.join();
    }

    return freeSpace;
  }

  public synchronized String getJob() {
    if (queue.isEmpty()) {
      return null;
    }
    return queue.poll();
  }

  public synchronized void requeue(String job) {
    queue.offer(job);
  }

  public synchronized void aggregate(long space) {
    freeSpace += space;
  }
}

class Worker extends Thread {
  private Processor parent;
  private String workerName;
  private int workerIndex;

  public Worker(Processor parent, String workerName, int indx) {
    this.parent = parent;
    this.workerName = workerName;
    this.workerIndex = indx;
  }

  public void run() {
    String job = parent.getJob();
    while (job != null) {
      try {
        int space = 1;
        if (workerIndex == 15) {
          space = -1;
        }
        System.out.println(workerName+ " processing "+job);
        Thread.sleep(50);
        if (space == -1) {
          System.out.println("failed-"+job);
          parent.requeue(job);
        } else
          parent.aggregate(1);
        job = parent.getJob();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
