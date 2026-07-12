#!/usr/bin/env python3
import json
import urllib.request
import urllib.error

BASE_API = "http://localhost:8081"

def post_json(path, data, token=None):
    url = f"{BASE_API}{path}"
    body = json.dumps(data).encode('utf-8')
    req = urllib.request.Request(url, data=body, headers={'Content-Type': 'application/json'})
    if token:
        req.add_header('Authorization', f'Bearer {token}')
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            return json.loads(resp.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        return json.loads(e.read().decode('utf-8'))

# Login
login = post_json('/api/wx/login', {'code': 'debug_grade'})
WX_TOKEN = login['data']['token']

# Start exam
start = post_json('/api/exam/start/1', {}, WX_TOKEN)
questions = start['data']['questions']

# Print all question details
print("Questions received:")
for q in questions:
    print(f"  ID={q['id']}, type={q['questionType']}, options={q.get('options')}")
