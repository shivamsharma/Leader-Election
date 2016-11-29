package ss.leaderelection;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Node, a java process which will participate in leader election and out of all workers there will be
 * one node which will act as leader and others as followers.
 * <p>
 * Author : Shivam Sharma
 * Date : 7/26/2016
 */
public class Node {
    private Logger logger = Logger.getLogger(Node.class);
    private final static String rootPath = "/nodes";    // Root node path
    private String name;                // Name of node
    private ZooKeeper zooKeeper;
    private String LEADER = null;       // Leader of workers

    public Node(String name, String zooKeeperAddr) throws IOException {
        this.name = name;
        logger.info("Zookeeper object is created. Addr: " + zooKeeperAddr + " and name of node: " + name);
        // Creating the instance of zookeeper with watcher which is monitoring if leader is down or not
        zooKeeper = new ZooKeeper(zooKeeperAddr, 60000, new LeaderWatcher());
    }

    /**
     * Electing leader from available workers.
     *
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void electLeader() throws KeeperException, InterruptedException, IOException {
        List<String> children = zooKeeper.getChildren(rootPath, false);
        // Sorting on the basis of name of workers.
        Collections.sort(children);
        if (children.size() == 0) {
            logger.error("No Leader is elected because of children size 0");
        } else {
            // Assign leader
            LEADER = children.get(0);
            String path = rootPath + "/" + LEADER;
            String data = new String(zooKeeper.getData(path, true, null));
            logger.info(LEADER + " is a Leader whose name is " + data);
        }
    }

    /**
     * Create node.
     *
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     */
    public void run() throws KeeperException, InterruptedException, IOException {
        // Create root path of workers if not exist which is persistent in nature
        if (zooKeeper.exists(rootPath, false) == null) {
            logger.info("Root path " + rootPath + " doesn't exist");
            logger.info("Creating root path...");
            zooKeeper.create(rootPath, new Date().toString().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
        }
        String currentNodePath = rootPath + "/node";
        // Create node with data `name` inside and znode type Ephemeral Sequential
        zooKeeper.create(currentNodePath, name.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        logger.info("Node with name: " + name + " is added");
        // Assign Leader to current node
        electLeader();
    }

    /**
     * Watcher implementation which is monitoring whether Leader is down or not. If leader goes down
     * then it will elect the new leader.
     */
    private class LeaderWatcher implements Watcher {
        public void process(WatchedEvent watchedEvent) {
            logger.info("Watcher Invoked");
            // Check whether event invoked is deletion of Leader node or not
            if (watchedEvent.getType().equals(Event.EventType.NodeDeleted)
                    && watchedEvent.getPath().equals(rootPath + "/" + LEADER)) {
                try {
                    logger.info("Leader is offline, electing another leader");
                    // If leader is down then elect new leader out of available workers
                    electLeader();
                } catch (KeeperException | InterruptedException | IOException e) {
                    logger.error("Error in electing leader");
                    logger.trace(e);
                }
            }
        }
    }
}
