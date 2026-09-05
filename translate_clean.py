import json
import re
import time
import concurrent.futures
from deep_translator import GoogleTranslator

input_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'
output_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'

with open(input_file, 'r', encoding='utf-8') as f:
    data = json.load(f)

# The pattern captures all the stardew formatting without spaces.
pattern = re.compile(r'(\$[a-zA-Z0-9]+|#|@|\^|\{[^}]+\}|%[a-zA-Z0-9_]+|\[[^\]]+\]|~|<[^>]+>)')

# The translation API sometimes translates English placeholder keys like MASK or ZZZ into Thai words like "มาส์ก".
# To avoid this, we can use numbers wrapped in some non-translatable characters, like _0_, _1_, etc.
# Or better, we can avoid standard spaces in placeholders or use a unicode char that is unlikely to be touched.
# Let's just use __0__, __1__, etc.
def mask_text(text):
    matches = pattern.findall(text)
    masked_text = text
    placeholder_map = {}

    def replacer(match):
        m = match.group(0)
        idx = len(placeholder_map)
        key = f" __{idx}__ "
        placeholder_map[f"__{idx}__"] = m
        return key

    masked_text = pattern.sub(replacer, text)
    masked_text = re.sub(r'\s+', ' ', masked_text).strip()
    return masked_text, placeholder_map

def unmask_text(text, placeholder_map):
    # Translator often puts space around or modifies spacing. We will just find all occurrences of __#__.
    for key, val in sorted(placeholder_map.items(), key=lambda x: len(x[0]), reverse=True):
        # We find combinations with or without spaces
        text = text.replace(f" {key} ", val)
        text = text.replace(f"{key} ", val)
        text = text.replace(f" {key}", val)
        text = text.replace(key, val)

        # sometimes translated text might have changed the underscores due to weird transliteration?
        # typically Google translate won't touch __0__
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
