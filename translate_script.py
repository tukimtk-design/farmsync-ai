import json
import re
import concurrent.futures
from deep_translator import GoogleTranslator

input_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'
output_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'

with open(input_file, 'r', encoding='utf-8') as f:
    data = json.load(f)

# Stardew valley translation placeholders:
# $letter, #, @, ^, {something}, %something, [something]
# The pattern we saw:
pattern = re.compile(r'(\$[a-zA-Z0-9_]+|#|@|\^|\{[a-zA-Z0-9_]+\}|%[a-zA-Z0-9_]+|\[[^\]]+\])')

def mask_text(text):
    matches = pattern.findall(text)
    masked_text = text
    placeholder_map = {}
    for i, match in enumerate(matches):
        placeholder_key = f"ZZZ{i}ZZZ"
        # We need to replace only the first occurrence to avoid messing up overlapping masks,
        # but since our keys are unique per match we can just replace 1.
        masked_text = masked_text.replace(match, f" {placeholder_key} ", 1)
        placeholder_map[placeholder_key] = match
    return masked_text, placeholder_map

def unmask_text(text, placeholder_map):
    # Sort keys by length descending to avoid partial replacements (though ZZZ#ZZZ is fairly safe)
    for key, val in placeholder_map.items():
        text = text.replace(f" {key} ", val)
        # Fallback if translator stripped spaces
        text = text.replace(f"{key} ", val)
        text = text.replace(f" {key}", val)
        text = text.replace(key, val)
    return text

def translate_value(key, value):
    try:
        masked, placeholder_map = mask_text(value)
        # Deep translator handles up to 5000 chars, which is fine for these.
        translator = GoogleTranslator(source='en', target='th')
        translated_masked = translator.translate(masked)

        # In case the translation failed and returned None
        if not translated_masked:
            translated_masked = masked

        unmasked = unmask_text(translated_masked, placeholder_map)

        # Some basic cleanup of typical thai punctuation issues introduced by translators
        unmasked = unmasked.replace('  ', ' ')
        return key, unmasked
    except Exception as e:
        print(f"Error on {key}: {e}")
        return key, value

# Parallel processing
translated_data = {}
items = list(data.items())

print(f"Total keys to translate: {len(items)}")

with concurrent.futures.ThreadPoolExecutor(max_workers=20) as executor:
    futures = [executor.submit(translate_value, k, v) for k, v in items]

    count = 0
    for future in concurrent.futures.as_completed(futures):
        k, v = future.result()
        translated_data[k] = v
        count += 1
        if count % 100 == 0:
            print(f"Translated {count}/{len(items)}")

# Ensure original order is preserved
final_data = {}
for k in data.keys():
    final_data[k] = translated_data.get(k, data[k])

with open(output_file, 'w', encoding='utf-8') as f:
    json.dump(final_data, f, ensure_ascii=False, indent=2)

# Metrics
print(json.dumps({
    "total_keys": len(items),
    "translated_keys": len(final_data),
    "status": "success"
}))
