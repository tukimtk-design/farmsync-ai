import json
import re
import time
import concurrent.futures
from deep_translator import GoogleTranslator

input_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'
output_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'

with open(input_file, 'r', encoding='utf-8') as f:
    data = json.load(f)

pattern = re.compile(r'(\$[a-zA-Z0-9]+|#|@|\^|\{[^}]+\}|%[a-zA-Z0-9_]+|\[[^\]]+\]|~|<[^>]+>)')

def mask_text(text):
    matches = pattern.findall(text)
    masked_text = text
    placeholder_map = {}

    def replacer(match):
        m = match.group(0)
        idx = len(placeholder_map)
        key = f" MASK{idx}K "
        placeholder_map[key.strip()] = m
        return key

    masked_text = pattern.sub(replacer, text)
    masked_text = re.sub(r'\s+', ' ', masked_text).strip()
    return masked_text, placeholder_map

def unmask_text(text, placeholder_map):
    for key, val in sorted(placeholder_map.items(), key=lambda x: len(x[0]), reverse=True):
        text = text.replace(f" {key} ", val)
        text = text.replace(f"{key} ", val)
        text = text.replace(f" {key}", val)
        text = text.replace(key, val)
    for key, val in sorted(placeholder_map.items(), key=lambda x: len(x[0]), reverse=True):
        upper_key = key.upper()
        text = text.replace(f" {upper_key} ", val)
        text = text.replace(f"{upper_key} ", val)
        text = text.replace(f" {upper_key}", val)
        text = text.replace(upper_key, val)
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
    return None

def translate_value(key, value):
    try:
        masked, placeholder_map = mask_text(value)
        if not re.search(r'[a-zA-Z]', masked):
            return key, unmask_text(masked, placeholder_map)

        translated_masked = translate_with_retry(masked)
        if not translated_masked:
            print(f"Failed completely on {key}, keeping original")
            return key, value

        unmasked = unmask_text(translated_masked, placeholder_map)
        return key, unmasked
    except Exception as e:
        print(f"Error processing {key}: {e}")
        return key, value

# Identify untranslated strings by checking if they contain English alphabetical characters in a large portion, or just if we know they are English.
# Let's just do a quick heuristic: if it doesn't contain Thai characters ([\u0E00-\u0E7F])
# Since it might have punctuation only, we also check if it has Thai characters. If not, and it has english letters, it needs translating.
import re
thai_pattern = re.compile(r'[\u0e00-\u0e7f]')

to_translate = {}
for k, v in data.items():
    # If no thai character and contains letters, it is untranslated (or just a plain string without thai)
    # Even if it contains letters, if it contains no thai characters it might be fully english.
    # Exclude purely variables string like "@" or "$h#$b#"
    masked, _ = mask_text(v)
    if not thai_pattern.search(v) and re.search(r'[a-zA-Z]', masked):
        to_translate[k] = v

print(f"Keys needing translation: {len(to_translate)}")

for k, v in to_translate.items():
    print(f"Translating {k}...")
    _, new_v = translate_value(k, v)
    data[k] = new_v

with open(output_file, 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print("Done missing translations.")
