

import socket

address=('127.0.0.1',3333)
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(address)
s.send('hhah')
data=s.recv(1024)
print data


s.close