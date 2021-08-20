#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
import subprocess
import sys

def exec_shell_command(command):
  split_command = command.split()
  out = subprocess.Popen(split_command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
  result, err = out.communicate()

  return result.decode()

def extract_pr_number(pr_line):
  return pr_line.split('\t')[0]

def check_not_empty(string):
  return len(string) > 0

def check_review_app_prefix(review_app):
  return review_app.startswith("simple-mob-pr-")

def generate_review_app_name(pr_number):
  return "simple-mob-pr-" + pr_number

open_prs = set(filter(check_not_empty, map(extract_pr_number, exec_shell_command("gh pr list").split("\n"))))

heroku_review_apps = set(filter(check_not_empty, filter(check_review_app_prefix, exec_shell_command("heroku apps --team=resolvetosavelives").split("\n"))))

required_server_apps = set(map(generate_review_app_name, open_prs))

review_apps_to_delete = heroku_review_apps - required_server_apps

for review_app in review_apps_to_delete:
  print("deleting {heroku_app_name}...".format(heroku_app_name = review_app))
  print(exec_shell_command("heroku apps:destroy --app={heroku_app_name} --confirm={heroku_app_name}".format(heroku_app_name = review_app)))
