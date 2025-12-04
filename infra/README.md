---
# Jenkins Setup for Local Development (Docker)

This guide explains how to run **Jenkins locally** using the configuration inside the `infra/` folder.

---

## 1. Requirements

Make sure the following are installed:

* **Docker** (latest)
* **Docker Compose** (v2+)
* **Git**

Verify installation:

```sh
docker --version
docker compose version
git --version
```

---

## 2. Project Structure

```
buy-01-git/
│
├── app/                 # Backend + frontend source code
├── infra/
│   ├── Dockerfile       # Custom Jenkins image (Docker, Maven, Node included)
│   │── docker-compose.yml
│   └── README.md
```

---

## 3. Build the Jenkins Image

Navigate to the jenkins folder inside the `infra/` folder:

```sh
cd infra
cd jenkins
```

Build the custom Jenkins image:

```sh
docker build -t jenkins .
```

This image includes **Docker CLI**, **Compose plugin**, **Maven**, and **Node**.

---

## 4. Start Jenkins

Still inside the `infra/jenkins/` directory:

```sh
docker compose up -d
```

Jenkins will be available at:

👉 **[http://localhost:8085](http://localhost:8085)**

---

## 5. First-Time Login

1. Open your browser and go to:

   👉 **[http://localhost:8085](http://localhost:8085)**

2. Jenkins will ask for the initial admin password. Retrieve it:

   ```sh
   docker exec -it jenkins cat /var/jenkins_home/secrets/initialAdminPassword
   ```

3. Paste the password into Jenkins.

4. Choose **"Install suggested plugins"**.

5. Create your admin user.

---

## 6. Enable Docker Inside Jenkins

The repo already mounts Docker:

```
/var/run/docker.sock:/var/run/docker.sock
```

This lets Jenkins run Docker commands directly.

To verify:

* Go to **Manage Jenkins → Tools → Docker**
* Jenkins should detect Docker automatically.

Create a test pipeline stage:

```sh
docker ps
```

If you see output, Docker is working.

---

## 7. Connect Jenkins to GitHub

Create a job:

1. Dashboard → **New Item**
2. Select **Pipeline**
3. Set:
    * **Triggers** → `Poll SCM` with `H/5 * * * *` (every 5 minutes) or H/2 * * * * (every 2 minutes) as a schedule. 

    * **Pipeline → Definition** → `Pipeline script from SCM`
    * **SCM** → `Git`
    * **Repository URL** → your repo URL (`https://github.com/Linnie43/buy-01-git`)
    * **Branches to build** → `*/dev` or your desired branch
    * **Script Path** → `Jenkinsfile`
    * Save the job.

---

## 8. Jenkinsfile Required in Your Repo

Place this in `app/` or the project root:

```groovy
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Build backend') {
            steps {
                sh 'mvn -B clean package'
            }
        }

        stage('Build frontend') {
            steps {
                sh 'npm install'
                sh 'npm run build'
            }
        }
    }
}
```

Modify as needed.

---

## 9. Restart Jenkins

```sh
docker compose restart
```

---

## 10. Stop Jenkins

```sh
docker compose down
```

---

## 11. Reset Everything (Deletes All Jenkins Data)

```sh
docker compose down -v
```

---
