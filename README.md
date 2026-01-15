# TnG_CBSE

## How to Start (Run Locally)

### 1. Clone the Repository and Switch to Your Branch

```bash
git clone <your-repo-url>
cd TnG_CBSE
git checkout <your-branch-name>
cd ewallet
```

### 2. Start MongoDB

The application requires MongoDB running at:

```
mongodb://localhost:27017
```

#### macOS (Homebrew)

```bash
brew tap mongodb/brew
brew install mongodb-community
brew services start mongodb-community
```

Check MongoDB status:

```bash
brew services list
```

#### Windows

1. Download MongoDB Community Server:
   [https://www.mongodb.com/try/download/community](https://www.mongodb.com/try/download/community)

2. During installation, you may select:
   **“Install MongoDB as a Service”** (recommended)

##### Manual Start (if not installed as a service)

```bash
mkdir C:\data\db
"C:\Program Files\MongoDB\Server\<version>\bin\mongod.exe"
```

> Keep this terminal open. MongoDB will run at `localhost:27017`.

### 3. Run the Spring Boot Application

From the project root:

```bash
cd ewallet
mvn spring-boot:run
```

Once started, the application should be running on:

```
http://localhost:8080
```
