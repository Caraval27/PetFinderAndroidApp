import os
from PIL import Image, UnidentifiedImageError

def clean_invalid_images(directory):
    for subdir, dirs, files in os.walk(directory):
        for file in files:
            file_path = os.path.join(subdir, file)
            try:
                with Image.open(file_path) as img:
                    img.verify()  # Verifies if it's a valid image
            except (UnidentifiedImageError, IOError):
                print(f"Deleting invalid image file: {file_path}")
                os.remove(file_path)

# Path to your dog_photos dataset
data_dir = "cat_and_dog_photos"
clean_invalid_images(data_dir)
