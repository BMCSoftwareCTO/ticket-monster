#!/bin/bash

# Usage: execute.sh [WildFly mode] [configuration file]
#
# The default mode is 'standalone' and default configuration is based on the
# mode. It can be 'standalone.xml' or 'domain.xml'.

JBOSS_HOME=/opt/jboss/wildfly
JBOSS_CLI=$JBOSS_HOME/bin/jboss-cli.sh
JBOSS_MODE=${1:-"standalone"}
JBOSS_CONFIG=${2:-"$JBOSS_MODE.xml"}
TM_DB_HOST=${DB_HOST}
TM_DB_PORT=${DB_PORT:-"5432"}
TM_DB_NAME=${DB_NAME:-"ticketmonster"}
TM_DB_USER=${DB_USER:-"ticketmonster"}
TM_DB_PASSWORD=${DB_PASSWORD:-"ticketmonster-docker"}

function wait_for_server() {
  until `$JBOSS_CLI -c "ls /deployment" &> /dev/null`; do
    sleep 1
  done
}

echo "=> Starting WildFly server"
$JBOSS_HOME/bin/$JBOSS_MODE.sh -c $JBOSS_CONFIG -Dmodcluster.host=127.0.0.1 > /dev/null &

echo "=> Waiting for the server to boot"
wait_for_server

echo "=> Executing the commands"
#$JBOSS_CLI -c --file=`dirname "$0"`/commands.cli

# Mark the commands below to be run as a batch
$JBOSS_CLI -c << EOF
batch

# Add Postgres JDBC Driver as a module
module add --name=org.postgresql --resources=/opt/jboss/wildfly/customization/postgresql-9.4-1201.jdbc41.jar --dependencies=javax.api,javax.transaction.api

#Add PostgreSQL JDBC Driver
/subsystem=datasources/jdbc-driver=postgres:add(driver-name=postgres, driver-module-name=org.postgresql, driver-class-name=org.postgresql.Driver)

#Add Datasource
data-source add --name=TicketMonsterPostgreSQLDS --jndi-name=java:jboss/datasources/TicketMonsterPostgreSQLDS --driver-name=postgres --connection-url=jdbc:postgresql://$TM_DB_HOST:$TM_DB_PORT/$TM_DB_NAME --user-name=$TM_DB_USER --password=$TM_DB_PASSWORD --valid-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker --exception-sorter-class-name=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter --validate-on-match=true --background-validation=true

#Execute the batch
run-batch
EOF

echo "=> Shutting down WildFly"
if [ "$JBOSS_MODE" = "standalone" ]; then
  $JBOSS_CLI -c ":shutdown"
else
  $JBOSS_CLI -c "/host=*:shutdown"
fi

