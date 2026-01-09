# TnG_CBSE
## **How to start**
## 1. Clone the repository and switch to your branch
git clone <your-repo-url>

git checkout <your-branch-name>

cd ewallet


## 2. Start MongoDB
### Mac (Homebrew):

brew tap mongodb/brew

brew install mongodb-community

brew services start mongodb-community


Check status:

brew services list

### Windows:

Download and install MongoDB Community Server from:
https://www.mongodb.com/try/download/community

During installation, optionally select “Install MongoDB as a Service” for automatic startup.

Manual start (if not installed as a service):

mkdir C:\data\db       # create data folder if it doesn't exist
"C:\Program Files\MongoDB\Server\<version>\bin\mongod.exe"


Keep this terminal open — MongoDB will run on localhost:27017.

## 3. Run the Spring Boot application

Using Maven:

mvn spring-boot:run
