import json
import re
import time
import concurrent.futures
from deep_translator import GoogleTranslator

input_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'
output_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'

with open(input_file, 'r', encoding='utf-8') as f:
    data = json.load(f)

pattern = re.compile(r'(\$[a-zA-Z0-9_]+|#|@|\^|\{[a-zA-Z0-9_]+\}|%[a-zA-Z0-9_]+|\[[^\]]+\])')

def mask_text(text):
    matches = pattern.findall(text)
    masked_text = text
    placeholder_map = {}
    for i, match in enumerate(matches):
        placeholder_key = f"ZZZ{i}ZZZ"
        masked_text = masked_text.replace(match, f" {placeholder_key} ", 1)
        placeholder_map[placeholder_key] = match
    # Replace multiple spaces with a single space to avoid translation issues
    masked_text = re.sub(r'\s+', ' ', masked_text).strip()
    return masked_text, placeholder_map

def unmask_text(text, placeholder_map):
    for key, val in sorted(placeholder_map.items(), key=lambda x: len(x[0]), reverse=True):
        text = text.replace(f" {key} ", val)
        text = text.replace(f"{key} ", val)
        text = text.replace(f" {key}", val)
        text = text.replace(key, val)
    # Basic cleanup
    text = text.replace('  ', ' ')
    return text

def translate_with_retry(text, retries=3):
    for i in range(retries):
        try:
            translator = GoogleTranslator(source='en', target='th')
            res = translator.translate(text)
            if res:
                return res
        except Exception as e:
            if i == retries - 1:
                print(f"Failed to translate: {text} - Error: {e}")
            time.sleep(1 + i)
    return text # fallback to original

def translate_value(key, value):
    try:
        masked, placeholder_map = mask_text(value)
        translated_masked = translate_with_retry(masked)
        unmasked = unmask_text(translated_masked, placeholder_map)
        return key, unmasked
    except Exception as e:
        print(f"Error processing {key}: {e}")
        return key, value

translated_data = {}
items = list(data.items())

print(f"Total keys to translate: {len(items)}")

with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
    futures = {executor.submit(translate_value, k, v): (k, v) for k, v in items}

    count = 0
    for future in concurrent.futures.as_completed(futures):
        k, v = future.result()
        translated_data[k] = v
        count += 1
        if count % 100 == 0:
            print(f"Translated {count}/{len(items)}")

final_data = {}
for k in data.keys():
    final_data[k] = translated_data.get(k, data[k])

with open(output_file, 'w', encoding='utf-8') as f:
    json.dump(final_data, f, ensure_ascii=False, indent=2)

print(json.dumps({
    "total_keys": len(items),
    "translated_keys": len(final_data),
    "status": "success"
}))
