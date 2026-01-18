# Contributing to FlexiRoute Navigator

Thank you for your interest in contributing to FlexiRoute Navigator! 🎉

## Getting Started

### Prerequisites
- Java 21 LTS or higher
- Maven 3.6+

### Setting Up the Development Environment

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/FlexiRoute.git
   cd FlexiRoute
   ```

2. **Download the dataset**
   - Download from [Google Drive](https://drive.google.com/drive/folders/1l3NG641rHeshkYW7aDxpb7RhUy0kRuiP)
   - Extract the `London/` folder to `dataset/London/`

3. **Build the project**
   ```bash
   mvn clean compile
   ```

4. **Run the application**
   ```bash
   ./run.sh  # Linux/Mac
   run.bat   # Windows
   ```

## Project Structure

```
FlexiRoute/
├── src/                    # Java source files
│   ├── GuiLauncher.java   # Main GUI application
│   ├── models/            # Data models
│   ├── managers/          # Business logic
│   └── ui/                # UI components
├── dataset/               # Graph data files (gitignored)
└── docs/                  # Documentation
```

## Code Style Guidelines

### Java
- Use 4 spaces for indentation
- Follow standard Java naming conventions
- Add Javadoc comments for public methods
- Keep methods focused and under 50 lines when possible

## Submitting Changes

1. **Create a branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** and commit with clear messages
   ```bash
   git commit -m "Add: brief description of your change"
   ```

3. **Push and create a Pull Request**
   ```bash
   git push origin feature/your-feature-name
   ```

## Reporting Issues

When reporting issues, please include:
- Clear description of the problem
- Steps to reproduce
- Expected vs actual behavior
- Java version and OS

## Questions?

Feel free to open an issue for any questions about contributing!
