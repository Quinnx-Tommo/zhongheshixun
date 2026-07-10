#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""批量将 4 份可行性分析报告 md 转为 docx"""
import os
import sys
sys.path.insert(0, r'd:\A-Users\Desktop\zhongheshixun')
from md_to_docx import parse_markdown, write_docx

BASE = r'd:\A-Users\Desktop\zhongheshixun\defense-materials\综合实训可行性分析'
FILES = [
    'A-学生甲-项目可行性分析报告.md',
    'B-学生乙-项目可行性分析报告.md',
    'C-学生丙-项目可行性分析报告.md',
    'D-学生丁-项目可行性分析报告.md',
]

for fn in FILES:
    src = os.path.join(BASE, fn)
    dst = src.replace('.md', '.docx')
    with open(src, 'r', encoding='utf-8') as f:
        md = f.read()
    blocks = parse_markdown(md)
    write_docx(blocks, dst)
    print(f'  -> {os.path.basename(dst)}  ({os.path.getsize(dst)} bytes)')
