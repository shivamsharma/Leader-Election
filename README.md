# LEADER ELECTION USING ZOOKEEPER
This project is about leader election using [zookeeper](https://zookeeper.apache.org/) in which each jvm process will
be leader or followers or observer. There can be only one leader at time and when that leader goes down then any of
follower should became leader. Observer should not take participate in election but they need a leader too and without
leader they should complain.

**Requirement**:

1. Java-1.7

2. Maven-3.x.x(To build project)

3. Zookeeper-3.4.6

## Usage
### Build project

    mvn clean package
This will create target directory with executable jar file(leader-election-1.0-jar-with-dependencies.jar).

### Start zookeeper
You can read about zookeeper from [here](https://zookeeper.apache.org/doc/trunk/index.html). From its getting started
[page](https://zookeeper.apache.org/doc/trunk/zookeeperStarted.html) you can bring up zookeeper server. By default zookeeper should
be started on `2181` port and ip address will be host address of zookeeper.

### Submit Node
You can start node by following command. Replace zookeeper host and zookeeper port.

    java -jar leader-election-1.0-jar-with-dependencies.jar Ironman zookeeper_host:zookeeper_port   # Ironman node
    java -jar leader-election-1.0-jar-with-dependencies.jar Hulk zookeeper_host:zookeeper_port   # Hulk node

Same name will not effect functionality but it is advised to choose unique name so that it will easy to distinguish
which is leader now. By starting above node in order, Ironman will became leader and Hulk will became follower and when
Ironman will be down then Hulk will became new leader and so on.

### Observer Node

    java -jar leader-election-1.0-jar-with-dependencies.jar Batman zookeeper_host:zookeeper_port observer     # Batman observer
