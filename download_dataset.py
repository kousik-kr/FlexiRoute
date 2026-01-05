#!/usr/bin/env python3
"""
FlexiRoute Dataset Downloader (Python)
Alternative downloader for users who prefer Python
"""

import os
import sys
import subprocess

DRIVE_FOLDER_ID = "1l3NG641rHeshkYW7aDxpb7RhUy0kRuiP"
DRIVE_URL = f"https://drive.google.com/drive/folders/{DRIVE_FOLDER_ID}"
DATASET_DIR = "dataset"

def check_gdown():
    """Check if gdown is installed"""
    try:
        subprocess.run(["gdown", "--version"], 
                      stdout=subprocess.DEVNULL, 
                      stderr=subprocess.DEVNULL, 
                      check=True)
        return True
    except (subprocess.CalledProcessError, FileNotFoundError):
        return False

def install_gdown():
    """Attempt to install gdown"""
    print("Installing gdown...")
    try:
        subprocess.run([sys.executable, "-m", "pip", "install", "gdown"], check=True)
        print("✓ gdown installed successfully!")
        return True
    except subprocess.CalledProcessError:
        print("✗ Failed to install gdown")
        print("\nPlease install manually:")
        print("  pip install gdown")
        print("  or")
        print("  pip3 install gdown")
        return False

def download_dataset():
    """Download dataset from Google Drive"""
    os.makedirs(DATASET_DIR, exist_ok=True)
    
    print(f"\nDownloading dataset from Google Drive...")
    print(f"This may take several minutes...\n")
    
    try:
        subprocess.run([
            "gdown",
            "--folder",
            DRIVE_URL,
            "-O",
            DATASET_DIR,
            "--remaining-ok"
        ], check=True)
        
        print("\n✓ Download completed successfully!")
        list_dataset_files()
        return True
        
    except subprocess.CalledProcessError as e:
        print(f"\n✗ Download failed: {e}")
        print("\nPlease try downloading manually from:")
        print(f"  {DRIVE_URL}")
        return False

def list_dataset_files():
    """List all files in the dataset directory"""
    if not os.path.exists(DATASET_DIR):
        print(f"\n✗ Dataset directory not found: {DATASET_DIR}")
        return
    
    files = [f for f in os.listdir(DATASET_DIR) 
             if os.path.isfile(os.path.join(DATASET_DIR, f))]
    
    if not files:
        print(f"\n✗ No files found in {DATASET_DIR}")
        return
    
    print(f"\n✓ Files in {DATASET_DIR}:")
    for filename in sorted(files):
        filepath = os.path.join(DATASET_DIR, filename)
        size = os.path.getsize(filepath)
        size_str = format_size(size)
        print(f"  - {filename} ({size_str})")

def format_size(bytes):
    """Format file size for display"""
    for unit in ['B', 'KB', 'MB', 'GB']:
        if bytes < 1024.0:
            return f"{bytes:.1f} {unit}"
        bytes /= 1024.0
    return f"{bytes:.1f} TB"

def main():
    print("=" * 60)
    print("FlexiRoute Dataset Downloader (Python)")
    print("=" * 60)
    
    print(f"\nDataset URL: {DRIVE_URL}")
    print(f"Target directory: {os.path.abspath(DATASET_DIR)}\n")
    
    # Check if gdown is installed
    if not check_gdown():
        print("✗ gdown is not installed")
        response = input("\nWould you like to install it now? (y/n): ").lower()
        
        if response == 'y':
            if not install_gdown():
                sys.exit(1)
        else:
            print("\nPlease install gdown manually:")
            print("  pip install gdown")
            print("\nThen run this script again.")
            sys.exit(1)
    else:
        print("✓ gdown is installed")
    
    # Download dataset
    print("\n" + "-" * 60)
    download_dataset()
    print("-" * 60)
    
    print(f"\nDataset location: {os.path.abspath(DATASET_DIR)}")
    print("\nYou can now run the FlexiRoute application!")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n✗ Download cancelled by user")
        sys.exit(1)
