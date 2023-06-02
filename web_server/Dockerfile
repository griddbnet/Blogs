FROM golang:1.20.4-bullseye


RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends autotools-dev automake libpcre3 libpcre3-dev
		

WORKDIR /
RUN wget --no-check-certificate https://github.com/griddb/c_client/releases/download/v5.1.0/griddb-c-client_5.1.0_amd64.deb
RUN dpkg -i griddb-c-client_5.1.0_amd64.deb

RUN wget https://prdownloads.sourceforge.net/swig/swig-4.0.2.tar.gz
RUN tar xvzf swig-4.0.2.tar.gz



WORKDIR /swig-4.0.2
RUN /swig-4.0.2/autogen.sh
RUN /swig-4.0.2/configure
RUN make
RUN make install

RUN go env -w GO111MODULE=off
RUN go get -d github.com/griddb/go_client
WORKDIR $GOPATH/src/github.com/griddb/go_client
RUN ls -la
RUN ./run_swig.sh
RUN go install


workdir /app

ADD . /app/web_server
ADD ./frontend /app/frontend


RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && apt-get install -y nodejs
WORKDIR /app/frontend
RUN npm install
RUN npm install react-scripts@3.4.1 -g --silent
RUN npm run pre-build
RUN npm run build
RUN npm run post-build

WORKDIR /app/web_server
RUN go get github.com/dgrijalva/jwt-go
RUN go get github.com/gorilla/mux
RUN go build -o web_server *.go 
RUN chmod +x /app/web_server/web_server

EXPOSE 8000

cmd ["/app/web_server/web_server"]
