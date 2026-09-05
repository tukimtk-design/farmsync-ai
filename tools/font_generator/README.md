# FarmSync Open-Source Thai Pixel/Bitmap Font Generator & Compiler Tool

This is a Python-based Open-Source Thai Bitmap Font Compiler (BMFont generator) for Stardew Valley 1.6 Content Patcher mods.

## Features
- Uses pure open-source TTF base fonts (OFL).
- Generates 3 font style variants:
  - **FarmSync Pixel Retro**: Chunky 8-bit / Pixel Art style for classic Stardew UI (Uses Itim OFL)
  - **FarmSync Clean Slate**: Modern Sans-Serif clean pixel for high readability on mobile (Uses Noto Sans Thai OFL)
  - **FarmSync Cozy Script**: Warm handwritten feel for dialogue boxes (Uses Mali OFL)
- Packs all Thai characters (U+0E01..U+0E5B) + tone marks (U+0E31, U+0E34..U+0E3A, U+0E47..U+0E4E) with correct vertical baseline offset calculation (prevent floating accents).
- Outputs valid BMFont files (`.fnt` + `.png`) directly ready to be bundled into StardewValley-ThaiTranslation-Mod.

## Requirements
- Python 3.8+
- Pillow (PIL)
- Requests

## Usage
1. Install dependencies:
```bash
pip install -r requirements.txt
```

2. Run the compiler:
```bash
python compiler.py
```

Optional arguments:
- `--size`: Font size (default: 32)
- `--tex_size`: Texture size (default: 512)
- `--out_dir`: Output directory (default: out)
