import json
import concurrent.futures
from translate_perfect import translate_value

failed_keys = [
    "Freddie.Dialogue.Fri",
    "Freddie.Dialogue.Sun",
    "Freddie.Dialogue.Tue2",
    "Freddie.Dialogue.Wed2",
    "Freddie.Dialogue.Thu2"
]

input_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'

with open(input_file, 'r', encoding='utf-8') as f:
    data = json.load(f)

for k in failed_keys:
    print(f"Original {k}: {data[k]}")
    # Let's see if we can get it translated
    _, new_val = translate_value(k, data[k])
    data[k] = new_val
    print(f"Translated {k}: {new_val}")

with open(input_file, 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)
