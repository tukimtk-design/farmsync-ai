import json
import re
from deep_translator import GoogleTranslator
import time

input_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'
output_file = 'ridgeside_village_work_packages/wp_rsv_01_partC.json'

with open(input_file, 'r', encoding='utf-8') as f:
    data = json.load(f)

# Need to reload original data to get English strings for failed translations
# Wait, actually the translation script overwrites the values even on failure,
# let's check if the values in output are in English.
