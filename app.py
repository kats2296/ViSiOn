from flask import Flask, request
import numpy as np
import os
import pickle
import base64
from keras.models import load_model
from keras.preprocessing import image as image_utils

app = Flask(__name__)

print "hellooo"
model = load_model('dundun.h5')
label_list_path = 'datasets/cifar-10-batches-py/batches.meta'
keras_dir = os.path.expanduser(os.path.join('~', '.keras'))
datadir_base = os.path.expanduser(keras_dir)
if not os.access(datadir_base, os.W_OK):
    datadir_base = os.path.join('/tmp', '.keras')
label_list_path = os.path.join(datadir_base, label_list_path)
with open(label_list_path, mode='rb') as f:
    labels = pickle.load(f)


def prediction():
    # building the path
    # testing for a single image
    test_image = image_utils.load_img('image.jpeg', target_size=(64, 64))
    test_image = image_utils.img_to_array(test_image)
    test_image = np.expand_dims(test_image, axis=0)
    result = model.predict_on_batch(test_image)
    # print(result)
    predicted_label = labels['label_names'][np.argmax(result)]
    return predicted_label


@app.route('/', methods=['GET', 'POST'])
def start():
    if request.method == 'POST':
        strng = request.values
        imageInstring = strng['image']
        imgdata = base64.b64decode(imageInstring)

        with open("image.jpeg", "wb") as fh:
            fh.write(imgdata)

        pred = prediction()
        return pred
    else:
        return "<h1>use post method</h1>"


@app.route('/hello/<username>')
def hello(username):
    return '<h1>u want soluchan %s ?</h1>' % username


if __name__ == '__main__':
    app.run(port=8080, use_reloader=True)
