import tensorflow as tf
from tensorflow.keras.models import load_model

# Save the current model to a Keras H5 file
model.save("interrupted_model.h5")
print("Model saved as 'interrupted_model.h5'")


# Load the saved Keras model
model = load_model("interrupted_model.h5")

# Convert the loaded model to TensorFlow Lite format
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()

# Save the TensorFlow Lite model to a file
with open("interrupted_model_converted.tflite", "wb") as f:
    f.write(tflite_model)

print("Model successfully converted and saved as 'interrupted_model_converted.tflite'.")
