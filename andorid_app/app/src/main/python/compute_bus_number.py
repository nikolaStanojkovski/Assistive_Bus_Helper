import io
import torch
import torch.nn as nn
import torch.nn.functional as F
from torchvision import transforms
from PIL import Image


class ImageClassificationBase(nn.Module):

    def training_step(self, batch):
        images, labels = batch
        out = self(images)  # Generate predictions
        loss = F.cross_entropy(out, labels)  # Calculate loss
        return loss

    def validation_step(self, batch):
        images, labels = batch
        out = self(images)  # Generate predictions
        loss = F.cross_entropy(out, labels)  # Calculate loss
        acc = accuracy(out, labels)  # Calculate accuracy
        return {'val_loss': loss.detach(), 'val_acc': acc}

    def validation_epoch_end(self, outputs):
        batch_losses = [x['val_loss'] for x in outputs]
        epoch_loss = torch.stack(batch_losses).mean()  # Combine losses
        batch_accs = [x['val_acc'] for x in outputs]
        epoch_acc = torch.stack(batch_accs).mean()  # Combine accuracies
        return {'val_loss': epoch_loss.item(), 'val_acc': epoch_acc.item()}

    def epoch_end(self, epoch, result):
        print("Epoch [{}], train_loss: {:.4f}, val_loss: {:.4f}, val_acc: {:.4f}".format(
            epoch, result['train_loss'], result['val_loss'], result['val_acc']))


class BusNumberClassification(ImageClassificationBase):
    def __init__(self):
        super().__init__()
        self.network = nn.Sequential(

            nn.Conv2d(3, 32, kernel_size=3, padding=1),
            nn.ReLU(),
            nn.Conv2d(32, 64, kernel_size=3, stride=1, padding=1),
            nn.ReLU(),
            nn.MaxPool2d(2, 2),

            nn.Conv2d(64, 128, kernel_size=3, stride=1, padding=1),
            nn.ReLU(),
            nn.Conv2d(128, 128, kernel_size=3, stride=1, padding=1),
            nn.ReLU(),
            nn.MaxPool2d(2, 2),

            nn.Conv2d(128, 256, kernel_size=3, stride=1, padding=1),
            nn.ReLU(),
            nn.Conv2d(256, 256, kernel_size=3, stride=1, padding=1),
            nn.ReLU(),
            nn.MaxPool2d(2, 2),

            nn.Flatten(),
            nn.Linear(82944, 1024),
            nn.ReLU(),
            nn.Linear(1024, 512),
            nn.ReLU(),
            nn.Linear(512, 35)  # 6
        )

    def forward(self, xb):
        return self.network(xb)


dataset_classes = ['11a', '15', '15a', '19', '2', '21a', '22', '22a', '23', '24', '25', '27', '3', '31', '4', '41',
                   '42', '44', '45', '47', '5', '50', '53', '57', '60', '62', '63', '65', '65b', '66', '67', '7', '70',
                   '71', '74']

def to_device(data, device):
    "Move data to the device"
    if isinstance(data, (list, tuple)):
        return [to_device(x, device) for x in data]
    return data.to(device, non_blocking=True)


def get_static_model():
    "Load OCR model"
    model = BusNumberClassification()
    model.load_state_dict(torch.load(
        '/data/data/mk.ukim.finki.assistivebushelper/files/chaquopy/AssetFinder/app/fastspeech2/busnumberclassification/bus_number_classification.pth', map_location='cpu'))
    model.eval()
    return model

def predict_img_class(imgBytes, model):
    """ Predict the class of image and Return Predicted Class"""
    img = Image.open(io.BytesIO(imgBytes))
    img = transforms.Resize((150,150))(img)
    img = transforms.ToTensor()(img)
    img = to_device(img.unsqueeze(0), 'cpu')
    prediction = model(img)
    _, preds = torch.max(prediction, dim=1)
    bus_number = dataset_classes[preds[0].item()]
    print("Successful bus number inference with OCR model: " + bus_number)
    return bus_number