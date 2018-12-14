package org.yardstickframework.runners.workers.node;

import java.io.IOException;
import java.util.List;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;

public class DockerCollectWorker extends DockerNodeWorker {

    public DockerCollectWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx, nodeList);
    }

    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        NodeType type = nodeInfo.getNodeType();

        String host = nodeInfo.getHost();

        String id = nodeInfo.getId();

        String contName = String.format("YARDSTICK_%s_%s", type, id);

        String nodeOutDir = String.format("%s/output", runCtx.getRemWorkDir());

        String mkdirCmd = String.format("mkdir -p %s", nodeOutDir);

        String cpCmd = String.format("cp %s:%s/output %s", contName, runCtx.getRemWorkDir(), runCtx.getRemWorkDir());

        CommandHandler hndl = new CommandHandler(runCtx);

//        System.out.println(cpCmd);

        log().info(String.format("Collecting data from the container '%s' on the host '%s'.", contName, host));

        try {
            hndl.runCmd(host, mkdirCmd);

            hndl.runDockerCmd(host, cpCmd);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}