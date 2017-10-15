import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TrajectoryCompress {
    static FileReader fr=null;
    static FileWriter fw=null;
    static BufferedWriter bw=null;
    int count;
    int ii=0;
    double a,b;
    int a1[]=new int[300];
    static List<Node> list;
    static List<Node> compresslist;
    public static double distance(double x1,double x2,double y1,double y2,int i)
    {
    	double x=Rad(Math.abs(list.get(i).getLat()-x1));
        double distancex1=x*6378137.0;
    	double y=Rad(Math.abs(list.get(i).getLon()-y1));
    	double distancey1=y*6378137.0;
    	double distancea=Math.sqrt(distancex1*distancex1+distancey1*distancey1);
    	double distancex2=Rad(Math.abs(list.get(i).getLat()-x2))*6378137.0;
    	double distancey2=Rad(Math.abs(list.get(i).getLon()-y2))*6378137.0;
    	double distanceb=Math.sqrt(distancex2*distancex2+distancey2*distancey2);
    	double distancex3=Rad(Math.abs(x1-x2))*6378137.0;
    	double distancey3=Rad(Math.abs(y1-y2))*6378137.0;
    	double distancec=Math.sqrt(distancex3*distancex3+distancey3*distancey3);
    	/*double distancea=Geodist(x1,y1,list.get(i).getLat(),list.get(i).getLon());
    	double x3=list.get(i).getLat();
    	double x4=list.get(i).getLon();
    	double distanceb=Geodist(x2,y2,list.get(i).getLat(),list.get(i).getLon());
    	double distancec=Geodist(x1,y1,x2,y2);*/
    	double p=(distancea+distanceb+distancec)/2;
    	double s=Math.sqrt(p*(p-distancea)*(p-distanceb)*(p-distancec));
    	return (2*s)/distancec;
    }
    public static double Geodist(double lat1, double lon1, double lat2, double lon2)
    {
        double radLat1 = Rad(lat1);
        double radLat2 = Rad(lat2);
        double delta_lon = Rad(lon2 - lon1);
        double top_1 = Math.cos(radLat2) * Math.sin(delta_lon);
        double top_2 = Math.cos(radLat1) * Math.sin(radLat2) - Math.sin(radLat1) * Math.cos(radLat2) * Math.cos(delta_lon);
        double top = Math.sqrt(top_1 * top_1 + top_2 * top_2);
        double bottom = Math.sin(radLat1) * Math.sin(radLat2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(delta_lon);
        double delta_sigma = Math.atan2(top, bottom);
        double distance = delta_sigma * 6378137.0;
        return distance;
    }

    public static double Rad(double d)
    {
        return d * Math.PI / 180.0;
    }

    public void compress(int first,int last,boolean tag[])
    {   double x1,x2,y1,y2,distance;
        double maxdistance=0;
        int maxindex=0;
    	for(int ii=first+1;ii<last;ii++)
    	{
    		x1=list.get(first).getLat();
    		x2=list.get(last).getLat();
    		y1=list.get(first).getLon();
    		y2=list.get(last).getLon();
    		distance=distance(x1,x2,y1,y2,ii);
    		if(distance>maxdistance)
    		{
    			maxdistance=distance;
    			maxindex=ii;
    		}
    	}
    	if(maxdistance>30&&maxindex>first&&maxindex<last)
    	{
    		tag[maxindex]=true;
    		compress(first,maxindex,tag);
    		compress(maxindex,last,tag);
    	}
    }
    public static double getMeanErro(List<Node> compresslist2)
    {
    	double erro=0;
    	for(int ii=1;ii<compresslist.size();ii++)
    	{
    		int start=compresslist2.get(ii-1).getId();
    		int end=compresslist2.get(ii).getId();
    		for(int jj=start+1;jj<end;jj++)
    		{
    			if((list.get(start).getLat()==list.get(jj).getLat()&&list.get(start).getLon()==list.get(jj).getLon())||((list.get(end).getLat()==list.get(jj).getLat())&&(list.get(end).getLon()==list.get(jj).getLon())))
                    continue;
    			erro+=distance(list.get(start).getLat(),list.get(end).getLat(),list.get(start).getLon(),list.get(end).getLon(),jj);
    		}
    	}
    	return erro/list.size();
    }
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File fp=new File("d:\\GPS.log");
		File fp1=new File("d:\\output.log");
		if(!fp1.exists())
		{
			try {
				fp1.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
           TrajectoryCompress trajector=new TrajectoryCompress();
           list=new ArrayList<>();
           compresslist=new ArrayList<>();
           String s;
           int ii=0;
       	try {
   			fr=new FileReader(fp);
   			BufferedReader br=new BufferedReader(fr);
   			while((s=br.readLine())!=null)
   			{
   			String[] s1=s.split(" ");
   			String degree=s1[5].substring(0, 2);
   			String min=s1[5].substring(2, s1[5].length());
   			double latitude=Double.parseDouble(degree)+(Double.parseDouble(min)/60);
   			degree=s1[3].substring(0, 3);
   			min=s1[3].substring(3, s1[3].length());
   			double lontitude=Double.parseDouble(degree)+(Double.parseDouble(min)/60);
   			Node node=new Node(latitude,lontitude,s1[1],s1[2],ii);
   			list.add(node);
   			ii++;}
   		} catch (Exception e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}
       	System.out.println("节点数"+ii);
           boolean[] tag=new boolean[list.size()];
           trajector.compress(0, list.size()-1,tag);
           int jj;
           int ff=0;
           try {
			fw=new FileWriter(fp1,true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
           bw=new BufferedWriter(fw);
           for(jj=0;jj<list.size();jj++)
           {
        	   if(tag[jj]==true)
        		   {compresslist.add(list.get(jj));
        		   System.out.println(list.get(jj).getInformation()+"第"+jj+"个坐标");
        		   try {
					bw.write(list.get(jj).getOutput());
					bw.newLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		   ff++;}
        	   
           }
           System.out.println("压缩了"+(list.size()-ff));
           double erros=getMeanErro(compresslist);
           System.out.println("平均误差率"+erros);
           try {
			bw.flush();
			bw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
           
	}

}
class Node
{double lat,lon;
	String date,time;
	int id;
	public Node(double lat,double lon,String date,String time,int id)
	{
		this.lon=lon;
		this.lat=lat;
		this.date=date;
		this.time=time;
		this.id=id;
	}
	public String getInformation()
	{
		return new String("年"+date+" "+"时间"+time+" "+"纬度"+lat+" 经度"+" "+lon);
	}
	public String getOutput() {
		return new String(date+" "+time+" "+lat+" "+lon);
	}
	public double getLat()
	{return lat;}
	public double getLon()
	{
		return lon;
	}
	public int getId()
	{return id;}
	}
