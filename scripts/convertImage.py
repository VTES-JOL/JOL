import os.path
import shutil

from krcg.vtes import VTES
from urllib.parse import urlparse

if __name__ == '__main__':
    VTES.load()

    for card in VTES:
        url = urlparse(card.url)
        base_name = os.path.basename(url.path)
        shutil.copy(f"/Users/shannon/static/card/{base_name}", f"/Users/shannon/static/images/{card.id}.jpg")
