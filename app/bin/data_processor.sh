#!/bin/bash
# Usage:
#   data_processor.sh <config filename>

if [ $# -lt 1 ]; then
  echo "Usage: $0 <config filename>" >&2
  exit 1
fi

umask 022

script_dir="$( cd -P "$( dirname "$0" )" && pwd )"/..
pushd $script_dir > /dev/null

java=/usr/bin/java
conf_dir="conf"
conf_file=$1
lib_dir=lib

log_dir=log
mkdir -p ${log_dir}

app_name=data-processor

options="-Djava.util.logging.config.file=logging.properties -Djava.net.preferIPv4Stack=true -Xmx1g"

main_class=org.ab.kafka.processor.FilmProcessor

lib_jars=; for i in ${lib_dir}/*.jar; do lib_jars=$lib_jars:$i; done;
lib_dep_jars=; for i in ${lib_dir}/dependencies/*.jar; do lib_dep_jars=$lib_dep_jars:$i; done;

jars=${lib_jars}${lib_dep_jars}
classpath="${conf_dir}:${jars}"

command="$java $options -cp $classpath $main_class -c ${conf_dir}/${conf_file}"
echo -e "$command \n"
exec $command

popd > /dev/null
