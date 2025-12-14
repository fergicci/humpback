BACKEND_DIR := backend
FRONTEND_DIR := frontend
BACKEND_IMAGE_NAME := humpback-backend
FRONTEND_IMAGE_NAME := humpback-frontend
UID := $(shell id -u)
GID := $(shell id -g)

.PHONY: all clean compile test build run stop restart image \
        backend-clean frontend-clean \
        backend-compile frontend-compile \
        backend-test frontend-test \
        backend-build frontend-build \
        backend-image frontend-image

all: clean compile test build run

backend-clean:
	cd $(BACKEND_DIR) && ./gradlew clean

frontend-clean:
	cd $(FRONTEND_DIR) && rm -rf node_modules dist build

clean: backend-clean frontend-clean

backend-compile:
	cd $(BACKEND_DIR) && ./gradlew compileJava

frontend-compile:
	cd $(FRONTEND_DIR) && npm install

compile: backend-compile frontend-compile

backend-test:
	cd $(BACKEND_DIR) && ./gradlew test

frontend-test:
	cd $(FRONTEND_DIR) && npm test

test: backend-test frontend-test

backend-build:
	cd $(BACKEND_DIR) && ./gradlew build

frontend-build: frontend-compile
	cd $(FRONTEND_DIR) && npm run build

build: backend-build frontend-build

run:
	docker compose up -d

stop:
	docker compose down --remove-orphans

restart: stop run

backend-image:
	cd $(BACKEND_DIR) && docker build --pull --rm -t $(BACKEND_IMAGE_NAME):latest .

frontend-image:
	cd $(FRONTEND_DIR) && docker build --pull --rm -t $(FRONTEND_IMAGE_NAME):latest .

image: backend-image frontend-image