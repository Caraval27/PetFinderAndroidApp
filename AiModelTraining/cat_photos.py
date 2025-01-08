import tensorflow as tf
import os
import numpy as np
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras import layers, models
from PIL import Image, UnidentifiedImageError
import pathlib

# Step 1: Load the data from the 'dog_photos' folder
data_dir = pathlib.Path("cat_and_dog_photos")  # Ensure 'dog_photos' folder is correctly located

# Step 2: Function to check if an image is valid
def is_valid_image(file_path):
    try:
        if file_path.endswith(('.png', '.jpg', '.jpeg')):  # Only process common image formats
            with Image.open(file_path) as img:
                img.verify()  # Verify that it is, in fact, an image
            return True
    except (UnidentifiedImageError, IOError):
        return False

# Step 3: Filter out invalid images
valid_images = []
for subdir, dirs, files in os.walk(data_dir):
    for file in files:
        file_path = os.path.join(subdir, file)
        if is_valid_image(file_path):
            valid_images.append(file_path)
        else:
            print(f"Invalid image file: {file_path}")

# Step 4: Set up ImageDataGenerator for data loading and augmentation
image_size = 224  # Define image size to resize images
batch_size = 32  # Number of images per batch

# Data augmentation and preprocessing
train_datagen = ImageDataGenerator(
    rescale=1./255,  # Normalize pixel values between 0 and 1
    rotation_range=30,
    width_shift_range=0.2,
    height_shift_range=0.2,
    shear_range=0.2,
    zoom_range=0.2,
    horizontal_flip=True,
    fill_mode='nearest',
    validation_split=0.2  # Add validation split (20% validation)
)

# Step 5: Set up the train and validation data generators
train_generator = train_datagen.flow_from_directory(
    data_dir,
    target_size=(image_size, image_size),
    batch_size=batch_size,
    class_mode='categorical',  # For multi-class classification
    subset='training',  # Use only training data
)

validation_generator = train_datagen.flow_from_directory(
    data_dir,
    target_size=(image_size, image_size),
    batch_size=batch_size,
    class_mode='categorical',
    subset='validation',  # Use only validation data
)

# Step 6: Define the model architecture
model = models.Sequential([
    layers.Conv2D(32, (3, 3), activation='relu', input_shape=(image_size, image_size, 3)),
    layers.MaxPooling2D((2, 2)),
    layers.Conv2D(64, (3, 3), activation='relu'),
    layers.MaxPooling2D((2, 2)),
    layers.Conv2D(128, (3, 3), activation='relu'),
    layers.MaxPooling2D((2, 2)),
    layers.Flatten(),
    layers.Dense(128, activation='relu'),
    layers.Dense(len(train_generator.class_indices), activation='softmax')  # Number of classes
])

# Step 7: Compile the model
model.compile(optimizer='adam', 
              loss='categorical_crossentropy', 
              metrics=['accuracy'])

# Step 8: Train the model
epochs = 20  # Define the number of epochs to train the model
model.fit(
    train_generator,
    epochs=epochs,
    steps_per_epoch=train_generator.samples // batch_size,
    validation_data=validation_generator,
    validation_steps=validation_generator.samples // batch_size
)

# Step 9: Save the model as a .tflite file
# Convert the trained model to TensorFlow Lite format
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

# Save the model to a .tflite file
with open("trained_model_cat_and_dog.tflite", "wb") as f:
    f.write(tflite_model)

print("Model has been converted to .tflite and saved as 'trained_model_cat_and_dog.tflite'")
