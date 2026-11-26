"""Rasterize the technical Thai preview with the pinned Noto release font."""

import argparse
import hashlib
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, features

FONT_SHA256 = "d941d72ac9043b0a376fcc36f1dd538abfec690259915e89f68ca48cc5772726"
STRINGS = (
    "ข่าว", "หุ้น", "ร้านค้า", "ธนาคาร", "กลับหน้าหลัก",
    "ยังไม่พร้อมใช้งาน", "ผู้ใช้กำลังตั้งค่า", "น้ำขึ้นให้รีบตัก",
    "เก้าอี้ / กุ้ง / กำไร", "Bullbear — ภาษาไทย 123",
)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("font", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    if hashlib.sha256(args.font.read_bytes()).hexdigest() != FONT_SHA256:
        parser.error("expected the full regular TTF from Noto Sans Thai Looped v2.000")
    if not features.check("raqm"):
        parser.error("Pillow with libraqm and HarfBuzz is required")

    image = Image.new("RGBA", (960, 570), "#151529")
    draw = ImageDraw.Draw(image)
    font = ImageFont.truetype(args.font, 32, layout_engine=ImageFont.Layout.RAQM)
    small = ImageFont.truetype(args.font, 24, layout_engine=ImageFont.Layout.RAQM)
    draw.rectangle((0, 0, 959, 569), outline="#464657", width=2)
    draw.rectangle((2, 2, 957, 5), fill="#4DA4DD")
    draw.text((32, 32), "Thai shaping preview", font=small, fill="#E4E4EF", anchor="lt")
    draw.text((928, 32), "Esc to close", font=small, fill="#B4B4C6", anchor="rt")
    for index, string in enumerate(STRINGS):
        x = 44 if index < 5 else 480
        y = 135 + (index % 5) * 85
        left, top, right, bottom = draw.textbbox((x, y), string, font=font,
                                                 anchor="lm", language="th")
        if left < 0 or top < 65 or right >= (470 if index < 5 else 950) or bottom >= 560:
            raise ValueError(f"Preview label exceeds its column: {index}")
        draw.text((x, y), string, font=font, fill="#E4E4EF", anchor="lm", language="th")
    image.save(args.output, optimize=True)


if __name__ == "__main__":
    main()
