#!/bin/bash

CLOUD="cloud"
DISPATCHER="dispatcher"
GENEGATOR="datagenerator"
MERGER="merger"
CHECKER="checker"
SERVERNODE="servernode"
CONSUMER="consumer" 

USAGE="usage: start.sh [cls | d | b | ".$CLOUD.$DISPATCHER.$GENEGATOR.$MERGER.$CHECKER.$SERVERNODE.$CONSUMER." ]"

#heap space jvm
#FLAG=-Xmx512m
FLAG=-Xmx2g

OUTPUT_FOLDER=output
METRICS_FOLDER=metrics2
METRICS_FOLDER=metrics3

APP_NAME=fresque-1.0.jar
SCRIPT=start.sh
PROPERTIES_DIR=properties

#classpath for IoT

CLASSPATH_IOT_CLOUD1=iot.cloud1
CLASSPATH_IOT_CLOUD2=iot.cloud2
CLASSPATH_IOT_COLLECTOR=iot.collector
CLASSPATH_IOT_CONSUMER=iot.consumer

#classpath for pined(2)

CLASSPATH_SOURCE=main
CLASSPATH_DATASOURCE=main.datasource
CLASSPATH_CLOUD=main.cloud
CLASSPATH_COLLECTOR=main.collector
CLASSPATH_CONSUMER=main.consumer

#classpath for distributing 2
CLASSPATH_DATASOURCE_2=main.datasource.distributing2
CLASSPATH_CLOUD_2=main.cloud.indextemplate.distributing2
CLASSPATH_MERGER=main.collector.indextemplate.distributing2.merger
CLASSPATH_CHECKER=main.collector.indextemplate.distributing2.checker
CLASSPATH_DISPATCHER=main.collector.indextemplate.distributing2.dispatcher
CLASSPATH_SERVERNODE_2=main.collector.indextemplate.distributing2.servernode

#classpath for distributing 3
CLASSPATH_DATASOURCE_3=main.datasource.distributing3
CLASSPATH_CLOUD_3=main.cloud.indextemplate.distributing3
CLASSPATH_MERGER_3=main.collector.indextemplate.distributing3.merger
CLASSPATH_CHECKER_3=main.collector.indextemplate.distributing3.checker
CLASSPATH_DISPATCHER_3=main.collector.indextemplate.distributing3.dispatcher
CLASSPATH_SERVERNODE_3=main.collector.indextemplate.distributing3.servernode

################################
# check command line arguments #
################################
if [ $# = 0 ]
then
	echo $USAGE
	exit;
fi

#####################
# Set up  VARIABLE ENVIRONMENT
#####################

if [ $1 = "cls" ]
then
	if [ -d "$OUTPUT_FOLDER" ]; then
	    echo "Deleting folder " $OUTPUT_FOLDER
	    rm -R $OUTPUT_FOLDER
	fi
	if [ -d "$METRICS_FOLDER" ]; then
	    echo "Deleting folder " $METRICS_FOLDER
	    rm -R $METRICS_FOLDER
	fi
	exit
fi

if [ $1 = "b" ]
then
       echo "#Start building ..."
       mvn -f ./pom.xml clean compile assembly:single 
#        mvn -f $CLASSPATH_SOURCE/pom.xml  
	echo "#Copying BigSDB-1-jar-with-dependencies.jar to current folder"
       cp ./target/BigSDB-1-jar-with-dependencies.jar ./$APP_NAME
	exit;
fi

if [ $1 = "cloud" ]
then
	echo "#Start cloud ..."
        java $FLAG -classpath $APP_NAME $CLASSPATH_CLOUD_3.Cloud
        exit;
fi

if [ $1 = "merger" ]
then
        echo "#Start merger ..."
        java $FLAG -classpath $APP_NAME $CLASSPATH_MERGER_3.Merger
        exit;
fi

if [ $1 = "checker" ]
then
        echo "#Start checker ..."
        java $FLAG -classpath $APP_NAME $CLASSPATH_CHECKER_3.Checker
        exit;
fi

if [ $1 = "dispatcher" ]
then
        echo "#Start dispatcher ..."
        java $FLAG -classpath $APP_NAME $CLASSPATH_DISPATCHER_3.Dispatcher
        exit;
fi

if [ $1 = "servernode" ]
then
        echo "#Start  server node ..."
        java -classpath $APP_NAME $CLASSPATH_SERVERNODE_3.ServerNode
        exit;
fi

if [ $1 = "datagenerator" ]
then
        echo "#Start data generator ..."
        java $FLAG -classpath $APP_NAME $CLASSPATH_DATASOURCE_3.DataGenerator
        exit;
fi

if [ $1 = "d" ]
then
        echo "#Distributing ..."
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR" to data generator ..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR  ubuntu@10.0.1.26:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to cloud ..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.74:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to merger..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.97:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to checker..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.99:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to dispatcher..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.75:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 1 ..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.79:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 2 ..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.78:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 3..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.77:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 4..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.90:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 5..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.80:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 6..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.92:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 7..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.82:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 8..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.81:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 9..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.93:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 10..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.85:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 11..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.84:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 12..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.94:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 13..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.89:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 14..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.83:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 15..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.95:	
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 16..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.87:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 17..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.91:
	echo "#Copying " $APP_NAME $SCRIPT $PROPERTIES_DIR " to computing node 18..."
        rsync -Pav -e "ssh -i key-galactica.pem"  $APP_NAME $SCRIPT $PROPERTIES_DIR ubuntu@10.0.1.96:
exit;
fi

echo $USAGE
