package org.yardstickframework.runners.workers.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.workers.Worker;

/**
 * Parent class for node workers.
 */
public abstract class NodeWorker extends Worker {
    /** Main list of NodeInfo objects to work with. */
    private List<NodeInfo> nodeList;

    /** Result list */
    private List<NodeInfo> resNodeList;

    /** Flag indicating whether or not task should run on the same host at the same time.*/
    private boolean runAsyncOnHost;

    /**
     * Constructor.
     *
     * @param runCtx Run context.
     * @param nodeList Main list of NodeInfo objects to work with.
     */
    NodeWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx);
        this.nodeList = new ArrayList<>(nodeList);
        resNodeList = new ArrayList<>(nodeList.size());
    }

    /**
     * Executes actual work for node.
     *
     * @param nodeInfo {@code NodeInfo} object to work with.
     * @return {@code NodeInfo} result.
     * @throws InterruptedException
     */
    public abstract NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException;

    /**
     *
     * @return {@code int} Number of objects in the main list.
     */
    int getNodeListSize() {
        return nodeList.size();
    }

    /**
     * Executes doWork() method defined in worker class asynchronously.
     */
    public List<NodeInfo> workForNodes() {
        beforeWork();

        ExecutorService execServ = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        Collection<Future<NodeInfo>> futList = new ArrayList<>(nodeList.size());

        final Map<String, Semaphore> semMap = new HashMap<>();

        for (final NodeInfo nodeInfo : nodeList)
            semMap.put(nodeInfo.getHost(), new Semaphore(1));

        for (final NodeInfo nodeInfo : nodeList) {
            futList.add(execServ.submit(new Callable<NodeInfo>() {
                @Override public NodeInfo call() throws Exception {
                    String host = nodeInfo.getHost();

                    Thread.currentThread().setName(threadName(nodeInfo));

                    if(!runAsyncOnHost)
                        semMap.get(host).acquire();

                    final NodeInfo res = doWork(nodeInfo);

                    semMap.get(host).release();

                    return res;
                }
            }));
        }

        for (Future<NodeInfo> f : futList) {
            try {
                NodeInfo nodeInfo = f.get(DFLT_TIMEOUT, TimeUnit.MILLISECONDS);

                resNodeList.add(nodeInfo);
            }
            catch (InterruptedException e) {
                for (Future<NodeInfo> f0 : futList)
                    f0.cancel(true);

                Thread.currentThread().interrupt();

                log().info(String.format("%s stopped.", getWorkerName()));

                log().debug(e.getMessage(), e);

                break;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        execServ.shutdown();

        afterWork();

        return new ArrayList<>(resNodeList);
    }

    /**
     * @return Response node list.
     */
    public List<NodeInfo> resNodeList() {
        return resNodeList;
    }

    /**
     * @return Node list.
     */
    public List<NodeInfo> nodeList() {
        return nodeList;
    }

    /**
     * @return Run async on host.
     */
    public boolean runAsyncOnHost() {
        return runAsyncOnHost;
    }

    /**
     * @param runAsyncOnHost New run async on host.
     */
    public void runAsyncOnHost(boolean runAsyncOnHost) {
        this.runAsyncOnHost = runAsyncOnHost;
    }
}