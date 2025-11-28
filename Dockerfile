FROM docker:24-dind

# Install required tools
RUN apk add --no-cache openjdk17 maven nodejs npm bash

RUN apk add --no-cache py3-pip \
    && pip install docker-compose
