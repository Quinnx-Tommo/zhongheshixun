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
login = post_json('/api/wx/login', {'code': 'debug_grade2'})
WX_TOKEN = login['data']['token']

# Start exam
start = post_json('/api/exam/start/1', {}, WX_TOKEN)
questions = start['data']['questions']

# DB correct answers
db_answers = {
    2: ('C', '糖尿病空腹血糖诊断标准'),
    6: ('D', '高血压非药物治疗'),
    8: ('3', '高血压诊断测量次数'),
    9: ('D', '高血压危险因素'),
    10: ('B', '正常高值血压范围'),
    11: ('T', '限制饮酒'),
    14: ('A', '理想血压范围'),
    16: ('D', '糖尿病典型症状'),
    17: ('F', '都需要胰岛素'),
    19: ('D', '糖尿病慢性并发症'),
}

# Build correct answers
answers = []
for q in questions:
    qid = q['id']
    qtype = q['questionType']
    correct_answer, desc = db_answers.get(qid, ('A', 'unknown'))
    answers.append({'questionId': qid, 'answer': correct_answer})
    print(f"  Q{qid} type={qtype} -> sending '{correct_answer}' (expected: {desc})")

# Submit
submit = post_json('/api/exam/submit', {'examId': 1, 'answers': answers}, WX_TOKEN)
print(f"\nResult: {json.dumps(submit, ensure_ascii=False)}")
