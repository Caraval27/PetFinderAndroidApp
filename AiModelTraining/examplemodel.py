import os
import shutil
import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras import layers, models, regularizers
from tensorflow.keras.callbacks import EarlyStopping, ReduceLROnPlateau, TensorBoard
from PIL import Image, UnidentifiedImageError
import pathlib

# Step 1: Define the data directory
data_dir = pathlib.Path("cat_and_dog_photos")  # Ensure 'cat_and_dog_photos' folder is correctly located

# Step 2: Function to check if an image is valid
def is_valid_image(file_path):
    try:
        if file_path.endswith(('.png', '.jpg', '.jpeg')):  # Only process common image formats
            with Image.open(file_path) as img:
                img.verify()  # Verify that it is, in fact, an image
            return True
    except (UnidentifiedImageError, IOError):
        return False

# Step 3: Remove the creation of the 'invalid_images' directory
# invalid_dir = data_dir / "invalid_images"
# invalid_dir.mkdir(exist_ok=True)

# Step 4: Filter out invalid images and delete them
for subdir, dirs, files in os.walk(data_dir):
    for file in files:
        file_path = os.path.join(subdir, file)
        if not is_valid_image(file_path):
            print(f"Invalid image file: {file_path}")
            # Delete invalid image
            os.remove(file_path)

# Step 5: Set up ImageDataGenerator for data loading and augmentation
image_size = 224  # Define image size to resize images
batch_size = 32  # Increased batch size for better convergence

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
    brightness_range=[0.8, 1.2],  # Adding brightness variation
    channel_shift_range=20.0,  # Adding color jitter
    validation_split=0.2  # Add validation split (20% validation)
)

# Step 6: Set up the train and validation data generators
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

# Step 7: Define the model architecture with Batch Normalization and Dropout
model = models.Sequential([
    layers.Conv2D(64, (3, 3), activation='relu', input_shape=(image_size, image_size, 3),
                  kernel_regularizer=regularizers.l2(0.0001)),
    layers.BatchNormalization(),
    layers.MaxPooling2D((2, 2)),
    layers.Dropout(0.3),
    
    layers.Conv2D(128, (3, 3), activation='relu', kernel_regularizer=regularizers.l2(0.0001)),
    layers.BatchNormalization(),
    layers.MaxPooling2D((2, 2)),
    layers.Dropout(0.3),
    
    layers.Conv2D(256, (3, 3), activation='relu', kernel_regularizer=regularizers.l2(0.0001)),
    layers.BatchNormalization(),
    layers.MaxPooling2D((2, 2)),
    
    layers.Flatten(),
    layers.Dense(512, activation='relu', kernel_regularizer=regularizers.l2(0.0001)),
    layers.Dropout(0.5),
    layers.Dense(len(train_generator.class_indices), activation='softmax')
])

# Step 8: Compile the model with a learning rate of 0.0005
model.compile(optimizer=tf.keras.optimizers.Adam(learning_rate=0.0005),  # Optimized learning rate
              loss='categorical_crossentropy',
              metrics=['accuracy'])

# Step 9: Implement Early Stopping and Learning Rate Scheduler
early_stopping = EarlyStopping(monitor='val_accuracy', patience=15, restore_best_weights=True)

# Learning Rate Scheduler to reduce the learning rate on plateau
lr_scheduler = ReduceLROnPlateau(monitor='val_loss', factor=0.5, patience=5, min_lr=1e-7)

# Step 10: Add TensorBoard for monitoring training
tensorboard = TensorBoard(log_dir='logs')

# Step 11: Train the model
epochs = 150  # Increased epochs for better convergence
model.fit(
    train_generator,
    epochs=epochs,
    steps_per_epoch=train_generator.samples // batch_size,
    validation_data=validation_generator,
    validation_steps=validation_generator.samples // batch_size,
    callbacks=[early_stopping, lr_scheduler, tensorboard]
)

# Step 12: Save the model as a .tflite file
# Convert the trained model to TensorFlow Lite format
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]  # Optimize the model
tflite_model = converter.convert()

# Save the model to a .tflite file
with open("trained_model_cat_and_dog_v5.tflite", "wb") as f:
    f.write(tflite_model)

print("Model has been converted to .tflite and saved as 'trained_model_cat_and_dog_v5.tflite'")
