package org.yardstickframework.runners.workers;

public class CheckWorkResult implements WorkResult {
    private boolean exit;

    /**
     * @return Exit.
     */
    public boolean exit() {
        return exit;
    }

    /**
     * @param exit New exit.
     */
    public void exit(boolean exit) {
        this.exit = exit;
    }
}