# Contributing to FlexiRoute Navigator

Thank you for your interest in contributing to FlexiRoute Navigator! 🎉

## Getting Started

### Prerequisites
- Java 21 LTS or higher
- Maven 3.6+
- Node.js 18+ (for frontend development)

### Setting Up the Development Environment

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/FlexiRoute.git
   cd FlexiRoute
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   ./run.sh  # Linux/Mac
   run.bat   # Windows
   ```

## Project Structure

```
FlexiRoute/
├── src/                    # Java source files
│   ├── GuiLauncher.java   # Main GUI application
│   ├── ApiServer.java     # REST API server
│   ├── models/            # Data models
│   ├── managers/          # Business logic
│   └── ui/                # UI components
├── frontend/              # React + Vite web UI
├── dataset/               # Graph data files (gitignored)
└── docs/                  # Documentation
```

## Code Style Guidelines

### Java
- Use 4 spaces for indentation
- Follow standard Java naming conventions
- Add Javadoc comments for public methods
- Keep methods focused and under 50 lines when possible

### Frontend (TypeScript/React)
- Use 2 spaces for indentation
- Use functional components with hooks
- Follow React best practices

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
