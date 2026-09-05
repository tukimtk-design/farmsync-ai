import json
import time
from translate_clean import translate_value

input_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'
output_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'

with open(input_file, 'r', encoding='utf-8') as f:
    data = json.load(f)

failed_keys = [
    "Pika.Dialogue.Mon2", "Pika.Dialogue.Sat", "Pika.Dialogue.Thu",
    "Pika.Dialogue.Wed", "Pika.Dialogue.Fri", "Pika.Dialogue.Tue2",
    "Pika.Dialogue.Sun", "Pika.Dialogue.Thu2", "Pika.Dialogue.Wed2",
    "Pika.Dialogue.Fri2"
]

# Wait a bit before retrying
time.sleep(5)

for k in failed_keys:
    if k in data:
        print(f"Retrying {k}")
        _, new_val = translate_value(k, data[k])
        data[k] = new_val

with open(output_file, 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print("Retries done.")
