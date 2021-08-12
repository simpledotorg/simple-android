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

heroku_app_name = sys.argv[2]
heroku_api_token = sys.argv[3]

additional_heroku_properties = sys.argv[4::]
additional_properties_map = dict(map(lambda item: item.split('='), additional_heroku_properties))
server_vars_map.update(additional_properties_map)

server_vars_map['SIMPLE_SERVER_HOST'] = '{app_name}.herokuapp.com'.format(app_name = heroku_app_name)

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

print('Set env variables on [{app_name}]\n{env_vars}'.format(
    app_name = heroku_app_name,
    env_vars = response.text
))
