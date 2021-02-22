# Morse Signaling Server

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



## 이슈사항

WebRTC는 마이크와 오디오 권한이 포함되기 때문에 웹소켓 연결시 ws가 아닌 wss를 이용해서 연결해야 한다.



## Morse란?

WebRTC를 기술을 이용해서 1:N의 방송이 가능한 플랫폼으로 "모두의  스트리밍"의 약어이다.



## WebRTC란?

WebRTC (Web Real-Time Communication)는 웹 브라우저 간에 플러그인의 도움 없이 서로 통신할 수 있도록 설계된 API이다. W3C에서 제시된 초안이며, 음성 통화, 영상 통화, P2P 파일 공유 등으로 활용될 수 있다. 간단하게 이야기 해서 웹브라우저간에 Adobe Flash나 ActiveX와 같은 별도의 플러그인 
없이 서로 통신할 수 있도록 만든 기술이며 이것을 이용해서 음성,영상 통화 등 여러가지를 할 수 있다. 여러분이 알만한 회사중에 WebRTC로 구성된 서비스는 Discord가 있다.

## Signaling Server의 역할

**일반적인 통식은 아래와 같이  서버를 통해 통신하게 된다.**
![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fbxqtuw%2FbtqQZxG4nwE%2FNVAaGFRD4TOf039jhG8DAK%2Fimg.png)

**하지만 WebRTC는 다음과 같은 순서로 작동하게 된다.**
![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FKqLMN%2FbtqQ3gLpVyZ%2FxMad7EAqWU6ZeKYREgeVZk%2Fimg.png)
![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FPXayX%2FbtqQZwnRfQc%2FqXEdveeN33Punf2keGQJXK%2Fimg.png)
![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fd6KnO6%2FbtqQZxUC5MA%2FanekQtqUmv8WCbkKujpcYK%2Fimg.png)

따라서, 이 서버는 **Peer와 Peer 간의 연결을 하기 위해 중간에서 도와주는 역할** 이라고 할 수 있다.

## Media Server

Kurento Media 서버를 사용한다.

![](https://www.kurento.org/sites/default/files/blog/kurento-1000x550.png)

Kurento Media 서버를 다운로드 받는 방법은 

이상하게 나는 도커를 이용해서 설치를 하였을때는 잘 동작되지 않아 로컬에 있는 Ubuntu 서버에 설치하였다. 쿠렌토 미디어서버의 공식 문서를 참조하여 설치하였다.

 

1. 우선 gnupg를 다운받는다.(암호화 관련)

```
sudo apt-get update && sudo apt-get install --no-install-recommends --yes \ gnupg
```

2. Kurento Repository를 서버에 설정을 한다.(반드시 저대로 잘써줘야 하며 오타가 있어서 안된다.)

```
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 5AFA7A83

# Get Ubuntu version definitions
source /etc/upstream-release/lsb-release 2>/dev/null || source /etc/lsb-release

# Add the repository to Apt
sudo tee "/etc/apt/sources.list.d/kurento.list" >/dev/null <<EOF
# Kurento Media Server - Release packages
deb [arch=amd64] http://ubuntu.openvidu.io/6.15.0 $DISTRIB_CODENAME kms6
EOF
```

3. 그 후 apt-get install을 이용하여 설치한다.

```
sudo apt-get update && sudo apt-get install --no-install-recommends --yes \
    kurento-media-server
```

 

실행은 service 명령어를 이용해서 키고 끄면 된다. 구글 스턴서버에 뭔가 문제가 있어 본인은 coturn 서버를 설치하여 STUN 설정을 별도로 해주었지만 필수는 아니기 때문에 넘어간다.



## Kurento Media Server 선정 이유

Java API를 지원하고 무엇보다 "무료"이다.



## API Doc

기본적으로 Websocket을 사용하며 Client와 핸들링 하기 위한 규칙을 정하였다.

### **Websocket 연결 URL**

wss://downsups.onstove.com:8443/call

### **Present Signaling Request**

| json object id | value            | 설명                            |
| :------------- | :--------------- | :------------------------------ |
| "id"           | "presenter"      | 방송 Open 요청                  |
| "token"        | token(String)    | 로그인 token                    |
| "title"        | title(String)    | 프레젠터가 보낸 title           |
| "contents"     | contents(String) | 방 설정                         |
| "sdpOffer"     | sdpOffer(String) | 스트리밍 미디어를 위한 sdp 요청 |

### **Present Signaling Response**

- **정상 처리 완료**

| json object id | value               | 설명                        |
| :------------- | :------------------ | :-------------------------- |
| "id"           | "presenterResponse" | presenter signaling 성공 ID |
| "response"     | "accepted"          | 성공                        |
| "sdpAnswer"    | sdpAnwser           | 처리된 sdpOffer             |



- **Token Expired Exception(토큰 Exception)**

| json object id | value             | 설명              |
| :------------- | :---------------- | :---------------- |
| "id"           | "tokenException"  | 토큰 만료 Id      |
| "response"     | "reject"          | 실패              |
| "message"      | "Token Exception" | 토큰 만료 Message |



- **유저 Exception(400 Error)**

| json object id | value             | 설명                          |
| :------------- | :---------------- | :---------------------------- |
| "id"           | "userException"   | User Exception                |
| "response"     | "reject"          | 실패                          |
| "message"      | exception message | 어떤 예외인지 나타내는 메시지 |



- **서버 Exception(500 Error)**

| json object id | value             | 설명                          |
| :------------- | :---------------- | :---------------------------- |
| "id"           | "serverException" | Server Exception              |
| "response"     | "reject"          | 실패                          |
| "message"      | exception message | 어떤 예외인지 나타내는 메시지 |



\---------------------------------------------------------------------------------------------------------------------------------------

### **Presenter Stop Request**

| json object id | value           | 설명                            |
| :------------- | :-------------- | :------------------------------ |
| "id"           | "stopPresenter" | Presenter 동영상 송출 중지 요청 |
| "token"        | token(String)   | 로그인 token                    |

### **Presenter Stop Respnse**

- **정상 처리 완료**

| json object id | value                   | 설명           |
| :------------- | :---------------------- | :------------- |
| "id"           | "stopCommunication"     | 방송 종료 안내 |
| "message"      | "방송이 종료되었습니다. | 방송 종료 멘트 |



- **비정상 처리**

  **유저 Exception(400 Error)**



| json object id | value             | 설명                          |
| :------------- | :---------------- | :---------------------------- |
| "id"           | "userException"   | User Exception                |
| "response"     | "reject"          | 실패                          |
| "message"      | exception message | 어떤 예외인지 나타내는 메시지 |



   **서버 Exception(500 Error)**

| json object id | value             | 설명                          |
| :------------- | :---------------- | :---------------------------- |
| "id"           | "serverException" | Server Exception              |
| "response"     | "reject"          | 실패                          |
| "message"      | exception message | 어떤 예외인지 나타내는 메시지 |



\---------------------------------------------------------------------------------------------------------------------------------------

### **Viewing Signaling Request**

| json object id | value        | 설명                            |
| :------------- | :----------- | :------------------------------ |
| "id"           | "viewer"     | Join 요청                       |
| "token"        | token        | 로그인 token                    |
| "presenterIdx" | presenterIdx | Presenter Index                 |
| "sdpOffer"     | sdpOffer     | 스트리밍 미디어를 위한 sdp 요청 |

### **Viewing** **Signaling Response**

- **정상 처리 완료**

| json object id | value                      | 설명                                                      |
| :------------- | :------------------------- | :-------------------------------------------------------- |
| "id"           | "viewerResponse"           | Viewing signaling 성공 ID                                 |
| "response"     | "accepted"                 | 성공                                                      |
| "sdpAnswer"    | sdpAnwser                  | 처리된 sdpOffer                                           |
| "message"      | Room Response Message 객체 | Room Server에서 넘어온 처리 완료 객체(내용은 아래와 같음) |

Room Response Message 정보

{
  "timestamp": "2021-01-30 21:46:05",
  "message": "success",
  "data": {
    "presenterIdx": 1,
    "viewerCount": 2,
    "title": "test",
    "presenterNickname": "test",
    "createdDt": "2021-01-30 21:34:11"
  }
}



- **Token Expired Exception(토큰 Exception)**

| json object id | value             | 설명              |
| :------------- | :---------------- | :---------------- |
| "id"           | "tokenException"  | 토큰 만료 Id      |
| "response"     | "reject"          | 실패              |
| "message"      | "Token Exception" | 토큰 만료 Message |



- **유저 Exception(400 Error)**

| json object id | value             | 설명                          |
| :------------- | :---------------- | :---------------------------- |
| "id"           | "userException"   | User Exception                |
| "response"     | "reject"          | 실패                          |
| "message"      | exception message | 어떤 예외인지 나타내는 메시지 |



- **서버 Exception(500 Error)**

| json object id | value             | 설명                          |
| :------------- | :---------------- | :---------------------------- |
| "id"           | "serverException" | Server Exception              |
| "response"     | "reject"          | 실패                          |
| "message"      | exception message | 어떤 예외인지 나타내는 메시지 |

\------------------------------------------------------------------------------------------------------------------------------

### **Viewer Stop Request**

| json object id | value         | 설명                                            |
| :------------- | :------------ | :---------------------------------------------- |
| "id"           | "stopViewer"  | Viewer 동영상 수신 중지 요청(with 방 나감 요청) |
| "token"        | token(String) | 로그인 token                                    |
| "presenterIdx" | presenterIdx  | presenterIdx 값                                 |

### **Viewer** **Stop Respnse**

- **정상 처리 완료**

| json object id | value                   | 설명           |
| :------------- | :---------------------- | :------------- |
| "id"           | "stopCommunication"     | 방송 종료 안내 |
| "message"      | "방송이 종료되었습니다. | 방송 종료 멘트 |



- **비정상 처리**

  **유저 Exception(400 Error)**



| json object id | value             | 설명                          |
| :------------- | :---------------- | :---------------------------- |
| "id"           | "userException"   | User Exception                |
| "response"     | "reject"          | 실패                          |
| "message"      | exception message | 어떤 예외인지 나타내는 메시지 |



   **서버 Exception(500 Error)**

| json object id | value             | 설명                          |
| :------------- | :---------------- | :---------------------------- |
| "id"           | "serverException" | Server Exception              |
| "response"     | "reject"          | 실패                          |
| "message"      | exception message | 어떤 예외인지 나타내는 메시지 |

\------------------------------------------------------------------------------------------------------------------------------

### **IceCandidate Request**

| json object id | value                          | 설명              |
| :------------- | :----------------------------- | :---------------- |
| "id"           | "iceCandidate"                 | iceCandidate 시도 |
| "token"        | token                          | 로그인 token      |
| "isPresenter"  | presenter : trueviewer : false | Presenter 여부    |





\------------------------------------------------------------------------------------------------------------------------------

### **Stop Communication(Server to Client)**

| json object id | value                    | 설명          |
| :------------- | :----------------------- | :------------ |
| "id"           | "stopCommunication"      | 방송종료 안내 |
| "message"      | "방송이 종료되었습니다." | 로그인 token  |

