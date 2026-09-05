import json
import re

input_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'
output_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'

with open(input_file, 'r', encoding='utf-8') as f:
    data = json.load(f)

# The earlier script might have left literal ZZZ5ZZ or มาส์ก4K in the output
# Let's check for any of those and also missing translations
mask_pattern1 = re.compile(r'ZZZ\d+ZZZ?')
mask_pattern2 = re.compile(r'มาส์ก\d+[a-zA-Z]')
mask_pattern3 = re.compile(r'MASK\d+K')
mask_pattern4 = re.compile(r'MASK\d+K', re.IGNORECASE)

problem_keys = []
for k, v in data.items():
    if mask_pattern1.search(v) or mask_pattern2.search(v) or mask_pattern3.search(v) or mask_pattern4.search(v):
        problem_keys.append(k)

print(f"Keys with broken masks: {len(problem_keys)}")
