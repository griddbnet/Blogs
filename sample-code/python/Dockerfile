# 1. Start from a lightweight Python base image
FROM python:3.12-slim

# 2. Set the working directory inside the container
WORKDIR /app

# 3. Install build dependencies: Java, Git, and Maven
RUN apt-get update && \
    apt-get install -y default-jre-headless git maven && \
    rm -rf /var/lib/apt/lists/*

# 4. Clone, build, and install the griddb_python client
#    This will ALSO install its dependencies (jpype1, pyarrow)
RUN git clone https://github.com/griddb/python_client.git && \
    cd python_client/java && \
    mvn install && \
    cp target/gridstore-arrow-*.jar /app/gridstore-arrow.jar && \
    cd ../python && \
    pip install . && \
    \
    # Clean up the build files
    cd /app && \
    rm -rf python_client

# 5. Copy your local JARs and Python code
COPY gridstore.jar .
COPY arrow-memory-netty.jar .
COPY *.py .

# 6. Set the default command
CMD ["python", "main.py"]
