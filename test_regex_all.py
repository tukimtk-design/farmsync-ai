import json
import re

with open('ridgeside_village_work_packages/wp_rsv_01_partC.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

pattern = re.compile(r'(\$[a-zA-Z0-9_]+|#|@|\^|\{[a-zA-Z0-9_]+\}|%[a-zA-Z0-9_]+|\[[^\]]+\])')

placeholders = set()
for k, v in data.items():
    matches = pattern.findall(v)
    for m in matches:
        placeholders.add(m)

print(placeholders)
