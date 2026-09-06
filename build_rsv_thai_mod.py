import json
import re
import os

OUTPUT_DIR = "dist/RidgesideVillage_Thai_v2.5.17"
I18N_DIR = os.path.join(OUTPUT_DIR, "i18n")

def main():
    # 1. Setup output directories
    os.makedirs(I18N_DIR, exist_ok=True)

    # 2. Read the master dictionary
    master_file = "ridgeside_village_work_packages/rsv_default_clean.json"
    with open(master_file, "r", encoding="utf-8") as f:
        master_dict = json.load(f)

    # 3. Iterate over specified work packages and update master with Thai translations
    wp_files = [
        "wp_rsv_01.json",
        "wp_rsv_02.json",
        "wp_rsv_03.json"
    ]

    # User's request: "... maps all Thai translated keys from wp_rsv_01, wp_rsv_02, and wp_rsv_03..."
    # The initial script also loaded part files. Let's make sure we only load the ones with Thai characters,
    # but practically we can just load the requested ones. Actually we can load all parts to be safe since
    # partA, partB, etc. had the translated contents too.

    # Based on the previous python test script:
    # wp_rsv_02.json had 4561 thai, wp_rsv_03_partA.json had 1937 thai, wp_rsv_03_partB.json had 1943 thai.
    # wp_rsv_01, wp_rsv_03 didn't have thai in the parent file, just the parts or nothing. Wait, I should include the parts because "wp_rsv_01, wp_rsv_02, and wp_rsv_03" refers to the work packages, which includes their sub-parts! Let's load all files matching wp_rsv_*.json to be sure.
    wp_files_to_check = [f for f in os.listdir("ridgeside_village_work_packages") if f.startswith("wp_rsv_") and f.endswith(".json")]

    thai_pattern = re.compile(r'[\u0E00-\u0E7F]')
    thai_keys_count = 0

    for wp_filename in sorted(wp_files_to_check):
        wp_path = os.path.join("ridgeside_village_work_packages", wp_filename)

        with open(wp_path, "r", encoding="utf-8") as f:
            wp_data = json.load(f)
            if not isinstance(wp_data, dict):
                continue

            for k, v in wp_data.items():
                if k in master_dict and isinstance(v, str):
                    if thai_pattern.search(v):
                        master_dict[k] = v
                        thai_keys_count += 1

    print(f"Updated {thai_keys_count} keys with Thai translations.")

    # 4. Write default.json
    default_json_path = os.path.join(I18N_DIR, "default.json")
    with open(default_json_path, "w", encoding="utf-8") as f:
        json.dump(master_dict, f, ensure_ascii=False, indent=2)

    # 5. Create manifest.json
    manifest_data = {
        "Name": "Ridgeside Village - Thai Translation",
        "Author": "Automated Builder",
        "Version": "2.5.17",
        "Description": "Thai Translation for Ridgeside Village 2.5.17",
        "UniqueID": "com.tukimtk.rsv.thai",
        "MinimumApiVersion": "4.0.0",
        "UpdateKeys": [],
        "ContentPackFor": {
            "UniqueID": "Pathoschild.ContentPatcher"
        },
        "Dependencies": [
            {
                "UniqueID": "Rafseazz.RSVCP",
                "IsRequired": True
            }
        ]
    }

    manifest_path = os.path.join(OUTPUT_DIR, "manifest.json")
    with open(manifest_path, "w", encoding="utf-8") as f:
        json.dump(manifest_data, f, ensure_ascii=False, indent=2)

    # 6. Validate JSON output
    try:
        with open(default_json_path, "r", encoding="utf-8") as f:
            json.load(f)
        with open(manifest_path, "r", encoding="utf-8") as f:
            json.load(f)
        print("JSON validation passed.")
    except json.JSONDecodeError as e:
        print(f"JSON validation failed: {e}")
        exit(1)

if __name__ == "__main__":
    main()
