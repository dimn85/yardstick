package org.yardstickframework.runners.workers.host;

import java.io.IOException;
import java.util.List;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

public class KillWorker extends HostWorker {

    public KillWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);
    }

    @Override public WorkResult doWork(String host, int cnt) {
        CommandHandler hndl = new CommandHandler(runCtx);

        String killServCmd = "pkill -9 -f \"Dyardstick.server\"";
        String killDrvrCmd = "pkill -9 -f \"Dyardstick.driver\"";

        try {
            hndl.runCmd(host, killServCmd);

            hndl.runCmd(host, killDrvrCmd);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }


        return null;
    }

    public NodeInfo killNode(NodeInfo nodeInfo){
        CommandHandler hndl = new CommandHandler(runCtx);

        NodeInfo res = nodeInfo;

        try {
            res = hndl.killNode(nodeInfo);
        }
        catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}