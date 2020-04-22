#!/bin/bash

sourceFile=${1}
targetFile=${2}

sourceChecksum=($(shasum ${sourceFile}))
targetChecksum=($(shasum ${targetFile}))

if [[ "$targetChecksum" == "$sourceChecksum" ]]; then
  >&2 echo "$changedFile was not changed!"
	exit 1
else
	exit 0
fi
