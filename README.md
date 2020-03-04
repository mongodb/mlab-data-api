# mLab Data API

## Build

```
./gradlew build
```

## Usage

```
./gradlew run
```

## Docker

### Build

```
docker build .
```

### Run

```
docker run -e MLAB_DATA_API_CONFIG='{port: [PORT], clusters: [...]}' -e MLAB_DATA_API_KEY=[KEY] [IMAGE]
```
