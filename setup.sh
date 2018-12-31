#Instrukcja
#!!! Nie wrzucac na repo folderu floodlight-v1.2/target bo repo ograniczone do 100mb
#!!! Z folderu floodlight nie sa wrzucone:  oflops  oftest  openflow  pox
#!!! bo zajmowaly za duzo miejsca, nie wiem czy to potrzebne do czegos
#to co wykomentowane to juz nie trza robic bo jak sciagniecie repo to bedzie

#install mininet
#git clone git://github.com/mininet/mininet
./mininet/util/install.sh

#install ant
sudo apt-get install ant

#install java7 JDK7!!!
tu instrukcja:
https://www3.ntu.edu.sg/home/ehchua/programming/howto/JDK_Howto.html
robic od "Step 1: Download and Install JDK"
JDK sciagnijcie z http://monalisa.cern.ch/MONALISA/download/java/ o nazwie jdk-7u80-linux-x64.tar.gz

#install floodlight
#mkdir floodlight
#cd floodlight
#wget https://github.com/floodlight/floodlight/archive/v1.2.tar.gz 
#tar -xf v1.2.tar.gz

#build floodlight
cd floodlight/floodlight-1.2
ant

#Launching FL
cd floodlight/floodlight-1.2/
java -jar target/floodlight.jar
127.0.0.1:8080/ui/index.html #need to be refreshed after topology change

#Launching MN
sudo mn --custom topology.py --topo mytopo --controller=remote,ip=127.0.0.1,port=6653



