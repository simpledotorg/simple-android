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
    if resource.text.find("...") >= 0:
        resource.text = replace_ellipsis(text=resource.text)
        return resource.attrib['name']


def process_plural(resource):
    for item in resource:
        if item.text.find("...") >= 0:
            item.text = replace_ellipsis(text=item.text)
            return resource.attrib['name']


def process_strings_file(path):
    print("Checking strings file at -> {}".format(path))

    parser = ElementTree.XMLParser(target=CommentedTreeBuilder())
    resources_tree = ElementTree.parse(path, parser=parser)
    resources_root = resources_tree.getroot()
    fixed_ellipsis_attrib = []

    for resource in resources_root:
        if resource.tag == 'string':
            attrib_name = process_string(resource=resource)
            if attrib_name is not None:
                fixed_ellipsis_attrib.append(attrib_name)
        elif resource.tag == 'plurals':
            attrib_name = process_plural(resource=resource)
            if attrib_name is not None:
                fixed_ellipsis_attrib.append(attrib_name)
        else:
            "Ignoring any other tags, most likely comments"

    fixed_ellipsis_count = len(fixed_ellipsis_attrib)
    if fixed_ellipsis_count > 0:
        "Done fixing the xml file, write the file back"
        resources_tree.write(path,
                             encoding='utf-8', xml_declaration=True)
        "Adding new line at end of the file"
        with open(path, "a") as strings_file:
            strings_file.write("\r\n")

        if fixed_ellipsis_count > 1:
            print("Fixed ellipsis for {} strings. \r\n{}\r\n".format(
                fixed_ellipsis_count, seprator.join(fixed_ellipsis_attrib)))
        else:
            print("Fixed ellipsis for {} string. \r\n{}\r\n".format(
                fixed_ellipsis_count, seprator.join(fixed_ellipsis_attrib)))
    else:
        print("File doesn't have any ellipsis issues!\r\n")


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
