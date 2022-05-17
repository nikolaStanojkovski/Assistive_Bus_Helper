import argparse
import numpy as np
import re
import torch
import yaml
from fastspeech2.dataset import TextDataset
import nltk
import os

nltkTimeoutCounter = 0
while True:
    try:
        nltk.data.find('taggers/averaged_perceptron_tagger.zip')
    except:
        nltk.download('averaged_perceptron_tagger')
    try:
        nltk.data.find('corpora/cmudict.zip')
        break
    except:
        nltk.download('cmudict')

    nltkTimeoutCounter += 1
    if nltkTimeoutCounter == 15:
            break

from g2p_en import G2p
from os.path import dirname, join
from pypinyin import pinyin, Style
from string import punctuation
from fastspeech2.text import text_to_sequence
from torch.utils.data import DataLoader
from fastspeech2.utils.model import get_model, get_vocoder
from fastspeech2.utils.tools import to_device, synth_samples

from os.path import dirname, join
from scipy.io import wavfile
from com.chaquo.python import Python


# Define all global properties
device = "cpu"
restore_step = 900000
mode = "single"
source = None
speaker_id = 0
preprocess_config = "fastspeech2/config/LJSpeech/preprocess.yaml"
model_config = "fastspeech2/config/LJSpeech/model.yaml"
train_config = "fastspeech2/config/LJSpeech/train.yaml"
pitch_control = 1.0
energy_control = 1.0
duration_control = 1.0

# Read all configurations
preprocess_config = yaml.load(
    open(join(dirname(__file__), preprocess_config), "r"), Loader=yaml.FullLoader
)
model_config = yaml.load(open(join(dirname(__file__), model_config), "r"), Loader=yaml.FullLoader)
train_config = yaml.load(open(join(dirname(__file__), train_config), "r"), Loader=yaml.FullLoader)
configs = (preprocess_config, model_config, train_config)


def read_lexicon(lex_path):
    lexicon = {}
    with open(lex_path) as f:
        for line in f:
            temp = re.split(r"\s+", line.strip("\n"))
            word = temp[0]
            phones = temp[1:]
            if word.lower() not in lexicon:
                lexicon[word.lower()] = phones
    return lexicon


def preprocess_english(text, preprocess_config):
    text = text.rstrip(punctuation)
    lexicon = read_lexicon("/data/data/mk.ukim.finki.assistivebushelper/files/chaquopy/AssetFinder/app/fastspeech2/lexicon/librispeech-lexicon.txt")

    g2p = G2p()
    phones = []
    words = re.split(r"([,;.\-\?\!\s+])", text)
    for w in words:
        if w.lower() in lexicon:
            phones += lexicon[w.lower()]
        else:
            phones += list(filter(lambda p: p != " ", g2p(w)))
    phones = "{" + "}{".join(phones) + "}"
    phones = re.sub(r"\{[^\w\s]?\}", "{sp}", phones)
    phones = phones.replace("}{", " ")

    print("Raw Text Sequence: {}".format(text))
    print("Phoneme Sequence: {}".format(phones))
    sequence = np.array(
        text_to_sequence(
            phones, preprocess_config["preprocessing"]["text"]["text_cleaners"]
        )
    )

    return np.array(sequence)


def synthesize(model, step, configs, vocoder, batchs, control_values, text):
    preprocess_config, model_config, train_config = configs
    pitch_control, energy_control, duration_control = control_values

    for batch in batchs:
        batch = to_device(batch, device)
        with torch.no_grad():
            # Forward
            output = model(
                *(batch[2:]),
                p_control=pitch_control,
                e_control=energy_control,
                d_control=duration_control
            )
            synth_samples(
                batch,
                output,
                vocoder,
                model_config,
                preprocess_config,
                train_config["path"]["result_path"],
            )


def get_static_model():
    return get_model(restore_step, configs, device, train=False)


def get_static_vocoder():
    return get_vocoder(model_config, device)


def main(text, model, vocoder):
    # Check source texts

    assert source is None and text is not None

    # Preprocess texts
    text = str(text)
    ids = raw_texts = [text[:100]]
    speakers = np.array([speaker_id])
    texts = np.array([preprocess_english(text, preprocess_config)])
    text_lens = np.array([len(texts[0])])
    batchs = [(ids, raw_texts, speakers, texts, text_lens, max(text_lens))]
    control_values = pitch_control, energy_control, duration_control
    synthesize(model, restore_step, configs, vocoder, batchs, control_values, text)
