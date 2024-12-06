# Online Text Storage using Spring Boot and GridDB

> **Connect** with me through [Upwork](https://www.upwork.com/freelancers/~018d8a1d9dcab5ac61), [LinkedIn](https://linkedin.com/in/alifruliarso), [Email](mailto:alif.ruliarso@gmail.com), [Twitter](https://twitter.com/alifruliarso)

## Technology Stack
Spring Boot, Docker, Thymeleaf, Maven\
Database: GridDB 5.5.0


## Run Application with Docker Compose

Build the docker image: 
```shell
docker compose -f docker-compose.yml build
```

Run the docker image: 

```shell
docker compose -f docker-compose.yml up -d
```

Check container

```shell
docker ps -a

docker compose logs --follow

docker compose logs > container.log
```

The website available at http://localhost:8080


### For development supporting auto-reload

**Prerequisites**:

- [Eclipse Temurin](https://adoptium.net/temurin/releases/)
- [Docker](https://docs.docker.com/engine/install/)

**Format code**
  ```shell
  mvn spotless:apply
   ```

### GridDB Operations
- Exec into docker container
  ```shell
  $ su gsadm
  $ gs_sh
  gs> setcluster clusterD dockerGridDB 239.0.0.1 31999 $node0
  gs> connect $clusterD
  ```
