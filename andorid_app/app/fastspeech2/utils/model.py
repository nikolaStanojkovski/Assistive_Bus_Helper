import os
from os.path import dirname, join
import json

import torch
import numpy as np

from fastspeech2.model import FastSpeech2, ScheduledOptim
from fastspeech2 import hifigan


def get_model(restore_step, configs, device, train=False):
    (preprocess_config, model_config, train_config) = configs

    model = FastSpeech2(preprocess_config, model_config).to(device)
    if restore_step:
        ckpt_path = "/data/data/mk.ukim.finki.androidkotlinapplication/files/chaquopy/AssetFinder/app/fastspeech2/output/ckpt/LJSpeech/{}.pth.tar".format(restore_step)
        ckpt = torch.load(ckpt_path, map_location='cpu')
        model.load_state_dict(ckpt["model"])

    if train:
        scheduled_optim = ScheduledOptim(
            model, train_config, model_config, restore_step
        )
        if restore_step:
            scheduled_optim.load_state_dict(ckpt["optimizer"])
        model.train()
        return model, scheduled_optim

    model.eval()
    model.requires_grad_ = False
    return model


def get_param_num(model):
    num_param = sum(param.numel() for param in model.parameters())
    return num_param


def get_vocoder(config, device):
    name = config["vocoder"]["model"]
    speaker = config["vocoder"]["speaker"]

    if name == "MelGAN":
        if speaker == "LJSpeech":
            vocoder = torch.hub.load(
                "descriptinc/melgan-neurips", "load_melgan", "linda_johnson"
            )
        elif speaker == "universal":
            vocoder = torch.hub.load(
                "descriptinc/melgan-neurips", "load_melgan", "multi_speaker"
            )
        vocoder.mel2wav.eval()
        vocoder.mel2wav.to(device)
    elif name == "HiFi-GAN":
        with open("/data/data/mk.ukim.finki.androidkotlinapplication/files/chaquopy/AssetFinder/app/fastspeech2/hifigan/config.json", "r") as f:
            config = json.load(f)
        config = hifigan.AttrDict(config)
        vocoder = hifigan.Generator(config)
        if speaker == "LJSpeech":
            ckpt = torch.load("/data/data/mk.ukim.finki.androidkotlinapplication/files/chaquopy/AssetFinder/app/fastspeech2/hifigan/generator_LJSpeech.pth.tar", map_location='cpu')
        elif speaker == "universal":
            ckpt = torch.load("/data/data/mk.ukim.finki.androidkotlinapplication/files/chaquopy/AssetFinder/app/fastspeech2/hifigan/generator_universal.pth.tar", map_location='cpu')
        vocoder.load_state_dict(ckpt["generator"])
        vocoder.eval()
        vocoder.remove_weight_norm()
        vocoder.to(device)

    return vocoder


def vocoder_infer(mels, vocoder, model_config, preprocess_config, lengths=None):
    name = model_config["vocoder"]["model"]
    with torch.no_grad():
        if name == "MelGAN":
            wavs = vocoder.inverse(mels / np.log(10))
        elif name == "HiFi-GAN":
            wavs = vocoder(mels).squeeze(1)

    wavs = (
        wavs.cpu().numpy()
        * preprocess_config["preprocessing"]["audio"]["max_wav_value"]
    ).astype("int16")
    wavs = [wav for wav in wavs]

    for i in range(len(mels)):
        if lengths is not None:
            wavs[i] = wavs[i][: lengths[i]]

    return wavs
