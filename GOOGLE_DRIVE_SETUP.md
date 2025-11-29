# Google Drive Dataset Auto-Download Setup

## Overview
The Wide-Path GUI now includes automatic dataset download from Google Drive. If the dataset files (`nodes` and `edges`) are not found when launching the application, you'll be prompted to download them.

## Configuration

### First Time Setup
1. Launch the GUI application
2. When prompted "Dataset files not found. Would you like to download them from Google Drive?", click **Yes**
3. If file IDs are not configured, you'll be prompted to configure them
4. Enter the Google Drive file IDs for your datasets:
   - **Nodes File ID**: The Google Drive file ID for the nodes dataset
   - **Edges File ID**: The Google Drive file ID for the edges dataset
   - **Dataset Directory**: The directory where datasets should be stored (default: current directory)

### Getting Google Drive File IDs
To get the file ID from a Google Drive shareable link:
1. Right-click the file in Google Drive and select "Get link"
2. Copy the link (format: `https://drive.google.com/file/d/FILE_ID/view?usp=sharing`)
3. Extract the `FILE_ID` portion from the URL

Example:
- Link: `https://drive.google.com/file/d/1abc123XYZ456/view?usp=sharing`
- File ID: `1abc123XYZ456`

### Manual Configuration
You can also create a `drive_config.properties` file manually:

```properties
drive.nodes.fileId=YOUR_NODES_FILE_ID
drive.edges.fileId=YOUR_EDGES_FILE_ID
dataset.directory=.
```

Place this file in the same directory as the application.

## Usage

### Automatic Download on Startup
- When you launch `GuiLauncher`, it will check for dataset files
- If not found, you'll be prompted to download them
- The download progress will be displayed
- Once complete, the application will load the graph and continue normally

### Manual Download Trigger
If you want to re-download or update datasets:
1. Delete the existing `nodes` and `edges` files
2. Restart the application
3. Click "Yes" when prompted to download

## Dataset Location
By default, datasets are stored in the current working directory. You can configure a different location:
- Update the `dataset.directory` property in `drive_config.properties`
- Or specify it in the configuration dialog when prompted

## Troubleshooting

### Download Fails
- Ensure the Google Drive file IDs are correct
- Check that the files are publicly accessible (Anyone with the link can view)
- Verify your internet connection

### Files Still Not Found After Download
- Check the `dataset.directory` configuration
- Ensure the download completed successfully (check file sizes)
- Verify the application is looking in the correct directory

## Integration Details
The auto-download feature integrates seamlessly with the existing graph loading mechanism:
1. `GuiLauncher.start()` checks if dataset exists using `GoogleDriveConfigHelper.checkDatasetExists()`
2. If not found, prompts user to download via `GoogleDriveDatasetLoader.downloadDataset()`
3. After successful download, proceeds with `BidirectionalAstar.loadGraphFromDisk()`
4. If download is cancelled or fails, application exits gracefully
