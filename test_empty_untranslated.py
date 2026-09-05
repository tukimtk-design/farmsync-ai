import json
import re

input_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'
with open(input_file, 'r', encoding='utf-8') as f:
    data = json.load(f)

thai_pattern = re.compile(r'[\u0e00-\u0e7f]')
pattern = re.compile(r'(\$[a-zA-Z0-9]+|#|@|\^|\{[^}]+\}|%[a-zA-Z0-9_]+|\[[^\]]+\]|~|<[^>]+>)')

untranslated = []
for k, v in data.items():
    if not thai_pattern.search(v):
        masked = pattern.sub('', v)
        if re.search(r'[a-zA-Z]', masked):
            untranslated.append((k, v))

print(f"Untranslated keys: {len(untranslated)}")
for k, v in untranslated[:10]:
    print(f"{k}: {v}")
