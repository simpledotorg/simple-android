#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import re
import os
from xml.etree import ElementTree


class CommentedTreeBuilder(ElementTree.TreeBuilder):
    def comment(self, data):
        self.start(ElementTree.Comment, {})
        self.data(data)
        self.end(ElementTree.Comment)

def replace_ellipsis(text):
    return text.replace("...", android_ellipsis)

def process_string(resource):
    resource.text = replace_ellipsis(text=resource.text)


def process_plural(resource):
    for item in resource:
        item.text = replace_ellipsis(text=item.text)

def process_strings_file(path):
    print("Checking strings file at -> {}".format(path))

    parser = ElementTree.XMLParser(target=CommentedTreeBuilder())
    resources_tree = ElementTree.parse(path, parser=parser)
    resources_root = resources_tree.getroot()

    for resource in resources_root:
        if resource.tag == 'string':
            process_string(resource=resource)
        elif resource.tag == 'plurals':
            process_plural(resource=resource)
        else:
            "Ignoring any other tags, most likely comments"

    print("Write {}".format(path))
    resources_tree.write(path, encoding='utf-8', xml_declaration=True)

    print("Adding new line at end of {}".format(path))
    with open(path, "a") as strings_file:
        strings_file.write("\r\n")


ElementTree.register_namespace('tools', 'http://schemas.android.com/tools')

seprator = ",\r\n"
rootdir = "/app/src/main/res"
strings_file_regix = re.compile('(strings.*xml$)')

strings_file_paths = []
for root, dirs, files in os.walk(os.getcwd() + rootdir):
    for file in files:
        if re.match(strings_file_regix, file):
            strings_file_paths.append("{}/{}".format(root, file))

print("Processing strings:")
print(seprator.join(strings_file_paths) + "\r\n")

android_ellipsis = u"\u2026"

for path in strings_file_paths:
    process_strings_file(path)

print("Done!")
