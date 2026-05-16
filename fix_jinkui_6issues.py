#!/usr/bin/env python3
"""
二次修复：金匮发微JSON解析6个残留问题
"""
import json
import copy

JSON_PATH = "app/src/main/assets/jinkui.json"

with open(JSON_PATH, 'r', encoding='utf-8') as f:
    data = json.load(f)

book = data['books'][0]

# ============================================================
# Helper: find chapter by title suffix
# ============================================================
def find_chapter(suffix):
    for i, ch in enumerate(book['chapters']):
        if ch.get('title', '').endswith(suffix):
            return i, ch
    return None, None

# ============================================================
# Issue 1: 第15章「黄瘅病」段9 — 注释误作原文
# ============================================================
ch15_idx, ch15 = find_chapter("黄疸病脉证并治第十五")
print(f"Issue 1: Found chapter 15 at index {ch15_idx}, title={ch15['title']}")
paras = ch15['paragraphs']

# Check current para 8 (段9, 0-based)
p8 = paras[8]
print(f"  Before - para 8 orig[:50] = \"{p8['original_text'][:50]}\"")
print(f"  Before - para 8 ann[:50] = \"{p8['annotation'][:50]}\"")

# The original text "诸病黄家，但利其小便，假令脉浮，当以汗解之，宜桂枝加黄芪汤主之。" is missing
# The "黄瘅之病，起于湿，成于水..." text and annotation are actually 曹注 for that original text
# Fix: insert the original text + 方名 before para 8, then set para 8's content to be 诸黄猪膏发煎

# Step 1: Insert "诸病黄家..." before para 8
orig_text_1 = "诸病黄家，但利其小便，假令脉浮，当以汗解之，宜桂枝加黄芪汤主之。"
fangming_text_1 = "##### 桂枝加黄芪汤方"
fangming_ann_1 = "见水气。"

# The merged annotation from current para 8
merged_ann_1 = p8['original_text'] + " " + p8['annotation']

# Insert at index 8: first 方名, then 原文 (in reverse order for insert)
new_para_fangming = {"original_text": fangming_text_1, "annotation": fangming_ann_1}
new_para_orig = {"original_text": orig_text_1, "annotation": merged_ann_1}

# Insert in reverse order to maintain correct position
paras.insert(8, new_para_fangming)
paras.insert(8, new_para_orig)

# Now paras[8] = 原文, paras[9] = 方名
# What was paras[8] is now at paras[10]
# Change paras[10] to 诸黄,猪膏发煎主之 (the missing original text before 猪膏发煎方)
paras[10]['original_text'] = "诸黄，猪膏发煎主之。"
paras[10]['annotation'] = ""  # The annotation moves to the 曹注 paragraph

# Now we need to insert 诸黄 annotation after 猪膏发煎方
# Find 猪膏发煎方 paragraph (currently at para 11, since we inserted 2 before old para 8)
# old para 9 (##### 猪膏发煎方) is now at index 11
p11 = paras[11]
print(f"  After - para 11 (old 猪膏发煎方) orig=\"{p11['original_text']}\"")
p11_ann = p11['annotation']
# p11's annotation contains the 方药 and 曹注 - this is fine, it's the annotation for 猪膏发煎

# But we need to verify para 10 now
print(f"  After - para 10 (诸黄猪膏发煎) orig=\"{paras[10]['original_text']}\"")
print(f"  After - para 10 ann=\"{paras[10]['annotation']}\"")

print(f"Issue 1: Fixed. Inserted 2 new paragraphs before old段9.")

# ============================================================
# Issue 2: 第15章「黄瘅病」段13 — 注释误作原文
# ============================================================
# Para 12 has 黄瘅从湿得之... which is annotation for 茵陈五苓散 (para 10, which is now at index 12)
# Let me re-index: after inserting 2 paragraphs, indices shifted +2
# old para 10 (黄瘅病，茵陈五苓散主之) is now at index 12
# old para 11 (##### 茵陈五苓散方) is now at index 13
# old para 12 (黄瘅从湿得之...) is now at index 14

p14 = paras[14]
print(f"\nIssue 2: Para 14 orig[:50] = \"{p14['original_text'][:50]}\"")
print(f"Issue 2: Para 14 ann = \"{p14['annotation']}\"")

# 黄瘅从湿得之 is the 曹注 for 茵陈五苓散, merge into para 13 (方) or para 12 (原文)
# Per MD structure, the annotation comes after the 方名 paragraph
# So merge into para 13 (##### 茵陈五苓散方)
p13 = paras[13]
if p13['annotation']:
    p13['annotation'] += "\n\n" + p14['original_text']
else:
    p13['annotation'] = p14['original_text']

# Remove the now-empty annotation paragraph
paras.pop(14)
print(f"Issue 2: Fixed. Merged 黄瘅从湿得之  annotation into 茵陈五苓散方 paragraph.")

# ============================================================
# Issue 3: 第15章「黄瘅病」段17 — 注释误作原文 + 原文缺失
# ============================================================
# After fix 1+2, let me find the current para 17 (originally index 16, shifted +2-1 = +1)
# old para 16 is now at index 17
p17 = paras[17]
print(f"\nIssue 3: Para 17 orig[:60] = \"{p17['original_text'][:60]}\"")
print(f"Issue 3: Para 17 ann[:60] = \"{p17['annotation'][:60]}\"")

# Split into two paragraphs:
# Para A: 诸黄，腹痛而呕者，宜柴胡汤。 + annotation = current original_text
# Para B: 男子黄，小便自利，当与虚劳小建中汤。 + annotation = current annotation

para_a_orig = "诸黄，腹痛而呕者，宜柴胡汤。"
para_a_ann = p17['original_text']  # This is actually the 曹注

para_b_orig = "男子黄，小便自利，当与虚劳小建中汤。"
# The current annotation starts with the original text, need to extract
ann_text = p17['annotation']
# ann_text starts with "男子黄，小便自利，当与虚劳小建中汤。\n\n此亦肝胆乘脾之方治也..."
# We need to remove the original text prefix
if ann_text.startswith("男子黄"):
    # Find the first double newline or period to know where original text ends and 曹注 begins
    # The original text is: "男子黄，小便自利，当与虚劳小建中汤。"
    # After that comes "此亦肝胆乘脾之方治也..."
    parts = ann_text.split("\n\n", 1)
    if len(parts) > 1:
        para_b_ann = parts[1]
    else:
        para_b_ann = ""
else:
    para_b_ann = ann_text

# Also need to insert 柴胡汤方名 paragraph
para_fangming = {"original_text": "##### 柴胡汤方", "annotation": "小柴胡汤，见呕吐哕下利。按本方加减法：腹痛，去黄芩加芍药。"}

# Replace para 17 with para A
paras[17] = {"original_text": para_a_orig, "annotation": para_a_ann}
# Insert 方名 after para A
paras.insert(18, para_fangming)
# Insert para B after 方名
paras.insert(19, {"original_text": para_b_orig, "annotation": para_b_ann})

print(f"Issue 3: Fixed. Split into 诸黄腹痛而呕者 + 柴胡汤方 + 男子黄小建中汤.")

# Verify updated chapter 15
print(f"\nChapter 15 now has {len(paras)} paragraphs:")
for i, p in enumerate(paras):
    print(f"  {i}: {p['original_text'][:60]}")

print("\n" + "="*60)

# ============================================================
# Issue 4: 第22章「妇人杂病」段10 — 注释误作原文
# ============================================================
ch22_idx, ch22 = find_chapter("妇人杂病脉证并治第二十二")
print(f"\nIssue 4: Found chapter 22 at index {ch22_idx}")

paras22 = ch22['paragraphs']
p22_8 = paras22[8]
p22_9 = paras22[9]

print(f"  Para 8 orig[:60] = \"{p22_8['original_text'][:60]}\"")
print(f"  Para 8 ann[:60] = \"{p22_8['annotation'][:60]}\"")
print(f"  Para 9 orig[:60] = \"{p22_9['original_text'][:60]}\"")
print(f"  Para 9 ann[:60] = \"{p22_9['annotation'][:60]}\"")

# Para 9 original_text contains "少阴脉，手太阴动脉之尺部也..." - this is annotation for 狼牙汤 (para 8)
# Para 9 annotation contains "胃气下泄，阴吹而正喧..." - this is original text + 曹注 for 膏发煎

# Fix: Move para 9's original_text to para 8's annotation
p22_8_ann = p22_8['annotation']
if p22_8_ann:
    if not p22_8_ann.endswith('\n\n'):
        p22_8_ann += '\n\n'
else:
    p22_8_ann = ''
p22_8_ann += "少阴脉，手太阴动脉之尺部也，属下焦。脉滑而数，属下焦湿热，湿热注于下焦，或为淋带，或为太阳蓄血，犹未可定为阴蚀也。惟阴中痒痛腐烂，乃可决为阴中生疮。狼牙草近今所无，陈修园以为可用狼毒代之，未知验否？但此证有虫与毒，即世俗所谓杨梅疮，似不如蛤蟆散为宜，方用硫黄三钱，胡椒二钱，研末纳蛤蟆口中，用线扎住，外用黄泥和水厚涂，入炭火烧之，俟泥团红透取出，候冷去泥细研，忌用铁器，用时以小磨麻油调，以鸡毛蘸涂患处，去其毒水，数日毒尽，虽肉烂尽可愈。此葛仙《肘后方》也，自来注释家徒事说理，不求实用，岂仲师著书之旨欤？"

p22_8['annotation'] = p22_8_ann

# Change para 9 to be the 膏发煎 paragraph
p22_9['original_text'] = "胃气下泄，阴吹而正喧，此谷气之实也，膏发煎主之。"

# Parse the annotation: current annotation starts with "曹颖甫注：胃气下泄..."
# Remove the original text from annotation and set the remaining as 曹注
current_ann = p22_9['annotation']
# Remove "曹颖甫注：胃气下泄，阴吹而正喧，此谷气之实也，膏发煎主之。" prefix
if current_ann.startswith("曹颖甫注："):
    # Also need to keep the "曹颖甫注：" for the remaining annotation since it's all his commentary
    pass  # Keep as is, it's all 曹注 now

# But we need to check if the annotation contains the original text that we just moved to original_text
# Current ann: "曹颖甫注：胃气下泄，阴吹而正喧，此谷气之实也，膏发煎主之。\n\n猪膏半斤...\n\n上二味...\n\n凡大便燥实之证..."
# Need to remove the original text from the annotation
lines = current_ann.split('\n\n')
# First line is "曹颖甫注：胃气下泄，阴吹而正喧，此谷气之实也，膏发煎主之。"
# Remove the original text part from it
first_line = lines[0]
original_text_part = "胃气下泄，阴吹而正喧，此谷气之实也，膏发煎主之。"
if original_text_part in first_line:
    first_line = first_line.replace(original_text_part, "").strip()
    # Remove trailing "曹颖甫注：" if that's all that's left
    if first_line == "曹颖甫注：" or first_line == "曹颖甫注：":
        lines = lines[1:]  # Remove the first line entirely
    else:
        lines[0] = first_line
    p22_9['annotation'] = '\n\n'.join(lines)
else:
    # Keep annotation as is, it already has the correct 曹注 after the original text
    pass

print(f"Issue 4: Fixed. Moved annotation to para 8, set para 9 as 膏发煎.")

# Verify
print(f"  After - para 8 ann[:80] = \"{p22_8['annotation'][:80]}\"")
print(f"  After - para 9 orig = \"{p22_9['original_text']}\"")
print(f"  After - para 9 ann[:80] = \"{p22_9['annotation'][:80]}\"")

print("\n" + "="*60)

# ============================================================
# Issue 5: 第3章段12 — 乱码修复确认
# ============================================================
ch3_idx, ch3 = find_chapter("百合狐惑阴阳毒病脉证治第三")
print(f"\nIssue 5: Found chapter 3 at index {ch3_idx}")
p3_12 = ch3['paragraphs'][12]
print(f"  Para 12 orig = \"{p3_12['original_text']}\"")

if "尐蔘牯蛎散" in p3_12['original_text']:
    p3_12['original_text'] = p3_12['original_text'].replace("尐蔘牯蛎散", "瓜蒌牡蛎散")
    print(f"  Fixed to: \"{p3_12['original_text']}\"")
else:
    print(f"  No garbled text found (already fixed or different).")

print("Issue 5: Checked." )

print("\n" + "="*60)

# ============================================================
# Issue 6: 批量检查所有段落
# ============================================================
print("\nIssue 6: Batch checking all paragraphs...")

annotation_keywords = [
    "黄瘅之病", "黄瘅从湿", "此即", "由此", "凡", "大凡",
    "要之", "总之", "然则", "由是", "予读", "予谓", "予尝",
    "吾谓", "故曰", "故知", "然其", "但此", "惟此", "盖以"
]

# Also check if annotation is empty but original text reads like annotation
issues_found_global = []

for ch_idx, ch in enumerate(book['chapters']):
    for p_idx, p in enumerate(ch['paragraphs']):
        orig = p['original_text']
        ann = p['annotation']
        
        # Check if original text starts with annotation-like patterns
        for kw in annotation_keywords:
            if orig.startswith(kw):
                issues_found_global.append((ch_idx, p_idx, kw, orig[:80]))
                break
        
        # Check if annotation contains "曹颖甫注" as prefix while original_text doesn't look like original 金匮 text
        if ann.startswith("曹颖甫注：") and not ann.startswith("曹颖甫注：方"):
            # This might be fine - some annotations explicitly start with "曹颖甫注"
            pass

if issues_found_global:
    print(f"\n  Found {len(issues_found_global)} potential annotation-original swaps:")
    for ch_idx, p_idx, kw, preview in issues_found_global:
        ch_name = book['chapters'][ch_idx]['title'][:20]
        print(f"    Ch{ch_idx} ({ch_name}) para {p_idx}: starts with '{kw}' -> \"{preview}\"")
else:
    print("  No additional annotation-original swaps found.")

# Also check for empty annotations that might indicate lost content
print("\n  Checking for paragraphs with empty annotations...")
for ch_idx, ch in enumerate(book['chapters']):
    for p_idx, p in enumerate(ch['paragraphs']):
        if not p['annotation'] and not p['original_text'].startswith('#'):
            # Check if this looks like an annotation without original text
            orig = p['original_text']
            if len(orig) > 40 and not any(orig.startswith(prefix) for prefix in 
                ["问曰", "师曰", "夫", "病者", "诸", "太阳", "阳明", "少阳", "厥阴", "妇人",
                 "百合病", "狐惑", "阳毒", "阴毒", "疟病", "中风", "血痹", "虚劳",
                 "肺痿", "奔豚", "胸痹", "腹满", "五脏", "痰饮", "消渴", "水气",
                 "黄瘅", "惊悸", "呕吐", "疮痈", "趺蹶"]):
                print(f"    Potential issue Ch{ch_idx} para {p_idx}: empty ann, orig=\"{orig[:60]}\"")

print("\nAll issues processed.")

# ============================================================
# Write back
# ============================================================
with open(JSON_PATH, 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print(f"\nSaved to {JSON_PATH}")
