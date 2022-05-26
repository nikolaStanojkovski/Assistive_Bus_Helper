# Assistive Bus Helper
-----------------------------------------------------------------------------------
## A Solution for In-house Bus Line Recognition
-----------------------------------------------------------------------------------
### Contributors: Monika Simjanovska, Kostadin Mishev, Tashko Pavlov, Mario Stojchevski & Nikola Stanojkovski
-----------------------------------------------------------------------------------

Daily activities still represent real challenges for visually impaired individuals due to the lack of affordable, appropriate assistive devices. The absence of assistive tools triggers an infinite loop of inappropriate education at its basics, followed by limited lifestyle development that leads to frustration, low confidence, reduced autonomy, and often physical safety risks. There are several methods and devices that are used to guide visually impaired persons, and all of them have their advantages and disadvantages.

<b>Assistive Bus Helper</b> is an Android application that provides an interactive manner for the visually impaired individuals to hear which bus line numbers are passing next to them, by just opening up the application and clicking on one button.

The applcation uses integrated python scripts as tools for loading <i>machine learning</i> models and creating predictions and inferences with them. <a href="https://chaquo.com/"><b>Chaquopy</b></a> was the Python SDK for Android which enabled this.

The application uses a pipeline of machine learning models which do all the complex processing: FastSpeech 2 as a text-to-speech model of the bus line number, YOLOX and EasyOCR for the OCR recognition and prediction from the automatically taken image.

-----------------------------------------------------------------------------------

<a href="https://github.com/ming024/FastSpeech2"><b>FastSpeech 2</b></a> is the PyTorch text-to-speech machine learning model which generated audio output for the predicted bus line number provided by the other two OCR models.

<a href="https://github.com/Megvii-BaseDetection/YOLOX"><b>YOLOX</b></a> is the machine learning model which was used for predicting which part of the taken image consisted of the bus line number and cropping it.

<a href="https://github.com/JaidedAI/EasyOCR"><b>EasyOCR</b></a> is the machine learning model which was used for the prediction of the bus number from the cropped image that was provided by YOLOX.