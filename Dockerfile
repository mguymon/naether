FROM ruby:3.0

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y \
     openjdk-17-jdk && \
    apt-get -y clean && rm -rf /var/lib/apt

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

RUN mkdir /app

WORKDIR /app

COPY Gemfile* /app/

RUN  bundle install

COPY . /app/
