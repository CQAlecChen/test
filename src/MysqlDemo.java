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
        // MySQL的JDBC URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称?参数=值
        // 避免中文乱码要指定useUnicode和characterEncoding
        // 执行数据库操作之前要在数据库管理系统上创建一个数据库，名字自己定，
        // 下面语句之前就要先创建javademo数据库
        String url = "jdbc:mysql://192.168.1.50:3306/test?"
                + "user=creditdev&password=creditdev&useUnicode=true&characterEncoding=UTF8";
 
        try {
            // 之所以要使用下面这条语句，是因为要使用MySQL的驱动，所以我们要把它驱动起来，
            // 可以通过Class.forName把它加载进去，也可以通过初始化来驱动起来，下面三种形式都可以
            Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
 
//            System.out.println("成功加载MySQL驱动程序");
            // 一个Connection代表一个数据库连接
            conn = DriverManager.getConnection(url);
            // Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
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
            System.out.println("MySQL操作错误！");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.close();
        }
 
    }
//test update
}
