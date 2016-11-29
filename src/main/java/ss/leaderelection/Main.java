package ss.leaderelection;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;

/**
 * Main entry point of leader election application
 * <p>
 * Author : Shivam Sharma
 * Date : 7/25/2016
 */
public class Main {
    private static Logger logger = Logger.getLogger(Main.class);
    private static String nodeName;
    private static String zookeeperAddr;
    private static boolean isObserver = false;

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        if (args.length < 2) {
            logger.error("Usage: For adding node to cluster");
            logger.error("\tjava -jar leader-election-1.0-jar-with-dependencies.jar name zookeeper_host:zookeeper_port");
            logger.error("Usage: For adding observer node: ");
            logger.error("\tjava -jar leader-election-1.0-jar-with-dependencies.jar name zookeeper_host:zookeeper_port observer");
            System.exit(1);
        }
        nodeName = args[0];
        zookeeperAddr = args[1];
        if (args.length >= 3 && args[2].equals("observer")) {
            isObserver = true;
        }
        if (isObserver) {
            new ObserverNode(nodeName, zookeeperAddr).run();
        } else {
            new Node(nodeName, zookeeperAddr).run();
        }
        waitIndefinitely();
    }

    static void waitIndefinitely() throws InterruptedException {
        while (true) Thread.sleep(60000);
    }
}
