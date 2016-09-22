#!/bin/bash
cd "$(dirname "$(realpath "$0")")"; # set script location as working directory
java -cp bin/:lib/* arden.MainClass "$@"
