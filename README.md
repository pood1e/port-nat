# port-nat
端口转发工具，内网穿透端口到公网服务器；隧道可配置加密；

## requirement

java 18

## usage

### server

1. 修改配置文件放置在启动目录下

`application.yml `

```yaml
proxy:
  preset:
    proxies:
      # max tunnel count
      - max-connection: 25
        # listen port
        port: 12002
        # listen server auth
        auth: true
        # required if auth enable
        pass: 123456
        # proxy port infos
        out-ports:
          # proxy port : bind on 127.0.0.1
          "12003": false
        # tunnel ssl protect
        ssl-enable: true
        # tunnel aes encrypt
        aes-enable: false
        # aes key string, required if aes enable
        aes-key:
        # tunnel rsa encrypt
        rsa-enable: false
        # server private key string, required if rsa enable
        rsa-pri-key:
        # client public key string, required if rsa enable
        rsa-pub-key:
```

2. 启动

```shell
nohup java -jar port-nat-server-1.0-SNAPSHOT.jar > log 2>&1 &
```

### client

1. 修改配置文件放置在启动目录下

`application.yml`

```yaml
proxy:
  clients:
    # server address
    - address: 127.0.0.1
      # server port
      port: 12002
      # listen server auth
      auth: true
      # required if auth enable
      pass: 123456
      # reconnect delay
      retry-delay: 5000
      # connection count
      max-connection: 5
      # proxy inner
      out-mapper:
        # out port
        "12003":
          # local network address
          address: 127.0.0.1
          # local network port
          port: 16305
      # tunnel ssl protect
      ssl-enable: true
      # tunnel aes protect
      aes-enable: false
      aes-key:
      # tunnel rsa protect
      rsa-enable: false
      # server public key string, required if rsa enable
      rsa-pub-key:
      # client private key string, required if rsa enable
      rsa-pri-key:
```

2. 启动

```
nohup java -jar port-nat-client-1.0-SNAPSHOT.jar > log 2>&1 &
```

