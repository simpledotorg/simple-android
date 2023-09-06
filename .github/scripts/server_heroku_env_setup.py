#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import json
import requests
import requests.auth

class BearerAuth(requests.auth.AuthBase):
    bearer_token = ''

    def __init__(self, token):
        self.bearer_token = token

    def __call__(self, r):
        r.headers['Authorization'] = 'Bearer {api_token}'.format(api_token = self.bearer_token)
        return r

env_file = open(sys.argv[1])
env_json = json.loads(env_file.read())
env_file.close()

server_config = env_json['env']
server_vars = list(filter(lambda item: 'value' in item[1], server_config.items()))
server_vars_map = dict(map(lambda item: (item[0], item[1]['value']), server_vars))

additional_properties_map = dict(map(lambda item: item.split('=', 1), sys.argv[4].split('\n')))
server_vars_map.update(additional_properties_map)

heroku_app_name = sys.argv[2]
heroku_api_token = sys.argv[3]

print('Heroku app url with uid: {app_name}'.format(app_name = heroku_app_name))

server_vars_map['SIMPLE_SERVER_HOST'] = '{app_name}.herokuapp.com'.format(app_name = heroku_app_name)
server_vars_map['SIMPLE_SERVER_ENV'] = 'android_review'

print('Setting Heroku config variables: {var_names}'.format(var_names = list(server_vars_map.keys())))

request_headers = {
    'Accept': 'application/vnd.heroku+json; version=3'
}

response = requests.patch(
    url = 'https://api.heroku.com/apps/{app_name}/config-vars'.format(app_name = heroku_app_name),
    auth = BearerAuth(heroku_api_token),
    json = server_vars_map,
    headers = request_headers
)

response.raise_for_status()
