FROM golang:1.20.4-bullseye

ADD . /app/authentication

WORKDIR /app/authentication

RUN go mod download && go mod verify

RUN go get github.com/dgrijalva/jwt-go
RUN go build -o authentication *.go
RUN chmod +x /app/authentication/authentication

cmd ["/app/authentication/authentication"]
