# -*- coding: utf-8 -*-
"""生成「伟哥天眼查」图标

设计意图（三层抽象）：
  1. 杏仁形眼眶        → 「天眼查」的"看"：一眼看穿企业底细
  2. 琥珀色虹膜         → 伟哥系列的品牌色，也是"聚光/洞察"
  3. 虹膜里的深色 W 折线 → 既是 WeiGe 的 W，也是"数据趋势线"

方法：1024 分辨率超采样绘制 → LANCZOS 缩到各密度，保证小尺寸不糊。
"""
import os
import sys

from PIL import Image, ImageDraw, ImageFilter

sys.stdout.reconfigure(encoding="utf-8")

RES = 1024
OUT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES_DIR = os.path.join(OUT, "app", "src", "main", "res")

BG_TOP = (14, 18, 26)
BG_BOTTOM = (8, 10, 15)
AMBER = (255, 122, 24)
AMBER_LIGHT = (255, 176, 103)
DARK = (10, 12, 18)

DENSITIES = {
    "mdpi": 1.0,
    "hdpi": 1.5,
    "xhdpi": 2.0,
    "xxhdpi": 3.0,
    "xxxhdpi": 4.0,
}


def vertical_gradient(size, top, bottom):
    img = Image.new("RGB", (1, size[1]))
    px = img.load()
    for y in range(size[1]):
        t = y / max(1, size[1] - 1)
        px[0, y] = tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3))
    return img.resize(size, Image.BILINEAR)


def draw_mark(size, with_bg=True, padding=0.0, corner=True):
    """size 为目标画布边长；padding 为内容内缩比例（自适应图标安全区用）"""
    S = RES
    canvas = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    rng = 3  # 超采样倍数由调用方决定（这里直接在高分辨率画）

    # ---------- 背景 ----------
    if with_bg:
        bg = vertical_gradient((S, S), BG_TOP, BG_BOTTOM).convert("RGBA")
        # 中心径向微光，避免死黑
        glow = Image.new("L", (S, S), 0)
        gd = ImageDraw.Draw(glow)
        cx, cy = S // 2, int(S * 0.47)
        for i in range(28, 0, -1):
            rr = int(S * 0.52 * i / 28)
            gd.ellipse([cx - rr, cy - rr, cx + rr, cy + rr], fill=int(26 * (1 - i / 28)))
        glow = glow.filter(ImageFilter.GaussianBlur(S // 14))
        amber_glow = Image.new("RGBA", (S, S), AMBER + (255,))
        amber_glow.putalpha(glow)
        bg = Image.alpha_composite(bg, amber_glow)

        mask = Image.new("L", (S, S), 0)
        md = ImageDraw.Draw(mask)
        if corner:
            radius = int(S * 0.225)      # 接近 Android 自适应图标的安全圆角
            md.rounded_rectangle([0, 0, S - 1, S - 1], radius=radius, fill=255)
        else:
            md.ellipse([0, 0, S - 1, S - 1], fill=255)
        bg.putalpha(mask)
        canvas = Image.alpha_composite(canvas, bg)

    # ---------- 内容 ----------
    content = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(content)

    scale = 1.0 - padding
    cx, cy = S / 2, S / 2
    ew = S * 0.72 * scale          # 眼眶宽度
    eh = S * 0.40 * scale          # 眼眶高度
    stroke = int(S * 0.052 * scale)

    # 眼眶：两段二次贝塞尔拼成杏仁形（用多点采样逼近）
    left = (cx - ew / 2, cy)
    right = (cx + ew / 2, cy)
    top_ctrl = (cx, cy - eh * 1.32)
    bot_ctrl = (cx, cy + eh * 1.32)

    def bezier(p0, p1, p2, n=120):
        pts = []
        for i in range(n + 1):
            t = i / n
            x = (1 - t) ** 2 * p0[0] + 2 * (1 - t) * t * p1[0] + t ** 2 * p2[0]
            y = (1 - t) ** 2 * p0[1] + 2 * (1 - t) * t * p1[1] + t ** 2 * p2[1]
            pts.append((x, y))
        return pts

    def stamp(pts, radius, color):
        """沿路径逐点盖圆章。
        Pillow 的 line(joint="curve") 会在每个折点处冒出细刺（10px 下清晰可见），
        改为密集圆章叠出圆头描边，边缘干净且首尾天然是圆的。"""
        for (x, y) in pts:
            d.ellipse([x - radius, y - radius, x + radius, y + radius], fill=color)

    # 采样 2 倍密度，保证圆章间距 < 1px，缩小后看不出扇贝边
    top_curve = bezier(left, top_ctrl, right, n=760)
    bot_curve = bezier(right, bot_ctrl, left, n=760)

    stamp(top_curve, stroke / 2.0, AMBER + (255,))
    stamp(bot_curve, stroke / 2.0, AMBER + (255,))

    # 虹膜：大圆，占据眼眶主要面积，保证小尺寸下依然可辨
    iris_r = S * 0.155 * scale
    d.ellipse([cx - iris_r, cy - iris_r, cx + iris_r, cy + iris_r],
              fill=AMBER + (255,))
    # 虹膜外发光
    halo = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    hd = ImageDraw.Draw(halo)
    hd.ellipse([cx - iris_r * 1.22, cy - iris_r * 1.22,
                cx + iris_r * 1.22, cy + iris_r * 1.22], fill=AMBER_LIGHT + (110,))
    halo = halo.filter(ImageFilter.GaussianBlur(S // 44))
    content = Image.alpha_composite(content, halo)
    d = ImageDraw.Draw(content)
    d.ellipse([cx - iris_r, cy - iris_r, cx + iris_r, cy + iris_r], fill=AMBER + (255,))

    # 虹膜内的 W 折线（品牌 + 数据趋势）用底色"镂空"
    r = iris_r
    w_pts = [
        (cx - 0.60 * r, cy - 0.30 * r),
        (cx - 0.22 * r, cy + 0.44 * r),
        (cx + 0.00 * r, cy - 0.10 * r),
        (cx + 0.22 * r, cy + 0.44 * r),
        (cx + 0.60 * r, cy - 0.30 * r),
    ]
    lw = max(3, int(r * 0.22))
    # 同样用圆章描边，避免折点冒刺
    for i in range(len(w_pts) - 1):
        (x0, y0), (x1, y1) = w_pts[i], w_pts[i + 1]
        steps = max(2, int(((x1 - x0) ** 2 + (y1 - y0) ** 2) ** 0.5))
        for s in range(steps + 1):
            t = s / steps
            px_, py_ = x0 + (x1 - x0) * t, y0 + (y1 - y0) * t
            d.ellipse([px_ - lw / 2, py_ - lw / 2,
                       px_ + lw / 2, py_ + lw / 2], fill=DARK + (255,))

    # 眼角两枚数据节点
    node_r = S * 0.016 * scale
    for nx in (cx - ew / 2 - S * 0.008 * scale, cx + ew / 2 + S * 0.008 * scale):
        d.ellipse([nx - node_r, cy - node_r, nx + node_r, cy + node_r],
                  fill=AMBER_LIGHT + (255,))

    return Image.alpha_composite(canvas, content)


def save(img, path, px):
    img.resize((px, px), Image.LANCZOS).save(path, optimize=True)
    print(f"  {os.path.relpath(path, OUT):<62} {px}x{px}")


def main():
    print("生成图标…")
    master = draw_mark(RES, with_bg=True, corner=True)
    round_master = draw_mark(RES, with_bg=True, corner=False)
    # 自适应图标前景：内容缩到 2/3，正好让眼眶宽度 = 108dp 画布的 48%
    # （72dp 可见区里的 72%），视觉重量与普通图标一致；四周留出掩码安全边距。
    fg_master = draw_mark(RES, with_bg=False, padding=1 / 3)
    fg_master.save(os.path.join(OUT, "docs", "_fg_master.png"))

    for dpi, factor in DENSITIES.items():
        folder = os.path.join(RES_DIR, f"mipmap-{dpi}")
        os.makedirs(folder, exist_ok=True)
        base = int(48 * factor)
        save(master, os.path.join(folder, "ic_launcher.png"), base)
        save(round_master, os.path.join(folder, "ic_launcher_round.png"), base)
        # 自适应图标前景画布 = 108dp
        save(fg_master, os.path.join(folder, "ic_launcher_foreground.png"),
             int(108 * factor))

    # 商店/文档用大图
    os.makedirs(os.path.join(OUT, "docs"), exist_ok=True)
    save(master, os.path.join(OUT, "docs", "icon_512.png"), 512)

    # 模拟系统组合自适应图标后的样子（背景色 = colors.xml 的 ic_launcher_background）
    fg = fg_master.resize((RES, RES), Image.LANCZOS)
    for shape in ("rounded", "circle"):
        comp = Image.alpha_composite(Image.new("RGBA", (RES, RES), (11, 13, 18, 255)), fg)
        mask = Image.new("L", (RES, RES), 0)
        md = ImageDraw.Draw(mask)
        if shape == "rounded":
            md.rounded_rectangle([0, 0, RES - 1, RES - 1], radius=int(RES * 0.225), fill=255)
        else:
            md.ellipse([0, 0, RES - 1, RES - 1], fill=255)
        comp.putalpha(mask)
        comp.resize((512, 512), Image.LANCZOS).save(
            os.path.join(OUT, "docs", f"icon_adaptive_{shape}.png"))
        print(f"  docs\\icon_adaptive_{shape}.png{'':<36} 512x512")
    os.remove(os.path.join(OUT, "docs", "_fg_master.png"))
    print("完成")


if __name__ == "__main__":
    main()
