#!/usr/bin/env python3
"""Test exam module: login -> start -> submit with auto-grade"""
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

def get_json(path, token=None):
    url = f"{BASE_API}{path}"
    req = urllib.request.Request(url)
    if token:
        req.add_header('Authorization', f'Bearer {token}')
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            return json.loads(resp.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        return json.loads(e.read().decode('utf-8'))

# Step 1: Login
print("=" * 60)
print("STEP 1: Miniapp Login")
login_resp = post_json('/api/wx/login', {'code': 'test_auto_grade_flow'})
if login_resp['code'] != 200:
    print(f"Login failed: {login_resp}")
    exit(1)
token = login_resp['data']['token']
user_id = login_resp['data']['userInfo']['id']
print(f"Login OK - userId: {user_id}")

# Step 2: Get exam list
print("\n" + "=" * 60)
print("STEP 2: Get Exam List")
list_resp = get_json('/api/exam/list', token)
if list_resp['code'] != 200:
    print(f"List failed: {list_resp}")
    exit(1)
exams = list_resp['data']
print(f"Available exams: {len(exams)}")
for e in exams[:3]:
    print(f"  - [{e['id']}] {e['title']} (type={e['examType']}, questions={e['questionCount']})")

# Step 3: Start exam 1
print("\n" + "=" * 60)
print("STEP 3: Start Exam 1")
start_resp = post_json('/api/exam/start/1', {}, token)
if start_resp['code'] != 200:
    print(f"Start failed: {start_resp}")
    exit(1)
exam_data = start_resp['data']
print(f"Exam: {exam_data['title']}")
print(f"Duration: {exam_data['duration']}min, Total: {exam_data['totalScore']}, Pass: {exam_data['passScore']}")
questions = exam_data['questions']
print(f"Questions: {len(questions)}")

# Step 4: Submit with correct answers
print("\n" + "=" * 60)
print("STEP 4: Submit Exam (with correct answers)")
# Correct answers from DB
answer_map = {
    1: 'C', 2: 'C', 5: 'B', 6: 'D', 7: 'T', 8: '3', 9: 'D', 10: 'B',
    11: 'T', 12: '140/90', 13: 'D', 14: 'A', 15: 'C', 16: 'D', 17: 'F',
    18: '6.1-7.0', 19: 'D', 20: 'C', 21: 'T', 22: '控制血糖,定期检查足部', 23: 'D', 24: 'B'
}
answers = []
for q in questions:
    qid = q['id']
    qtype = q['questionType']
    correct = answer_map.get(qid, 'A')
    answers.append({'questionId': qid, 'answer': correct})
    type_name = {1:'单选', 2:'多选', 3:'判断', 4:'填空', 5:'问答'}.get(qtype, '?')
    print(f"  Q{qid} [{type_name}]: {correct}")

submit_resp = post_json('/api/exam/submit', {'examId': 1, 'answers': answers}, token)
print(f"\nSubmit response: {json.dumps(submit_resp, ensure_ascii=False, indent=2)}")

if submit_resp['code'] == 200:
    result = submit_resp['data']
    print("\n" + "=" * 60)
    print("RESULT: Auto-grade SUCCESS!")
    print(f"  Score: {result['score']}/{result['totalScore']}")
    print(f"  Correct: {result['correctCount']}, Wrong: {result['wrongCount']}")
    print(f"  Correct Rate: {result['correctRate']}%")
    print(f"  Passed: {result['passed']}")
else:
    print(f"\nSubmit failed: {submit_resp}")
