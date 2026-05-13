#!/usr/bin/env python3
"""
Generate APK icons at all Android densities from the v2 icon design.
Outputs to mipmap-* directories.
"""

import math, os, random
from PIL import Image, ImageDraw, ImageFont, ImageFilter

BASE_DIR = "/Users/lanloki/StudioProjects/hdnj/app/src/main/res"
SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

BG_COLOR = "#C83C31"       # 朱砂红
GOLD = "#ECD396"           # 暖金（主色）
GOLD2 = "#E8C878"          # 中金（线条）
GOLD3 = "#D4A846"          # 深金（阴影/细节）

random.seed(42)

def find_font():
    candidates = [
        "/System/Library/AssetsV2/com_apple_MobileAsset_Font8/6331c5916c361af1b83fb8b8b76ef2eece20c8eb.asset/AssetData/Kai.ttf",
        "/System/Library/AssetsV2/com_apple_MobileAsset_Font8/13b8ce423f920875b28b551f9406bf1014e0a656.asset/AssetData/Xingkai.ttc",
        "/System/Library/AssetsV2/com_apple_MobileAsset_Font8/88d6cc32a907955efa1d014207889413890573be.asset/AssetData/Kaiti.ttc",
        "/System/Library/Fonts/Supplemental/Songti.ttc",
        "/System/Library/Fonts/STHeiti Medium.ttc",
    ]
    for fp in candidates:
        if os.path.exists(fp):
            return fp
    return None

FONT_PATH = find_font()

def draw_window_frame(draw, gold_clr, gold2_clr, S):
    M = int(10 * S / 512)
    fw = int(16 * S / 512)
    cx = cy = S // 2

    outer = M
    inner = M + fw
    draw.rounded_rectangle(
        [(outer, outer), (S - outer, S - outer)],
        radius=max(2, int(14 * S / 512)), outline=gold_clr, width=max(1, int(4 * S / 512))
    )
    draw.rounded_rectangle(
        [(inner, inner), (S - inner, S - inner)],
        radius=max(2, int(10 * S / 512)), outline=gold2_clr, width=max(1, int(2 * S / 512))
    )

    corner_side = int(42 * S / 512)
    L = corner_side
    lw = max(1, int(3 * S / 512))
    for sx, sy in [(-1, -1), (-1, 1), (1, -1), (1, 1)]:
        bx = cx + sx * (S // 2 - inner - corner_side // 2 - max(1, int(4 * S / 512)))
        by = cy + sy * (S // 2 - inner - corner_side // 2 - max(1, int(4 * S / 512)))
        hw = L // 2

        if sx < 0 and sy < 0:
            pts = [(bx - hw, by - hw), (bx + hw, by - hw), (bx + hw, by + hw)]
            ipts = [(bx - hw + 8, by - hw + 8), (bx + hw - 8, by - hw + 8), (bx + hw - 8, by + hw - 8)]
        elif sx > 0 and sy < 0:
            pts = [(bx + hw, by - hw), (bx - hw, by - hw), (bx - hw, by + hw)]
            ipts = [(bx + hw - 8, by - hw + 8), (bx - hw + 8, by - hw + 8), (bx - hw + 8, by + hw - 8)]
        elif sx < 0 and sy > 0:
            pts = [(bx - hw, by + hw), (bx + hw, by + hw), (bx + hw, by - hw)]
            ipts = [(bx - hw + 8, by + hw - 8), (bx + hw - 8, by + hw - 8), (bx + hw - 8, by - hw + 8)]
        else:
            pts = [(bx + hw, by + hw), (bx - hw, by + hw), (bx - hw, by - hw)]
            ipts = [(bx + hw - 8, by + hw - 8), (bx - hw + 8, by + hw - 8), (bx - hw + 8, by - hw + 8)]

        draw.line(pts + [pts[0]], fill=gold_clr, width=lw, joint="curve")
        draw.line(ipts + [ipts[0]], fill=gold2_clr, width=max(1, int(2 * S / 512)), joint="curve")

    diamond_size = int(16 * S / 512)
    spacing = int(52 * S / 512)
    rim_radius = S // 2 - inner - diamond_size // 2 - max(1, int(5 * S / 512))

    for angle_deg in range(30, 360, 30):
        if angle_deg % 90 == 0:
            continue
        rad = math.radians(angle_deg)
        px = cx + rim_radius * math.cos(rad)
        py = cy + rim_radius * math.sin(rad)

        dpts = [
            (px, py - diamond_size // 2),
            (px + diamond_size // 2, py),
            (px, py + diamond_size // 2),
            (px - diamond_size // 2, py),
        ]
        draw.line(dpts + [dpts[0]], fill=gold_clr, width=max(1, int(2 * S / 512)), joint="curve")

def render_dazhuan(S):
    if not FONT_PATH:
        return None

    mask = Image.new("L", (S, S), 0)
    mdraw = ImageDraw.Draw(mask)
    font_size = max(32, int(220 * S / 512))

    try:
        font = ImageFont.truetype(FONT_PATH, font_size)
    except:
        return None

    bbox = mdraw.textbbox((0, 0), "黄", font=font)
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    mx = (S - tw) // 2 - bbox[0]
    my = (S - th) // 2 - bbox[1] + max(1, int(15 * S / 512))

    mdraw.text((mx, my), "黄", font=font, fill=255)

    # Thicken
    mask = mask.filter(ImageFilter.MaxFilter(max(3, int(7 * S / 512))))
    mask = mask.filter(ImageFilter.MinFilter(3))
    mask = mask.filter(ImageFilter.MaxFilter(max(3, int(5 * S / 512))))

    # Edge noise
    pixels = mask.load()
    w, h = mask.size
    for x in range(w):
        for y in range(h):
            v = pixels[x, y]
            if 20 < v < 200:
                noise = random.randint(-25, 25)
                pixels[x, y] = max(0, min(255, v + noise))
            elif v > 220:
                if random.random() < 0.02:
                    pixels[x, y] = max(0, v - random.randint(10, 50))

    mask = mask.filter(ImageFilter.SMOOTH_MORE)
    return mask

def generate_icon(size):
    S = size
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    RADIUS = max(4, int(48 * S / 512))
    draw.rounded_rectangle([(0, 0), (S, S)], radius=RADIUS, fill=BG_COLOR)

    draw_window_frame(draw, GOLD, GOLD2, S)

    inner_margin = max(8, int(72 * S / 512))
    draw.rounded_rectangle(
        [(inner_margin, inner_margin),
         (S - inner_margin, S - inner_margin)],
        radius=max(2, int(8 * S / 512)), outline=GOLD3, width=max(1, int(2 * S / 512))
    )

    huang_mask = render_dazhuan(S)
    if huang_mask is None:
        return None

    gold_layer = Image.new("RGBA", (S, S), GOLD)
    char_img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    char_img.paste(gold_layer, (0, 0), huang_mask)

    outline_mask = huang_mask.filter(ImageFilter.MinFilter(3))
    outline_mask = outline_mask.filter(ImageFilter.MaxFilter(max(5, int(9 * S / 512))))
    outline = Image.new("L", (S, S), 0)
    opx = outline.load()
    mpx = outline_mask.load()
    hpx = huang_mask.load()
    for x in range(S):
        for y in range(S):
            if mpx[x, y] > 128 and hpx[x, y] < 128:
                opx[x, y] = 180

    shadow_layer = Image.new("RGBA", (S, S), GOLD3)
    char_img.paste(shadow_layer, (0, 0), outline)
    img = Image.alpha_composite(img, char_img)

    # Vignette
    overlay = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    odraw = ImageDraw.Draw(overlay)
    for i in range(max(5, int(30 * S / 512))):
        alpha = int(5 * (1 - i / max(5, int(30 * S / 512))))
        odraw.rounded_rectangle(
            [(i, i), (S - i, S - i)],
            radius=max(2, RADIUS - i),
            outline=(0, 0, 0, alpha), width=1
        )
    img = Image.alpha_composite(img, overlay)
    return img

if __name__ == "__main__":
    print("=" * 50)
    print("  🦞🎨 黄帝内经APP图标 v2 生成")
    print("=" * 50)

    for density, size in SIZES.items():
        out_dir = os.path.join(BASE_DIR, density)
        img = generate_icon(size)
        if img is None:
            print(f"  ❌ {density}: 生成失败")
            continue

        # Save both ic_launcher.png and ic_launcher_round.png
        for name in ["ic_launcher.png", "ic_launcher_round.png"]:
            out_path = os.path.join(out_dir, name)
            img.save(out_path, "PNG")
            print(f"  ✅ {out_path} ({size}×{size})")

    print("\n  🎉 全部图标生成完成！")
