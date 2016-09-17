#coding=utf-8
import socket
import threading
from time import sleep

def response(sock, addr):
    print "收到请求"
    data = sock.recv(1024)
    print (data[::-1])
    sock.send(html)
    sock.close()

html = '''HTTP/1.1 200 OK\nContent-Type: text/html\n\r\nHello world!'''
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind(('127.0.0.1', 3333))
s.listen(50)
print "正在等待连接……"
while 1:
    sleep(100)
    sock,addr = s.accept()
    t = threading.Thread(target=response, args=(sock,addr))
    t.start()