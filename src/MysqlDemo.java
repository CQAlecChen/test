import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
public class MysqlDemo {
    public static void main(String[] args) throws Exception {
        Connection conn = null;
        String sql;
        // MySQL��JDBC URL��д��ʽ��jdbc:mysql://�������ƣ����Ӷ˿�/���ݿ������?����=ֵ
        // ������������Ҫָ��useUnicode��characterEncoding
        // ִ�����ݿ����֮ǰҪ�����ݿ����ϵͳ�ϴ���һ�����ݿ⣬�����Լ�����
        // �������֮ǰ��Ҫ�ȴ���javademo���ݿ�
        String url = "jdbc:mysql://192.168.1.50:3306/test?"
                + "user=creditdev&password=creditdev&useUnicode=true&characterEncoding=UTF8";
 
        try {
            // ֮����Ҫʹ������������䣬����ΪҪʹ��MySQL����������������Ҫ��������������
            // ����ͨ��Class.forName�������ؽ�ȥ��Ҳ����ͨ����ʼ������������������������ʽ������
            Class.forName("com.mysql.jdbc.Driver");// ��̬����mysql����
 
//            System.out.println("�ɹ�����MySQL��������");
            // һ��Connection����һ�����ݿ�����
            conn = DriverManager.getConnection(url);
            // Statement������кܶ෽��������executeUpdate����ʵ�ֲ��룬���º�ɾ����
            Statement stmt = conn.createStatement();
            sql = "select* from test.json_test";
            ResultSet rs = stmt.executeQuery(sql);
          List<JsonTest> list=new ArrayList<JsonTest>();
          while (rs.next()) {
        	  JsonTest j=new JsonTest();
        	  j.setClientId(Integer.parseInt(rs.getString(1)));
        	  j.setJson(rs.getString(2));
        	  list.add(j);
          }
          for(JsonTest j:list){
        	  if(j.getJson()!=null) {
	        	  JSONObject json = JSONObject.parseObject(j.getJson());
//	        	  System.out.println(json);
	        	  JSONObject reports = JSONObject.parseObject(json.getJSONObject("result").getJSONArray("query_reports").get(0).toString());
//	        	  System.out.println(reports.getJSONArray("product_res_detail"));
	        	  JSONArray array = reports.getJSONArray("product_res_detail");
	        	  List<DetailList> receiverList = array.toJavaList(DetailList.class);
	        	  System.out.print(j.getClientId()+":");
//	        	  System.out.print(j.getClientId());
	        	  for(DetailList l:receiverList) {
	        		  int sum=0;
	        		  if(!(l.getName().equals("webloan_hit_summary"))&&!(l.getName().equals("webloan_risk_info"))) {
//	        		  if(l.getName().equals("webloan_hit_detail_m12")) {
		        		  for(Detail d:l.getDetail()) {
		        			  sum+=d.getCount();
		        		  }
		        		  System.out.print(sum+" ");
	        		  }
	        	  }
	        	  System.out.println();
        	  }
          }
        } catch (SQLException e) {
            System.out.println("MySQL��������");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.close();
        }
 
    }
//test update
}
