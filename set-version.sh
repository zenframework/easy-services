#!/bin/sh
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$1

