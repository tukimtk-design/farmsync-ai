import json
import re
import time
import concurrent.futures
from deep_translator import GoogleTranslator

input_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'
output_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'

with open(input_file, 'r', encoding='utf-8') as f:
    data = json.load(f)

# Better pattern to capture placeholders including brackets exactly
# Stardew variables:
# $letter or $number
# #
# @
# ^
# {something}
# %something
# [something]
# ~ (tilde is also sometimes formatting)
pattern = re.compile(r'(\$[a-zA-Z0-9]+|#|@|\^|\{[^}]+\}|%[a-zA-Z0-9_]+|\[[^\]]+\]|~|<[^>]+>)')

def mask_text(text):
    # Find all matches
    matches = pattern.findall(text)

    # We will replace them sequentially but with unique keys
    masked_text = text
    placeholder_map = {}

    # To prevent overlapping replacements when one text has multiple identical placeholders
    # we use a while loop or re.sub with a function.

    def replacer(match):
        m = match.group(0)
        idx = len(placeholder_map)
        key = f" MASK{idx}K " # spaces around to help translator
        placeholder_map[key.strip()] = m
        return key

    masked_text = pattern.sub(replacer, text)

    # Clean up spaces
    masked_text = re.sub(r'\s+', ' ', masked_text).strip()
    return masked_text, placeholder_map

def unmask_text(text, placeholder_map):
    # Restore placeholders
    # We sort by length descending to be safe, though MASK{i}K should be distinct
    for key, val in sorted(placeholder_map.items(), key=lambda x: len(x[0]), reverse=True):
        # We try to find the key with or without surrounding spaces
        text = text.replace(f" {key} ", val)
        text = text.replace(f"{key} ", val)
        text = text.replace(f" {key}", val)
        text = text.replace(key, val)
    # The translator might also uppercase the mask in some languages, check lowercase/uppercase
    for key, val in sorted(placeholder_map.items(), key=lambda x: len(x[0]), reverse=True):
        upper_key = key.upper()
        text = text.replace(f" {upper_key} ", val)
        text = text.replace(f"{upper_key} ", val)
        text = text.replace(f" {upper_key}", val)
        text = text.replace(upper_key, val)

    # Some basic cleanup
    text = text.replace('  ', ' ')
    return text

def translate_with_retry(text, retries=5):
    for i in range(retries):
        try:
            translator = GoogleTranslator(source='en', target='th')
            res = translator.translate(text)
            if res:
                return res
        except Exception as e:
            time.sleep(1 + i * 2)
    return None # Fallback to original if completely failed

def translate_value(key, value):
    try:
        masked, placeholder_map = mask_text(value)
        # If there's no actual text to translate, skip it
        if not re.search(r'[a-zA-Z]', masked):
            return key, unmask_text(masked, placeholder_map)

        translated_masked = translate_with_retry(masked)
        if not translated_masked:
            print(f"Failed completely on {key}, keeping original")
            return key, value # keep original on failure

        unmasked = unmask_text(translated_masked, placeholder_map)
        return key, unmasked
    except Exception as e:
        print(f"Error processing {key}: {e}")
        return key, value

translated_data = {}
items = list(data.items())

print(f"Total keys to translate: {len(items)}")

with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
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
