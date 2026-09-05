import os
import argparse
import requests
from PIL import ImageFont, ImageDraw, Image

# Requirements:
# Generate 3 font style variants:
# 1. FarmSync Pixel Retro (Chunky 8-bit / Pixel Art style for classic Stardew UI)
# 2. FarmSync Clean Slate (Modern Sans-Serif clean pixel for high readability on mobile)
# 3. FarmSync Cozy Script (Warm handwritten feel for dialogue boxes)

FONTS = {
    # Pixel art style: Itim gives a nice chunky look for Retro
    "FarmSyncPixelRetro": "https://github.com/google/fonts/raw/main/ofl/itim/Itim-Regular.ttf",
    "FarmSyncCleanSlate": "https://github.com/googlefonts/noto-fonts/raw/main/hinted/ttf/NotoSansThai/NotoSansThai-Regular.ttf",
    "FarmSyncCozyScript": "https://github.com/google/fonts/raw/main/ofl/mali/Mali-Regular.ttf"
}

def download_fonts():
    os.makedirs("fonts", exist_ok=True)
    for name, url in FONTS.items():
        path = f"fonts/{name}.ttf"
        if not os.path.exists(path):
            print(f"Downloading {name}...")
            r = requests.get(url)
            with open(path, "wb") as f:
                f.write(r.content)

def generate_bmfont(font_name, font_path, size=32, tex_size=256, out_dir="out"):
    print(f"Generating BMFont for {font_name}...")
    font = ImageFont.truetype(font_path, size)

    # Requirement: "Pack all Thai characters (U+0E01..U+0E5B) + tone marks (U+0E31, U+0E34..U+0E3A, U+0E47..U+0E4E)"
    # U+0E01..U+0E5B covers all these.
    # ASCII chars can be useful too, but let's just do space + Thai range to be sure.
    char_list = [0x20] + list(range(0x0E01, 0x0E5C))

    ascent, descent = font.getmetrics()
    line_height = ascent + descent
    base = ascent

    class ShelfPacker:
        def __init__(self, width, height):
            self.width = width
            self.height = height
            self.current_y = 0
            self.current_x = 0
            self.row_height = 0

        def pack(self, w, h):
            if w > self.width:
                raise ValueError("Item wider than packer")
            if self.current_x + w > self.width:
                self.current_x = 0
                self.current_y += self.row_height
                self.row_height = 0

            if self.current_y + h > self.height:
                raise ValueError("Packer full")

            x = self.current_x
            y = self.current_y

            self.current_x += w
            self.row_height = max(self.row_height, h)

            return x, y

    padding = 2
    packer = ShelfPacker(tex_size, tex_size)

    out_image = Image.new("RGBA", (tex_size, tex_size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(out_image)

    char_data = []

    for c_code in char_list:
        c = chr(c_code)

        try:
            mask = font.getmask(c)
            bbox = mask.getbbox()
            if bbox is None:
                width = int(font.getlength(c))
                height = 1
                left, top, right, bottom = 0, 0, width, 1
            else:
                left, top, right, bottom = font.getbbox(c, anchor="ls")
                width = right - left
                height = bottom - top
                if width <= 0 or height <= 0:
                    width = int(font.getlength(c))
                    height = 1
                    left, top, right, bottom = 0, 0, width, 1
        except Exception:
            bbox = font.getbbox(c, anchor="ls")
            if not bbox:
                width = int(font.getlength(c))
                height = 1
                left, top, right, bottom = 0, 0, width, 1
            else:
                left, top, right, bottom = bbox
                width = right - left
                height = bottom - top

        pack_w = width + padding * 2
        pack_h = height + padding * 2

        try:
            px, py = packer.pack(pack_w, pack_h)
        except ValueError:
            print(f"Warning: Texture size {tex_size} is too small for {font_name}. Increase tex_size.")
            break

        draw_x = px + padding - left
        draw_y = py + padding - top

        if c_code != 0x20 and bbox is not None:
            draw.text((draw_x, draw_y), c, font=font, fill=(255, 255, 255, 255), anchor="ls")

        xadvance = int(font.getlength(c))
        yoffset = base + top
        xoffset = left

        char_data.append({
            "id": c_code,
            "x": px + padding,
            "y": py + padding,
            "width": width,
            "height": height,
            "xoffset": xoffset,
            "yoffset": yoffset,
            "xadvance": xadvance
        })

    os.makedirs(out_dir, exist_ok=True)

    # Save Image
    png_file = f"{font_name}.png"
    out_image.save(os.path.join(out_dir, png_file))

    # Save FNT
    fnt_file = f"{font_name}.fnt"
    with open(os.path.join(out_dir, fnt_file), "w", encoding="utf-8") as f:
        f.write(f'info face="{font_name}" size={size} bold=0 italic=0 charset="" unicode=1 stretchH=100 smooth=1 aa=1 padding={padding},{padding},{padding},{padding} spacing=1,1 outline=0\n')
        f.write(f'common lineHeight={line_height} base={base} scaleW={tex_size} scaleH={tex_size} pages=1 packed=0 alphaChnl=0 redChnl=4 greenChnl=4 blueChnl=4\n')
        f.write(f'page id=0 file="{png_file}"\n')
        f.write(f'chars count={len(char_data)}\n')
        for cd in char_data:
            f.write(f'char id={cd["id"]} x={cd["x"]} y={cd["y"]} width={cd["width"]} height={cd["height"]} xoffset={cd["xoffset"]} yoffset={cd["yoffset"]} xadvance={cd["xadvance"]} page=0 chnl=15\n')

    print(f"Generated {fnt_file} and {png_file} in {out_dir}")

def main():
    parser = argparse.ArgumentParser(description="FarmSync Open-Source Thai Pixel/Bitmap Font Generator & Compiler Tool")
    parser.add_argument("--size", type=int, default=32, help="Font size (default: 32)")
    parser.add_argument("--tex_size", type=int, default=512, help="Texture size (default: 512)")
    parser.add_argument("--out_dir", type=str, default="out", help="Output directory")
    args = parser.parse_args()

    download_fonts()
    for name in FONTS.keys():
        generate_bmfont(name, f"fonts/{name}.ttf", size=args.size, tex_size=args.tex_size, out_dir=args.out_dir)

if __name__ == "__main__":
    main()
