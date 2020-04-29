#!/bin/bash

sourceDirectory=${1}
targetDirectory=${2}

changedFile=

# shellcheck disable=SC2045
for file in $(ls $sourceDirectory)
do
	sourceFile="$sourceDirectory/$file"
	sourceChecksum=($(shasum ${sourceFile}))

	targetFile="$targetDirectory/$file"
	targetChecksum=($(shasum ${targetFile}))

	if [[ "$targetChecksum" != "$sourceChecksum" ]]; then
		changedFile=$file
		break
	fi
done

if [[ -z $changedFile ]]; then
	exit 0
else
	>&2 echo "$changedFile was changed!"
	exit 1
fi
