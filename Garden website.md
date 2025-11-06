# Garden website

https://www.howtographql.com/basics/3-big-picture/
https://graphql.org/code/
https://www.graphql-java.com/tutorials/getting-started-with-spring-boot/
https://www.graphql-java.com/blog/spring-for-graphql


https://start.spring.io/
https://docs.spring.io/spring-graphql/reference/index.html

https://www.ibm.com/blog/database-deep-dives-janusgraph/

https://www.ibm.com/topics/mongodb


https://neo4j.com/developer-blog/graphql-development-best-practices/


https://tinkerpop.apache.org/docs/current/
https://kelvinlawrence.net/book/Gremlin-Graph-Guide.html


## The problem

I want to create an application a person might use when exploring plants to plant in their garden. In my case, I want to create a catalogue of plants and invite users to take photos of plants they see in gardens in their area. I want to pin the photo to a plant species and a location. Users can then search for a plant and the app will return photos that is has of the plant in their area. The purpose of knowing the location is so that the user can then go to that location and see the plant for themselves rather than just relying on a photo

### Domain Driven Design - Event Storming

- https://miro.com/app/board/uXjVJvaToYI=/

Events
- A photo of a plant is taken at a location
- A species of plant is identified at a location
- A species of plant is added to the plant catalog
- A photo of a plant is connected to a plant at a location
- The various locations of a plant are shown
- A location is visited
- The plants at a particular location are shown

## Other stuff

kali linux

wsl --install kali-linux

sudo apt update
sudo apt full-upgrade

sudo usermod -s /usr/bin/zsh jamie

sudo apt install -y docker.io
sudo service docker start


Cassandra
- see https://docs.janusgraph.org/storage-backend/cassandra/
- see https://github.com/JanusGraph/janusgraph/releases
- see https://hub.docker.com/_/cassandra/

sudo docker pull cassandra:latest

sudo docker run -d --net=graphdb_bridge -p 7001:7001 -p 7199:7199 -p 9042:9042 -p 9160:9160 -v /var/lib/cassandra:/var/lib/cassandra --name cassandra cassandra:latest

sudo docker run -it --net=graphdb_bridge -p 7001:7001 -p 7199:7199 -p 9042:9042 -p 9160:9160 -v /var/lib/cassandra:/var/lib/cassandra --name cassandra cassandra:latest


JanusGraph

https://github.com/JanusGraph/janusgraph/releases
https://docs.janusgraph.org/getting-started/installation/
https://github.com/JanusGraph/janusgraph/discussions
https://discord.com/channels/981533699378135051/981533699378135054


Download https://github.com/cdarlint/winutils/raw/master/hadoop-2.8.5/bin/winutils.exe
Place it under c:\dev\tools\janusgraph-1.0.0\bin\winutils.exe


Docker version
- see https://hub.docker.com/r/janusgraph/janusgraph
- see https://github.com/JanusGraph/janusgraph-docker
- The container will launch the JanusGraphServer with a Java heap size set to 4Gb. For now,
  we dont have enough RAM in the lenovo laptop to do this

sudo docker pull janusgraph/janusgraph:latest

sudo docker run --name janusgraph janusgraph/janusgraph:latest
sudo docker run -it --name janusgraph janusgraph/janusgraph:latest

sudo docker run -it --name janusgraph --net=graphdb_bridge -p 8182:8182 -e JANUS_PROPS_TEMPLATE=cql -v /mnt/c/dev/tools/janusgraph-1.0.0/scripts/empty-sample.groovy:/opt/janusgraph/scripts/empty-sample.groovy -v /mnt/c/dev/tools/janusgraph-1.0.0/conf/jvm-11.options:/opt/janusgraph/conf/jvm-11.options janusgraph/janusgraph:latest

sudo docker run --rm --link janusgraph:janusgraph -e GREMLIN_REMOTE_HOSTS=janusgraph -it janusgraph/janusgraph:latest ./bin/gremlin.sh



Command

set JAVA_HOME=c:\dev\tools\jdk-21.0.2+13
set JAVA_HOME=c:\dev\tools\jdk-16.0.2+7
set JANUSGRAPH_HOME=c:\dev\tools\janusgraph-1.0.0
set Path=%JAVA_HOME%\bin;%Path%

cd %JANUSGRAPH_HOME%\bin

.\gremlin.bat


PowerShell

$env:JAVA_HOME = "c:\dev\tools\jdk-21.0.2+13"
$env:JANUSGRAPH_HOME = "c:\dev\tools\janusgraph-1.0.0"
$env:Path = "$env:JAVA_HOME\bin;" + $ENV:Path 

cd $env:JANUSGRAPH_HOME\bin

.\gremlin.bat



https://resources.austplants.com.au/plant/banksia-ericifolia/




