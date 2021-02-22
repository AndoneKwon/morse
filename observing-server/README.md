# Morse Observing Server

## How to Excute

Linux Build

```
./gradlew clean -x test build
```

Window Build

```
gradlew.bat
```

을 실행시킨다.

jar파일이 생성되면

```
java -jar 파일명
```

을 통해서 실행시킨다.



## 사용기술스택

Spring Boot 2.4 , Java 11, Redis, Kurento Media Server(WebRTC) 6.4버전



## Morse란?

WebRTC를 기술을 이용해서 1:N의 방송이 가능한 플랫폼으로 "모두의  스트리밍"의 약어이다.



## Observing Server

현재 연결 관리(연결여부)를 Websocket으로 하고있다.

유선 환경에서는 대부분 문제가 되지 않지만 무선환경에서는 WIFI가 LTE 또는 LTE가 WIFI로 바뀐 경우에는 서버가 연결이 끊긴것을 알 수 없다.

(WebRTC도 네트워크 환경이 바뀌기 때문에 당연히 작동하지 않는다.)

따라서 Observing Server를 통해서 Media Flow를 검사하다가 10초이상 끊어지면 방송이 불가능 한 상황으로 보고 해당 방송을 강제로 끊어버린다.

Endpoint Status가 6개에서 4개로 바뀌는 경우에 Flow가 들어오지 않는다고 판단한다.