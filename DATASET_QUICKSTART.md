# 📦 Dataset Setup - Quick Reference

## Google Drive Link
🔗 https://drive.google.com/drive/folders/1l3NG641rHeshkYW7aDxpb7RhUy0kRuiP

## Three Ways to Download

### 1️⃣ Automatic (Python)
```bash
python3 download_dataset.py
```
- Interactive installer
- Auto-installs gdown if needed
- Shows progress and file sizes

### 2️⃣ Semi-Automatic (Bash)
```bash
./download_dataset.sh
```
- Requires gdown pre-installed
- Downloads entire folder
- Linux/Mac only

### 3️⃣ Manual
1. Visit the Google Drive link above
2. Download all files
3. Place in `dataset/` folder

## What Happens
- Application checks for files on startup
- Shows helpful instructions if missing
- Auto-downloads if `gdown` available
- Lists missing files with ✓/✗ status

## Files Needed
- `nodes_264346.txt` - Node data
- `edges_264346.txt` - Edge connections
- Optional: `graph_264346.txt`, `*_old.txt` variants

## Troubleshooting

**Install gdown:**
```bash
pip install gdown
```

**Make scripts executable:**
```bash
chmod +x download_dataset.sh download_dataset.py
```

**Check dataset folder:**
```bash
ls -lh dataset/
```

## Git Status
✅ Dataset files are **automatically ignored** by git  
✅ Won't be committed to repository  
✅ Safe to download large files locally  

## Documentation
- 📚 [Full Setup Guide](DATASET_SETUP.md)
- 📚 [Dataset README](dataset/README.md)
- 📚 [Main README](README.md)

---
**Quick Test**: `java -cp build DatasetDownloader`
