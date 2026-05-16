#!/usr/bin/env python3
"""
验证脚本：检查每个段落的original_text是否以原文标记开头
"""
import json

with open("app/src/main/assets/jinkui.json", 'r', encoding='utf-8') as f:
    data = json.load(f)

book = data['books'][0]

# 原文标记（金匮原文的常见开头）
original_prefixes = [
    "问曰", "师曰", "夫", "病者", "诸", "太阳", "阳明", "少阳", "厥阴", "妇人",
    "百合病", "狐惑", "阳毒", "阴毒", "疟", "中风", "血痹", "虚劳",
    "肺痿", "肺痈", "奔豚", "胸痹", "腹满", "五脏", "五脏风寒",
    "痰饮", "消渴", "水气", "黄瘅", "惊悸", "吐血", "呕吐",
    "疮痈", "趺蹶", "寸口", "跌阳", "少阴",
    "#####", "####",
]

# 注释性语气（注文常见开头）
annotation_starts = [
    "黄瘅之病", "黄瘅从湿", "此即", "由此", "凡", "大凡",
    "要之", "总之", "然则", "由是", "予读", "予谓", "予尝",
    "吾谓", "故曰", "故知", "然其", "但此", "惟此", "盖以",
    "方用", "所以", "乃知", "考其", "按",
]

issues = []
total_paras = 0

for ch_idx, ch in enumerate(book['chapters']):
    for p_idx, p in enumerate(ch['paragraphs']):
        total_paras += 1
        orig = p['original_text'].strip()
        ann = p['annotation'].strip()
        
        # Skip formula headers
        if orig.startswith('#'):
            continue
            
        # Skip if already a known original text
        is_original = any(orig.startswith(prefix) for prefix in original_prefixes)
        
        # Check if it starts with annotation-like text
        for kw in annotation_starts:
            if orig.startswith(kw) and not is_original:
                issues.append((ch_idx, ch['title'], p_idx, kw, orig[:80]))
                break

print(f"Total paragraphs checked: {total_paras}")
print(f"Issues found: {len(issues)}")
for ch_idx, title, p_idx, kw, preview in issues:
    print(f"  Ch{ch_idx} \"{title[:20]}\" para {p_idx}: starts with '{kw}'")
    print(f"    -> \"{preview}\"\n")

if len(issues) == 0:
    print("✅ All clean! No annotation-original swaps detected.")
else:
    print(f"❌ {len(issues)} issues remain. Need manual review.")

# Also verify specific issues are fixed
print("\n--- Specific verifications ---")

# Issue 1: Check 诸病黄家 exists
ch15 = book['chapters'][14]
found_zhubing = False
for p in ch15['paragraphs']:
    if p['original_text'].startswith("诸病黄家"):
        found_zhubing = True
        ann_len = len(p['annotation'])
        print(f"✅ Issue 1: 诸病黄家 found, annotation length = {ann_len}")
        break
if not found_zhubing:
    print("❌ Issue 1: 诸病黄家 NOT found!")

# Issue 2: Check 黄瘅从湿得之 is in annotation
found_huangdan_congshi = False
for p in ch15['paragraphs']:
    if "黄瘅从湿得之" in p['annotation']:
        found_huangdan_congshi = True
        print(f"✅ Issue 2: 黄瘅从湿得之 moved to annotation")
        break
if not found_huangdan_congshi:
    print("❌ Issue 2: 黄瘅从湿得之 not found in any annotation!")

# Issue 3: Check 诸黄腹痛而呕者 and 男子黄
found_zhuhuang_futong = False
found_nanzi_huang = False
for p in ch15['paragraphs']:
    if p['original_text'].startswith("诸黄，腹痛"):
        found_zhuhuang_futong = True
        print(f"✅ Issue 3a: 诸黄腹痛而呕者 found")
    if p['original_text'].startswith("男子黄"):
        found_nanzi_huang = True
        print(f"✅ Issue 3b: 男子黄 found")

# Issue 4: Check chapter 22
ch22 = book['chapters'][21]
# Check para 8 has the 少阴脉 annotation
if "少阴脉，手太阴动脉之尺部也" in ch22['paragraphs'][8]['annotation']:
    print(f"✅ Issue 4a: 少阴脉 annotation merged into狼牙汤 paragraph")
# Check para 9 has 胃气下泄
if ch22['paragraphs'][9]['original_text'].startswith("胃气下泄"):
    print(f"✅ Issue 4b: 膏发煎 original text set correctly")

# Issue 5: Check chapter 3
ch3 = book['chapters'][2]
if "瓜蒌牡蛎散" in ch3['paragraphs'][12]['original_text']:
    print(f"✅ Issue 5: 瓜蒌牡蛎散 fixed")
