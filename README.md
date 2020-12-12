## Instructions for compiling and running
--------------------------------------

1. Make sure your current working directory is "Battleships".

2. Compile by running:
```
        javac @sources.txt
```
3. Start the server by running:
```
        java -classpath src server.Server
```
4. In two or more other terminals, open two or more clients by running:
```
        java -classpath src view.MatchRoomView
```

## Configuration
-------------

You may change the hostname and port that the server binds to and the client connects to by editing config.properties.

## Client
- **Model/Client.java:**

  + **Feature**:

| Feature |  Details |
|---|---|
| ownBoard | bang cua client|
| opponentBoard | bang cua doi thu |
| out, in | goi tin chuyen sang Server va nhan tu Server |
| clientView | Giao dien cua Client |

  + **Function**

| Function | Details |
|---|---|
| Run | Doi input tu Server va phan tich input, thuc hien cau lenh tuong ung |
| parseInput | Phan tich input nhan tu Server |
| sendBoard | Gui board cua client len Server |
|sendMove | Gui toa do ban tren board cua doi thu |
| writeObject | void():  Ghi mot object vao mot outputStream



## Server
- Message: Cac thong tin ve message:
-
## Cac chuc nang:
- Login vao Game
- Chon nguoi choi
- Xep tau
- Choi
- Chat
