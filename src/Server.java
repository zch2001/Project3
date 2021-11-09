import java.io.*;
import java.net.*;


public class Server {

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
        try {
            System.out.println("\t\t\t\t\tA\t\t\t\t\t\t\t\t\tB");
            System.out.println("Round\t\tSleep time\t\tselection  point\t\t\t\tSleep time\t\tselection  point");
            ServerSocket ss = new ServerSocket(10080);
            DatagramSocket ds=new DatagramSocket(8000);
            Client_state csA=null;
            Client_state csB=null;
            Judge j=null;
            Socket s = ss.accept();
            for(int i=0;i<3;i++) {
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                csA = (Client_state) ois.readObject();


                byte []buf=new byte[1024];
                DatagramPacket ReceivePacket=new DatagramPacket(buf,buf.length);
                ds.receive(ReceivePacket);
                csB=toObject(buf);


                System.out.print("\t" + (i + 1));
                j=new Judge(csA,csB,s,ds);
                j.start();
            }
            while(j.isAlive()){}
            if (csA.getScore1() > csB.getScore2()) {
                System.out.println("A is the winner!");

            }
            if (csA.getScore1() < csB.getScore2()) {
                System.out.println("B is the winner!");

            }
            if (csA.getScore1() == csB.getScore2()) {
                System.out.println("play even!");

            }
            //输出最后比分结果


        }catch (Exception e) { e.printStackTrace(); }
    }
}

class Judge extends Thread {
    Client_state csA;
    Client_state csB;
    Socket s;
    DatagramSocket ds;

    public Judge( Client_state csA,Client_state csB,Socket s,DatagramSocket ds) {
        this.s=s;
        this.csA = csA;
        this.csB=csB;
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
    //创建函数来实现对象转为数组

    @Override
    public void run() {
        try {
            csA.setT1(false);
            csB.setT2(false);
            if (csA.getC1() == csB.getC2()) {
                csA.setScore1(1);
                csB.setScore2(1);
            } else if (csA.getC1() == 'T' && csB.getC2() == 'S') {
                csA.setScore1(2);
            } else if (csA.getC1() == 'S' && csB.getC2() == 'P') {
                csA.setScore1(2);
            } else if (csA.getC1() == 'P' && csB.getC2() == 'S') {
                csA.setScore1(2);
            } else if (csB.getC2() == 'T' && csA.getC1() == 'S') {
                csB.setScore2(2);
            } else if (csB.getC2() == 'S' && csA.getC1() == 'P') {
                csB.setScore2(2);
            } else if (csB.getC2() == 'P' && csA.getC1() == 'S') {
                csB.setScore2(2);
            }
            System.out.printf("\t\t"+"%.2f"+"s\t "+csA.getC1()+"\t\t\t"+csA.getScore1()+"\t\t\t\t"+"%.2f"+"s\t "+csB.getC2()+"\t\t\t"+csB.getScore2(),csA.gettime1()/1000,csB.gettime2()/1000);
            //将结果进行输出
            ObjectOutputStream oos=new ObjectOutputStream(s.getOutputStream());
            oos.writeObject(csA);
            //向用户发送A对象
            byte[]buf;
            buf=toByteArray(csB);
            DatagramPacket SendPacket=new DatagramPacket(buf,buf.length,InetAddress.getByName("localhost"),7000);
            ds.send(SendPacket);
            //向用户发送B对象
            System.out.println();

        } catch (Exception e) { e.printStackTrace(); }


    }
}