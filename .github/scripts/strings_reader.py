#!/usr/bin/env python3
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

def noop_processor(resource):
        print("Ignoring any other tags, most likely comments")

def string_processor(resource):
    original = resource.text
    resource.text = transform_text(resource.text)
    if original != resource.text:
        print("{3}> {0} : {1} -> {2}".format(resource.attrib['name'], original, resource.text, indent_char))

def plural_processor(resource):
    for item in resource:
        original = item.text
        item.text = transform_text(item.text)
        if original != item.text:
            formatted_name = "{0}.{1}".format(resource.attrib['name'], item.attrib['quantity'])
            print("{3}> {0} : {1} -> {2}".format(formatted_name, original, item.text, indent_char))

def replace_ellipsis(text):
    return text.replace("...", ellipsis_unicode)

def escape_characters(text):
    text_as_list = list(text)

    for index, character in enumerate(text_as_list):
        if(character in strings_to_escape):
            if(index == 0 or text_as_list[index - 1] != escape_char):
                text_as_list.insert(index, escape_char)
        
    return ''.join(text_as_list)

def transform_text(text):
    return replace_ellipsis(escape_characters(text))

class ResourceTagProcessor(dict):

    def __init__(self, *args):
        dict.__init__(self, args)
        self['string'] = string_processor
        self['plurals'] = plural_processor

    def __missing__(self, key):
        return noop_processor


def process_strings_file(path):
    print("\n\n----- BEGIN: {} -----\n".format(path))

    parser = ElementTree.XMLParser(target=CommentedTreeBuilder())
    resources_tree = ElementTree.parse(path, parser=parser)
    resources_root = resources_tree.getroot()

    processor = ResourceTagProcessor()

    for resource in resources_root:
        processor[resource.tag](resource)

    print()
    print("{}Writing transformed resources to file{}".format(indent_char, ellipsis_unicode))
    resources_tree.write(path, encoding='utf-8', xml_declaration=True)

    print("{}Adding new line at end of file{}".format(indent_char, ellipsis_unicode))
    with open(path, "a") as strings_file:
        strings_file.write("\r\n")

    print("\n----- END: {} -----\n".format(path))


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

ellipsis_unicode = "\u2026"
strings_to_escape = ["\"", "\'"]
escape_char = '\\'
indent_char = '  '

for path in strings_file_paths:
    process_strings_file(path)
