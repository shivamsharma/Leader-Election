package ss.leaderelection;


import org.apache.log4j.Logger;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Observer node which cannot take participate in election of leader. This node will complain if there is no
 * leader present and retrying till it gets a leader
 * <p>
 * Author : Shivam Sharma
 * Date : 7/26/2016
 */
public class ObserverNode {
    private static Logger logger = Logger.getLogger(ObserverNode.class);
    private final static String rootPath = "/nodes";
    private final static String observerPath = "/observer_nodes";
    private String name;
    private ZooKeeper zooKeeper;
    private String LEADER = null;

    public ObserverNode(String name, String zooKeeperAddr) throws IOException {
        this.name = name;
        logger.info("Zookeeper object is created. Addr: " + zooKeeperAddr + " and name of node: " + name);
        zooKeeper = new ZooKeeper(zooKeeperAddr, 60000, new LeaderWatcher());
    }

    /**
     * Check whether leader exists or not.
     *
     * @return true or false whether leader is present or not
     * @throws KeeperException
     * @throws InterruptedException
     */
    boolean leaderExists() throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(rootPath, false);
        // Sort to get leader
        Collections.sort(children);
        // If no leader is present then return false
        if (children.size() == 0) {
            logger.error("No leader exists");
            return false;
        }
        // Assign leader
        LEADER = children.get(0);
        String path = rootPath + "/" + LEADER;
        String data = new String(zooKeeper.getData(path, true, null));
        logger.info(LEADER + " is a alive Leader whose name is " + data);
        return true;
    }

    void run() throws KeeperException, InterruptedException {
        // Create observer path if not present
        if (zooKeeper.exists(observerPath, false) == null) {
            logger.info("Observer path " + observerPath + " doesn't exist");
            logger.info("Creating observer path...");
            zooKeeper.create(observerPath, new Date().toString().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
        }
        String currentNodePath = observerPath + "/node";
        // Check and retry till leader is up
        while (!leaderExists()) {
            logger.error("Retrying after 5 seconds....");
            Thread.sleep(5000);
        }
        // Create ephemeral sequential znodes in observer path
        zooKeeper.create(currentNodePath, name.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        logger.info("Observer Node with name: " + name + " is added");
    }

    /**
     * Watcher implementation which is monitoring whether Leader is down or not. If leader goes down
     * then it will wait till new leader is elected.
     */
    private class LeaderWatcher implements Watcher {
        @Override
        public void process(WatchedEvent watchedEvent) {
            logger.info("Observer Watcher Invoked");
            if (watchedEvent.getType().equals(Event.EventType.NodeDeleted)
                    && watchedEvent.getPath().equals(rootPath + "/" + LEADER)) {
                try {
                    while (!leaderExists()) {
                        logger.error("Retrying after 5 seconds....");
                        Thread.sleep(5000);
                    }
                } catch (KeeperException | InterruptedException e) {
                    logger.error("Error in watching leader");
                    logger.trace(e);
                }
            }
        }
    }
}
