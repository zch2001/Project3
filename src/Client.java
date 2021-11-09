import java.io.*;
import java.net.*;

class Client_state implements Serializable{
    private boolean t1 =false;
    private boolean t2 =false;
    //创建表示A,B是否生成的全局变量
    private char c1='0';
    private char c2='0';
    //创建A,B生成的字符变量（S为剪刀，P为布，T为石头）
    private static int score1 = 0;
    private static int score2 = 0;
    //创建两个int变量代表A,B的分数
    private double sleeptimeA=0;
    private double sleeptimeB=0;
    //创建两个int变量代表A,B的睡眠时间
    public boolean isT1() { return t1; }
    public boolean isT2() { return t2; }
    public void setT1(boolean t1) { this.t1 = t1; }
    public void setT2(boolean t2) { this.t2 = t2; }
    public char getC1() { return c1; }
    public char getC2() { return c2; }
    public void setC1(char c1) { this.c1 = c1; }
    public void setC2(char c2) { this.c2 = c2; }
    public int getScore1() { return score1; }
    public int getScore2() { return score2; }
    public void setScore1(int score1) { this.score1 += score1; }
    public void setScore2(int score2) { this.score2 += score2; }
    public double gettime1() { return sleeptimeA; }
    public double gettime2() { return sleeptimeB; }
    public void settime1(double time) { sleeptimeA= time; }
    public void settime2(double time) { sleeptimeB= time; }
    //创建所有变量的getter和setter函数
}


public class Client {

    //创建函数来实现对象转为数组
    public static Client_state toObject (byte[] bytes) {
        Client_state obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
            ObjectInputStream ois = new ObjectInputStream (bis);
            obj = (Client_state) ois.readObject();
            ois.close();
            bis.close();
        } catch (Exception ex) { ex.printStackTrace(); }
        return obj;
    }
    //创建函数实现数组转为对象

    public static void main(String[] args){
        Client_state csA=new Client_state();
        Client_state csB=new Client_state();
        try {
            Socket socket = new Socket("localhost",10080);
            DatagramSocket ds=new DatagramSocket(7000,InetAddress.getByName("localhost"));
            //创建socket
            A a;
            B b;
            //创建线程A,B的对象
            for(int i=0;i<3;i++){
                if(i>0) {
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    csA = (Client_state) ois.readObject();
                    byte[] buf=new byte[1024];
                    DatagramPacket ReceivePacket=new DatagramPacket(buf,buf.length);
                    ds.receive(ReceivePacket);
                    csB=toObject(buf);
                }
                a=new A(csA,socket);
                b=new B(csB,ds);
                //初始化Client_state,A,B的对象
                a.start();
                b.start();
                //启动进程
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
class A extends Thread{
    int tempA=0;
    Client_state cs;
    Socket sA;

    public A(Client_state c,Socket s){
        sA=s;
        cs=c;
    }
    public void run(){

        try {
            synchronized (cs) {
                while(cs.isT1()) {
                    cs.wait();
                }
                //判断当前线程是否已生成字母(石头剪刀布），若已生成则等待
                double sleeptime=Math.random() * 1000;
                Thread.sleep((long)sleeptime);
                cs.settime1(sleeptime);
                //随机睡眠
                tempA= (int) (Math.random() * 3);
                if(tempA==0){cs.setC1('T');}
                if(tempA==1){cs.setC1('S');}
                if(tempA==2){cs.setC1('P');}
                //在石头剪刀布中随机生成一个

                if(cs.getC1()=='T'||cs.getC1()=='S'||cs.getC1()=='P'){
                    cs.setT1(true);
                    cs.notifyAll();
                }
                //判断是否已经生成，若生成则标志设为true,发送给服务器，并进行下一步

                ObjectOutputStream oos=new ObjectOutputStream(sA.getOutputStream());
                oos.writeObject(cs);
                //向服务器发送对象(TCP)
            }
        } catch (Exception e) { e.printStackTrace(); }

    }


}

class B extends Thread {
    int tempB = 0;
    Client_state cs;
    DatagramSocket ds;
    public B(Client_state c,DatagramSocket ds) {
        cs = c;
        this.ds=ds;
    }
    public static byte[] toByteArray (Client_state obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray ();
            oos.close();
            bos.close();
        } catch (IOException ex) { ex.printStackTrace(); }
        return bytes;
    }
    public void run() {

        try {
            //创建输出流用于向服务器发送数据(UDP)
            synchronized (cs) {
                while (cs.isT2()) { cs.wait();}
                //判断当前线程是否已生成字母(石头剪刀布），若已生成则等待}
                double sleeptime=Math.random() * 1000;
                Thread.sleep((long)sleeptime);
                cs.settime2(sleeptime);
                //随机睡眠
                tempB = (int) (Math.random() * 3);
                if (tempB == 0) { cs.setC2('T'); }
                if (tempB == 1) { cs.setC2('S'); }
                if (tempB == 2) { cs.setC2('P'); }
                //在石头剪刀布中随机生成一个

                if (cs.getC2() == 'T' || cs.getC2() == 'S' || cs.getC2() == 'P') {
                    cs.setT2(true);
                    cs.notifyAll();
                }
                //判断是否已经生成，若生成则标志设为true,并进行下一步
                byte[]buf;
                buf=toByteArray(cs);
                DatagramPacket SendPacket=new DatagramPacket(buf,buf.length,InetAddress.getByName("localhost"),8000);
                ds.send(SendPacket);
                //向服务器发送对象(UDP)
            }
        } catch (Exception e) { e.printStackTrace(); }



    }
}