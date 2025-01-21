#!/bin/bash

link="$1"

story_regex="https:\/\/app.shortcut.com\/simpledotorg\/story\/[0-9]+(?:\/.*)?\$"

if [[ "$link" =~ $story_regex ]]; then
  echo "PR description contains a valid story link."
  exit 0
else
  echo "PR description doesn't contain any valid story links."
  exit 1
fi
