# mLab Data API

## Build

```
./gradlew build
```

## Usage

### Environment Variables

* `MLAB_DATA_API_KEY`: API key to authenticate all requests
* `MLAB_DATA_API_CONFIG`: Configuration string or name of file containing configuration

### Configuration

The `MLAB_DATA_API_CONFIG` environment variable must contain a string with the configuration
settings or the name of a file containing said configuration.  The configuration may be
specified in either YAML or JSON format.  Most users may find that YAML is more convenient
when using a file and JSON is more convenient when specifying the configuration inline.

Example (YAML):
```
port: 8080
clusters:
  rs-ds0000000: mongodb://user:pass@ds0000000.mlab.com:27001/admin
  rs-ds1111111: mongodb://user:pass@ds1111111.mlab.com:27001/admin
databases:
  foo: mongodb://user:pass@ds2222222.mlab.com:27001/foo
  bar: mongodb://user:pass@ds3333333.mlab.com:27001/bar
```

Example (JSON):
```
{
  port: 8080,
  clusters: {
    "rs-ds0000000": "mongodb://user:pass@ds0000000.mlab.com:27001/admin",
    "rs-ds1111111": "mongodb://user:pass@ds1111111.mlab.com:27001/admin"
  },
  databases: {
    foo: "mongodb://user:pass@ds2222222.mlab.com:27001/foo",
    bar: "mongodb://user:pass@ds3333333.mlab.com:27001/bar"
  }
}
```

### Run
```
./build/install/mlab-data-api/bin/mlab-data-api
```

## Docker

### Build

```
docker build .
```

### Run

```
docker run -e MLAB_DATA_API_CONFIG='{port: [PORT], clusters: [...]}' -e MLAB_DATA_API_KEY=[KEY] -p [PORT]:[PORT] [IMAGE]
```
